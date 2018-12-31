package com.malliina.staticweb.js

import com.malliina.staticweb.Constants.HelloContainerId
import org.scalajs.dom.document
import scalatags.JsDom.all._

object StaticApp {
  def main(args: Array[String]): Unit = {
    Option(document.getElementById(HelloContainerId)).foreach { container =>
      container.appendChild(p("Welcome to Scala.js").render)
    }
  }
}
