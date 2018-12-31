import java.nio.file.Path

import autowire._
import com.lihaoyi.workbench.Api
import com.lihaoyi.workbench.WorkbenchBasePlugin.server
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.concurrent.ExecutionContext

val distPath = settingKey[String]("Path to built prod distribution (relative to base directory)")
val distDirectory = settingKey[Path]("Prod distribution directory")
val cleanDist = taskKey[Unit]("Cleans dist directory")
val deploy = taskKey[Unit]("Deploys the application")
val build = taskKey[Unit]("Builds the content")
val prepare = taskKey[Unit]("Generates the site for deployment")
val Static = config("static")

ThisBuild / distPath := "dist"
ThisBuild / distDirectory := (baseDirectory.value / distPath.value).toPath

val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.12.8"
)

val shared = crossProject(JSPlatform, JVMPlatform)
  .settings(commonSettings)

val sharedJs = shared.js
val sharedJvm = shared.jvm

val client: Project = project.in(file("client"))
  .enablePlugins(ScalaJSBundlerPlugin, WorkbenchBasePlugin)
  .dependsOn(sharedJs)
  .settings(commonSettings)
  .settings(
    version in webpack := "4.28.2",
    version in startWebpackDevServer := "3.1.4",
    emitSourceMaps := false,
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.dev.config.js"),
    webpackBundlingMode in fullOptJS := BundlingMode.Application,
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack.prod.config.js"),
    webpackExtraArgs in fullOptJS := Seq(s"--env.dist=../${distPath.value}"),
    npmDevDependencies in Compile ++= Seq(
      "webpack-merge" -> "4.1.5",
      "style-loader" -> "0.23.1",
      "css-loader" -> "2.1.0",
      "mini-css-extract-plugin" -> "0.5.0",
      "postcss-loader" -> "3.0.0",
      "postcss-import" -> "12.0.1",
      "postcss-preset-env" -> "6.5.0",
      "autoprefixer" -> "9.4.3",
      "cssnano" -> "4.1.8"
    ),
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.6.7",
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "org.scalatest" %%% "scalatest" % "3.0.5" % Test
    ),
    workbenchDefaultRootObject := Some((s"${distDirectory.value}/index.html", s"${distDirectory.value}/"))
  )

val content: Project = project.in(file("content"))
  .dependsOn(sharedJvm)
  .settings(commonSettings)
  .settings(
    version := "0.0.1",
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-storage" % "1.55.0",
      "com.lihaoyi" %% "scalatags" % "0.6.7",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "ch.qos.logback" % "logback-core" % "1.2.3"
    ),
    build := Def.taskDyn {
      val files = webpack.in(client, Compile, fastOptJS in client).value
      files.foreach { f =>
        val meta = f.metadata.get(BundlerFileTypeAttr)
        // With BundlingMode.LibraryOnly(), these two are not processed by webpack, so we copy them manually
        if (meta.contains(BundlerFileType.Loader) || meta.contains(BundlerFileType.Application)) {
          IO.copyFile(f.data, distDirectory.value.resolve(f.data.name).toFile)
        }
      }
      val excludedScripts = Seq("styles", "fonts")
      val assets = AssetUtils.assetGroup(files, excludedScripts).mkString
      run in Compile toTask s" build ${distDirectory.value} /workbench.js $assets"
    }.value,
    // Watches both JS sources and content sources, so that recompilation is triggered when either changes
    watchSources := watchSources.value ++ (watchSources in client).value,
    refreshBrowsers := {
      implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
      (server in client).value.Wire[Api].reload().call()
    },
    refreshBrowsers := refreshBrowsers.triggeredBy(build).value,
    clean in Static := {
      AssetUtils.deleteDirectory(distDirectory.value)
    },
    prepare := Def.taskDyn {
      val files = webpack.in(client, Compile, fullOptJS in client).value
      // Excludes scripts emitted from CSS extraction
      val excludedScripts = Seq("styles", "fonts")
      val assets = AssetUtils.assetGroup(files, excludedScripts).mkString
      run in Compile toTask s" build ${distDirectory.value} $assets"
    }.dependsOn(clean in Static).value,
    deploy := Def.taskDyn {
      run in Compile toTask s" deploy ${distDirectory.value} static.malliina.com"
    }.value,
    deploy := (deploy dependsOn prepare).value
  )
