package com.malliina.content

import java.io._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.util.zip.GZIPOutputStream

import com.google.cloud.storage.Acl.{Role, User}
import com.google.cloud.storage.{Acl, BlobInfo}
import com.malliina.content.GCP.log
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters.{asScalaIteratorConverter, mutableSeqAsJavaListConverter}
import scala.collection.mutable

object GCP {
  private val log = LoggerFactory.getLogger(getClass)

  def apply(dist: Path, bucketName: String) = new GCP(dist, bucketName, StorageClient())

  // https://stackoverflow.com/a/27917071
  def deleteDirectory(dir: Path): Path = {
    if (Files.exists(dir)) {
      Files.walkFileTree(dir, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      })
    } else {
      dir
    }
  }
}

/** Deploys files in `dist` to `bucketName` in Google Cloud Storage.
  */
class GCP(dist: Path, bucketName: String, client: StorageClient) {
  val bucket = client.bucket(bucketName)

  val defaultContentType = "application/octet-stream"
  val contentTypes = Map(
    "html" -> "text/html",
    "js" -> "text/javascript",
    "css" -> "text/css",
    "jpg" -> "image/jpg"
  )

  val defaultCacheControl = "private, max-age=0"
  val cacheControls = Map(
    "js" -> "public, max-age=31536000",
    "css" -> "public, max-age=31536000",
    "html" -> "public, max-age=10"
  )

  def deploy(): Unit = {
    val files = Files.list(dist).iterator().asScala.toList
    files.foreach { file =>
      val name = file.getFileName.toString
      val extension = ext(file)
      val contentType = contentTypes.getOrElse(extension, defaultContentType)
      val isFingerprinted = name.count(_ == '.') > 1
      val cacheControl =
        if (isFingerprinted) cacheControls.getOrElse(extension, defaultCacheControl)
        else defaultCacheControl
      val blob = BlobInfo.newBuilder(bucketName, name)
        .setContentType(contentType)
        .setAcl(mutable.Buffer(Acl.of(User.ofAllUsers(), Role.READER)).asJava)
        .setContentEncoding("gzip")
        .setCacheControl(cacheControl)
        .build()
      val gzipFile = Files.createTempFile(name, "gz")
      gzip(file, gzipFile)
      client.upload(blob, gzipFile)
      log.info(s"Uploaded '$file' to '$bucketName' as '$contentType'.")
    }
    val index = "index.html"
    if (files.exists(_.getFileName.toString == index)) {
      bucket.toBuilder.setIndexPage(index).build().update()
      log.info(s"Set index page to '$index'.")
    }
    val notfound = "notfound.html"
    if (files.exists(_.getFileName.toString == notfound)) {
      bucket.toBuilder.setNotFoundPage(notfound).build().update()
      log.info(s"Set 404 page to '$notfound'.")
    }
    log.info(s"Deployed to '$bucketName'.")
  }

  def gzip(src: Path, dest: Path): Unit =
    using(new FileInputStream(src.toFile)) { in =>
      using(new FileOutputStream(dest.toFile)) { out =>
        using(new GZIPOutputStream(out, 8192)) { gzip =>
          copyStream(in, gzip)
          gzip.finish()
        }
      }
    }

  // Adapted from sbt-io
  private def copyStream(in: InputStream, out: OutputStream): Unit = {
    val buffer = new Array[Byte](8192)

    def read(): Unit = {
      val byteCount = in.read(buffer)
      if (byteCount >= 0) {
        out.write(buffer, 0, byteCount)
        read()
      }
    }

    read()
  }

  def using[T <: AutoCloseable, U](res: T)(code: T => U): U = try {
    code(res)
  } finally {
    res.close()
  }

  def ext(path: Path) = {
    val name = path.getFileName.toString
    val idx = name.lastIndexOf('.')
    if (idx >= 0 && name.length > idx + 1) name.substring(idx + 1)
    else ""
  }
}
