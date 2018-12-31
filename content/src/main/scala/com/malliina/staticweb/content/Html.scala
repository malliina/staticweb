package com.malliina.staticweb.content

import com.malliina.staticweb.Constants.HelloContainerId
import com.malliina.staticweb.content.Html.defer
import scalatags.Text.all._

object Html {
  val defer = attr("defer").empty

  def apply(css: Seq[String], js: Seq[String]): Html = new Html(css, js)
}

class Html(css: Seq[String], js: Seq[String]) {
  def index = template(
    div(id := HelloContainerId)(
      h1("Hi")
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
