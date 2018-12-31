package com.malliina.content

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}

import scalatags.Text

case class TagPage(tags: Text.TypedTag[String]) {
  override def toString = tags.toString()

  def toBytes = s"${TagPage.DocTypeTag}${tags.render}".getBytes(StandardCharsets.UTF_8)

  def toFile(file: Path) = {
    if (!Files.exists(file)) Files.createFile(file)
    Files.write(file, toBytes, StandardOpenOption.TRUNCATE_EXISTING)
  }
}

object TagPage {
  val DocTypeTag = "<!DOCTYPE html>"
}
