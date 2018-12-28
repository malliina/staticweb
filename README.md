# staticweb

This small application demonstrates deployment of a static website to Google Cloud Storage.

The build uses [scalajs-bundler](https://github.com/scalacenter/scalajs-bundler) to integrate 
[Scala.js](https://www.scala-js.org/) with [webpack](https://webpack.js.org/).

## Deploy

To deploy the app to [static.malliina.com](https://static.malliina.com), run:

    sbt deploy

The *deploy* command builds and outputs assets to the *dist* directory, then uploads gzipped versions of the files to 
Cloud Storage.
