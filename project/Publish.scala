import sbt._
import sbt.Keys._

object Publish {
  lazy val settings = Seq(
    homepage := Some(url("http://github.com/adnanyaqoobvirk/datadog-scala")),
    publishMavenStyle := true,
    publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
            Some("snapshots" at nexus + "content/repositories/snapshots")
        else
            Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    pomExtra := (
      <licenses>
       <license>
        <name>MIT</name>
        <url>https://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
       </license>
      </licenses>
      <scm>
        <url>git@github.com:adnanyaqoobvirk/datadog-scala.git</url>
        <connection>scm:git:git@github.com:adnanyaqoobvirk/datadog-scala.git</connection>
      </scm>
      <developers>
        <developer>
          <name>Cory Watson</name>
          <email>github@onemogin.com</email>
          <organization>Cory Industries Ltd Inc</organization>
          <organizationUrl>https://onemogin.com</organizationUrl>
        </developer>
        <developer>
          <name>Adnan Yaqoob</name>
          <email>adnan@yaqoob.org</email>
          <organization>Yaqoob.org</organization>
          <organizationUrl>https://yaqoob.org</organizationUrl>
        </developer>
      </developers>
    )
  )
}
