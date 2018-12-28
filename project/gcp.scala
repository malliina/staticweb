import java.io.{FileInputStream, IOException}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Acl.{Role, User}
import com.google.cloud.storage.{Acl, BlobInfo, Storage, StorageOptions}
import sbt.fileToRichFile
import sbt.internal.util.ManagedLogger
import sbt.io.IO

import scala.collection.JavaConverters.{asJavaCollectionConverter, asScalaIteratorConverter, mutableSeqAsJavaListConverter}
import scala.collection.mutable

object StorageClient {
  val credentialsFile = Paths.get(sys.props("user.home")).resolve(".gcp").resolve("credentials.json")

  def apply(): StorageClient =
    new StorageClient(StorageOptions.newBuilder().setCredentials(credentials).build().getService)

  def credentials = {
    val file = sys.env.get("GOOGLE_APPLICATION_CREDENTIALS").map(Paths.get(_)).getOrElse(credentialsFile)
    GoogleCredentials.fromStream(new FileInputStream(file.toFile))
      .createScoped(Seq("https://www.googleapis.com/auth/cloud-platform").asJavaCollection)
  }
}

class StorageClient(val client: Storage) {
  def bucket(name: String) = client.get(name)

  def upload(blob: BlobInfo, file: Path) = client.create(blob, Files.readAllBytes(file))
}

object GCP {
  def apply(dist: Path, log: ManagedLogger) = new GCP(dist, "static.malliina.com", log, StorageClient())

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
class GCP(dist: Path, bucketName: String, log: ManagedLogger, client: StorageClient) {
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
      val extension = file.toFile.ext
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
      IO.gzip(file.toFile, gzipFile.toFile)
      client.upload(blob, gzipFile)
      log.info(s"Uploaded '$file' to '$bucketName' as '$contentType'.")
    }
    val index = "index.html"
    if (files.exists(_.getFileName.toString == index)) {
      bucket.toBuilder.setIndexPage(index).build().update()
      log.info(s"Set index page to '$index'.")
    }
    log.info(s"Deployed to '$bucketName'.")
  }
}
