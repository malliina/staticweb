Seq(
  "org.scala-js" % "sbt-scalajs" % "0.6.26",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0",
  "ch.epfl.scala" % "sbt-scalajs-bundler" % "0.14.0",
  "com.lihaoyi" % "workbench" % "0.4.1",
  "org.scalameta" % "sbt-mdoc" % "1.2.0"
).map(addSbtPlugin)
