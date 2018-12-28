addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.26")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.14.0")

libraryDependencies ++= Seq(
  "com.google.cloud" % "google-cloud-storage" % "1.55.0",
  "com.lihaoyi" %% "scalatags" % "0.6.7"
)
