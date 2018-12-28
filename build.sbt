import java.nio.file.Path

name := "staticweb"
enablePlugins(ScalaJSBundlerPlugin)

version := "0.0.1"
scalaVersion := "2.12.8"

version in webpack := "4.28.2"
version in startWebpackDevServer := "3.1.4"

emitSourceMaps := false

webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()
webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.dev.config.js")

webpackBundlingMode in fullOptJS := BundlingMode.Application
webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack.prod.config.js")
webpackExtraArgs in fullOptJS := Seq(s"--env.dist=${distPath.value}")

npmDevDependencies in Compile ++= Seq(
//  "html-webpack-plugin" -> "3.0.6",
//  "html-webpack-exclude-assets-plugin" -> "0.0.7",
//  "html-webpack-include-assets-plugin" -> "1.0.6",
  "webpack-merge" -> "4.1.5",
  "style-loader" -> "0.23.1",
  "css-loader" -> "2.1.0",
  "mini-css-extract-plugin" -> "0.5.0",
  "postcss-loader" -> "3.0.0",
  "postcss-import" -> "12.0.1",
  "postcss-preset-env" -> "6.5.0",
  "autoprefixer" -> "9.4.3",
  "cssnano" -> "4.1.8",
  "sugarss" -> "2.0.0"
)

scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
  "com.lihaoyi" %%% "scalatags" % "0.6.7",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.scalatest" %%% "scalatest" % "3.0.5" % Test
)

val distPath = settingKey[String]("Path to built prod distribution (relative to base directory)")
val distDirectory = settingKey[Path]("Prod distribution directory")
val cleanDist = taskKey[Unit]("Cleans dist directory")
val deploy = taskKey[Unit]("Deploys the application")

distPath := "dist"
(webpack in(Compile, fastOptJS)) := {
  val files = (webpack in(Compile, fastOptJS)).value
  Html.generate(files, (crossTarget in(Compile, fastOptJS)).value.toPath, Nil, streams.value.log)
  files
}
(webpack in(Compile, fullOptJS)) := {
  val files = (webpack in(Compile, fullOptJS)).value
  // Excludes scripts emitted from CSS extraction
  val excludedScripts = Seq("styles", "fonts")
  Html.generate(files, distDirectory.value, excludedScripts, streams.value.log)
  files
}
distDirectory := (baseDirectory.value / distPath.value).toPath
cleanDist := GCP.deleteDirectory(distDirectory.value)
deploy := GCP(distDirectory.value, streams.value.log).deploy()
deploy := ((deploy dependsOn (webpack in(Compile, fullOptJS))) dependsOn cleanDist).value
