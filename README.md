# staticweb

This small application demonstrates deployment of a static website to Google Cloud Storage.

The build uses [scalajs-bundler](https://github.com/scalacenter/scalajs-bundler) to integrate 
[Scala.js](https://www.scala-js.org/) with [webpack](https://webpack.js.org/). 

HTML is generated with [ScalaTags](https://github.com/lihaoyi/scalatags) at build-time.

## Development

A live reload environment is supported for local development.

First, launch incremental compilation:

    sbt ~content/build
    
Then, navigate to 

    http://localhost:12345

Any code changes to JavaScript (in module *client*) or HTML (in module *content*) will trigger a recompilation 
followed by a browser refresh.

## Deploy

To deploy the app to [static.malliina.com](https://static.malliina.com), run:

    sbt content/deploy

The *deploy* command builds and outputs assets to the *dist* directory, then uploads gzipped versions of the files to 
Google Cloud Storage.
