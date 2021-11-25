scalaVersion := "2.13.6"
name := "service-rdf-database-deployment"

lazy val rdf4jVersion = "3.7.4"
lazy val slf4jVersion = "1.7.32"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.0.1",
  "org.eclipse.rdf4j" % "rdf4j-storage" % rdf4jVersion % "provided",
  "com.github.jsonld-java" % "jsonld-java" % "0.13.3",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assembly / target := file("assembly")
assembly / assemblyJarName := "service-rdf-database-deployment.jar"
Global / onChangedBuildSource := ReloadOnSourceChanges