organization := "com.github.adnanyaqoobvirk"

name := "datadog-scala"

scalaVersion := "2.11.8"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

scalacOptions in Test ++= Seq("-Yrangepos")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.4",
  "org.json4s" %% "json4s-native" % "3.5.0",
  "org.json4s" %% "json4s-jackson" % "3.5.0",
  "org.specs2" %% "specs2-core" % "3.0.1" % "test"
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

Publish.settings