package com.malliina.staticweb

import org.scalajs.dom.document
import scalatags.JsDom.all._

object StaticApp {
  def main(args: Array[String]): Unit = {
    val content =
      div(
        h1("Hi"),
        p("Welcome to Scala.js")
      )
    document.body.appendChild(content.render)
  }
}
