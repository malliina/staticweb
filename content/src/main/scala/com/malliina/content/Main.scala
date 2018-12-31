package com.malliina.content

import java.nio.file.Paths

object Main {
  def main(args: Array[String]): Unit = {
    val command = args(0)
    val target = Paths.get(args(1))
    command match {
      case "build" =>
        val files = args.drop(2)
        val css = files.filter(_.endsWith(".css"))
        val js = files.filter(_.endsWith(".js"))
        Html.generate(SiteSpec(css, js, target))
      case "deploy" =>
        val bucket = args(2)
        GCP(target, bucket).deploy()
      case other =>
        throw new Exception(s"Unknown input: '$other'.")
    }
  }
}
