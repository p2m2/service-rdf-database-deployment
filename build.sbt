lazy val scala212 = "2.12.16"
lazy val scala213 = "2.13.8"
lazy val supportedScalaVersions = List(scala212, scala213)

scalaVersion := scala213
name := "service-rdf-database-deployment"
organization := "com.github.p2m2"
organizationName := "p2m2"
organizationHomepage := Some(url("https://www6.inrae.fr/p2m2"))
licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
description := "Service to deploy RDF file on a Spark/Hadoop Cluster"
scmInfo := Some(
  ScmInfo(
    url("https://github.com/p2m2/service-rdf-database-deployment"),
    "scm:git@github.com:p2m2/service-rdf-database-deployment.git"
  )
)

developers := List(
  Developer("ofilangi", "Olivier Filangi", "olivier.filangi@inrae.fr",url("https://github.com/ofilangi"))
)
val static_version_build = "0.4.1"
val version_build = scala.util.Properties.envOrElse("VERSION", static_version_build)

version := "test"

credentials += {
  val realm = scala.util.Properties.envOrElse("REALM_CREDENTIAL", "" )
  val host = scala.util.Properties.envOrElse("HOST_CREDENTIAL", "" )
  val login = scala.util.Properties.envOrElse("LOGIN_CREDENTIAL", "" )
  val pass = scala.util.Properties.envOrElse("PASSWORD_CREDENTIAL", "" )

  val file_credential = Path.userHome / ".sbt" / ".credentials"

  if (reflect.io.File(file_credential).exists) {
    Credentials(file_credential)
  } else {
    Credentials(realm,host,login,pass)
  }
}

publishTo := {
  if (isSnapshot.value)
    Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")
  else
    Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}

publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
pomIncludeRepository := { _ => false }
publishMavenStyle := true

lazy val rdf4jVersion = "4.0.2"
lazy val slf4jVersion = "1.7.36"
lazy val uTestVersion = "0.7.11"

crossScalaVersions := supportedScalaVersions


libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.0.1",
  "org.eclipse.rdf4j" % "rdf4j-storage" % rdf4jVersion,
  "com.github.jsonld-java" % "jsonld-java" % "0.13.4",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion,
  "com.lihaoyi" %% "utest" % uTestVersion % "test"
)


Test / parallelExecution := false
testFrameworks += new TestFramework("utest.runner.Framework")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assembly / target := file("assembly")
assembly / assemblyJarName := "service-rdf-database-deployment.jar"
Global / onChangedBuildSource := ReloadOnSourceChanges
