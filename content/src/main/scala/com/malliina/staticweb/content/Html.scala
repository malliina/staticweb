package com.malliina.staticweb.content

import java.nio.file.{Files, Path, StandardOpenOption}

import com.malliina.staticweb.Constants.HelloContainerId
import com.malliina.staticweb.content.Html.defer
import org.slf4j.LoggerFactory
import scalatags.Text.all._

case class SiteSpec(css: Seq[String], js: Seq[String], to: Path)

object Html {
  private val log = LoggerFactory.getLogger(getClass)

  val defer = attr("defer").empty

  def apply(css: Seq[String], js: Seq[String]): Html = new Html(css, js)

  def write(page: TagPage, to: Path): Path = {
    if (!Files.isRegularFile(to)) {
      val dir = to.getParent
      if (!Files.isDirectory(dir))
        Files.createDirectories(dir)
      Files.createFile(to)
    }
    Files.write(to, page.toBytes, StandardOpenOption.TRUNCATE_EXISTING)
    log.info(s"Wrote ${to.toAbsolutePath}.")
    to
  }

  def generate(spec: SiteSpec) = {
    val to = spec.to
    val html = Html(spec.css, spec.js)
    write(html.notFound, to.resolve("notfound.html"))
    write(html.index, to.resolve("index.html"))
  }

}

class Html(css: Seq[String], js: Seq[String]) {
  def index = template(
    div(id := HelloContainerId)(
      h1("Hi!")
    )
  )

  def notFound = template(div(h1("Not found")))

  def template(content: Modifier*) = TagPage(
    html(
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
  )
}
