package fr.inrae.metabolomics.services

import java.io.{BufferedWriter, File, FileWriter}

case object ServiceRdfDatabaseDeployment extends App {

        val rootPathDatabasesHdfsCluster = "/rdf"

        import scopt.OParser

        case class Config(
                           category: String = "metabohub",
                           database: String = "unknown",
                           files: Seq[String] = Seq(),
                           output: File = new File("./script.sh"))

        val builder = OParser.builder[Config]

        val parser = {
                import builder._
                OParser.sequence(
                        programName("service-rdf-database-deployment"),
                        head("service-rdf-database-deployment", "1.0"),
                        opt[File]('o', "output")
                          .required()
                          .valueName("<output>")
                          .action((x, c) => c.copy(output = x))
                          .text("output script is a required file property"),
                        opt[String]("category")
                          .required()
                          .action({ case (r, c) => c.copy(category = r) })
                          .valueName("<category>")
                          .text("database category. should be metabohub(default),ext"),
                        opt[String]("database")
                          .required()
                          .action({ case (r, c) => c.copy(database = r) })
                          .valueName("<database>")
                          .text("database *name*"),
                        help("help").text("prints this usage text"),
                        arg[String]("<file>...")
                          .unbounded()
                          .action((x, c) => c.copy(files = c.files :+ x))
                          .text("optional unbounded args"),
                        note("some notes." + sys.props("line.separator")),
                        checkConfig(_ => success)
                )
        }

        println("#!/bin/bash")
        println("# == service database deployment / Metabolomics Semantic Data lake / MetaboHUB == ")
        // if you want to access the command line args:
        println("# -- args : ");
        args.foreach(print)
        println("")
        val script =
                """
          hdfs dfs mkdir
          """.stripMargin
        println()

        // OParser.parse returns Option[Config]
        OParser.parse(parser, args, Config()) match {
                case Some(config) =>
                        // do something
                        println(config)
                        buildScript(config.files, config.output,config.category,config.database)
                case _ =>
                        // arguments are bad, error message will have been displayed
                        System.err.println("exit with error.")
        }

        def buildScript(files: Seq[String], output: File, category: String, databaseName: String): Unit = {

                val bw = new BufferedWriter(new FileWriter(new File(output.getPath)))

                bw.write("#!/bin/bash\n")
                bw.write(s"/usr/local/hadoop/bin/hdfs dfs -mkdir ${rootPathDatabasesHdfsCluster}/${category}/${databaseName}\n")
                bw.write(s"/usr/local/hadoop/bin/hdfs dfs -put -f ${files.mkString(" ")} ${rootPathDatabasesHdfsCluster}/${category}/${databaseName}\n")
                bw.close()
                println("output script file:" + output.getPath)

        }
}