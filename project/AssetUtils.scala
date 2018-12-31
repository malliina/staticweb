import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}

import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.{BundlerFileType, BundlerFileTypeAttr}

object AssetUtils {

  case class AssetGroup(scripts: Seq[String], styles: Seq[String]) {
    def mkString = (styles ++ scripts).mkString(" ")
  }

  def assetGroup(files: Seq[sbt.Attributed[sbt.File]], excludePrefixes: Seq[String]): AssetGroup = {
    def filesOf(fileType: BundlerFileType) = files.filter(_.metadata.get(BundlerFileTypeAttr).contains(fileType))

    // Separates app scripts from library ones for ordering
    val apps = filesOf(BundlerFileType.Application) ++ filesOf(BundlerFileType.ApplicationBundle)
    val libraries = filesOf(BundlerFileType.Library)
    val assets = filesOf(BundlerFileType.Asset)
    val loaders = filesOf(BundlerFileType.Loader)
    val scripts = (apps ++ loaders ++ assets ++ libraries).distinct.reverse.map(_.data)
      .filter(f => f.ext == "js" && !excludePrefixes.exists(e => f.name.startsWith(e)))
      .map(_.name).distinct
    val styles = files.map(_.data).filter(_.ext == "css").map(_.name)
    AssetGroup(scripts, styles)
  }

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
