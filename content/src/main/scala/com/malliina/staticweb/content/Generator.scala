package com.malliina.staticweb.content

import java.nio.file.{Files, Path, StandardOpenOption}

import org.slf4j.LoggerFactory

object Generator {
  private val log = LoggerFactory.getLogger(getClass)

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
    write(html.index, to.resolve("index.html"))
    write(html.notFound, to.resolve("notfound.html"))
  }
}

case class SiteSpec(css: Seq[String], js: Seq[String], to: Path)
