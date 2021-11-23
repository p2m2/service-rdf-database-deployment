scalaVersion := "2.13.6"
name := "service-rdf-database-deployment"
libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.1"
assembly / target := file("assembly")
assembly / assemblyJarName := "service-rdf-database-deployment.jar"