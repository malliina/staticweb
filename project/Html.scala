import java.nio.charset.StandardCharsets
import java.nio.file.Path

import Html.defer
import sbt.{Logger, _}
import sbt.io.IO
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.{BundlerFileType, BundlerFileTypeAttr}
import scalatags.Text.TypedTag
import scalatags.Text.all._

object Html {
  val defer = attr("defer").empty

  def apply(js: Seq[String], css: Seq[String]): Html = new Html(js, css)

  def generate(files: Seq[sbt.Attributed[sbt.File]], to: Path, excludePrefixes: Seq[String], log: Logger) = {
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
    val html = Html(scripts, styles)
    write(html.notFound, to.resolve("notfound.html"), log)
    write(html.index, to.resolve("index.html"), log)
    files
  }

  def write(html: TypedTag[String], to: Path, log: Logger): Unit = {
    IO.write(to.toFile, html.render.getBytes(StandardCharsets.UTF_8))
    log.info(s"Wrote '$to'.")
  }
}

class Html(js: Seq[String], css: Seq[String]) {
  def index = template(
    div(
      h1("Hi"),
      p("Welcome to Scala.js!")
    )
  )

  def notFound = template(h1("Not found"))

  def template(content: Modifier*) = html(
    head(
      meta(charset := "UTF-8"),
      tag("title")("Scala.js website"),
      css.map { path => link(href := path, rel := "stylesheet") },
      js.map { path => script(defer, src := path, `type` := "text/javascript") }
    ),
    body(
      content
    )
  )
}
