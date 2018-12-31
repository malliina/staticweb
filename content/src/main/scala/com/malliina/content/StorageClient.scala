package com.malliina.content

import java.io.FileInputStream
import java.nio.file.{Files, Path, Paths}

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.{BlobInfo, Storage, StorageOptions}

import scala.collection.JavaConverters.asJavaCollectionConverter

object StorageClient {
  val credentialsFile = Paths.get(sys.props("user.home")).resolve(".gcp").resolve("credentials.json")

  def apply(): StorageClient =
    new StorageClient(StorageOptions.newBuilder().setCredentials(credentials).build().getService)

  def credentials = {
    val file = sys.env.get("GOOGLE_APPLICATION_CREDENTIALS").map(Paths.get(_)).getOrElse(credentialsFile)
    GoogleCredentials.fromStream(new FileInputStream(file.toFile))
      .createScoped(Seq("https://www.googleapis.com/auth/cloud-platform").asJavaCollection)
  }
}

class StorageClient(val client: Storage) {
  def bucket(name: String) = client.get(name)

  def upload(blob: BlobInfo, file: Path) = client.create(blob, Files.readAllBytes(file))
}
