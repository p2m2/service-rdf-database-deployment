package fr.inrae.metabolomics.services

import fr.inrae.semantic_web.ProvenanceBuilder
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil

import java.io.{BufferedWriter, File, FileWriter}

case object ServiceRdfDatabaseDeployment extends App {

        val rootPathDatabasesHdfsCluster = "/rdf"
        val hdfs = "/usr/local/hadoop/bin/hdfs"

        import scopt.OParser

        case class Config(
                           category: String = "metabohub",
                           database: String = "",
                           release : String = "",
                           soft    : String = "",
                           startDate: String = "",
                           askOmicsAbstraction: Option[String] = None,
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
                        opt[String]("release")
                          .required()
                          .action({ case (r, c) => c.copy(release = r) })
                          .valueName("<release>")
                          .text("release name"),
                        opt[String]("soft")
                          .required()
                          .action({ case (r, c) => c.copy(soft = r) })
                          .valueName("<soft>")
                          .text("soft in charge of the RDF generation"),
                        opt[String]("database")
                          .required()
                          .action({ case (r, c) => c.copy(database = r) })
                          .valueName("<database>")
                          .text("database *name*"),
                        opt[String]("start-date")
                          .required()
                          .action({ case (r, c) => c.copy(startDate = r) })
                          .validate(x =>
                                  if ( XMLDatatypeUtil.isValidDateTime(x) ) success
                                  else failure("start-date : bad datetime format"))
                          .valueName("<start-date>")
                          .text("start datetime of the RDF generation file."),
                        opt[String]("askomics-abstraction")
                          .optional()
                          .action({ case (r, c) => c.copy(askOmicsAbstraction = Some(r)) })
                          .valueName("<askomics-abstraction>")
                          .text("askomics-abstraction *name*"),

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
                        buildScript(config.files, config.output,config.category,
                                config.database,config.release,config.askOmicsAbstraction,config.soft,config.startDate)
                case _ =>
                        // arguments are bad, error message will have been displayed
                        System.err.println("exit with error.")
        }

        def buildScript(
                         files: Seq[String],
                         output: File,
                         category: String,
                         databaseName: String,
                         release : String,
                         abstraction_askomics : Option[String],
                         soft : String,
                         startDate : String
                       ): Unit = {

                val bw = new BufferedWriter(new FileWriter(new File(output.getPath)))

                val dirData =s"${rootPathDatabasesHdfsCluster}/${category}/${databaseName}/${release}"
                val dirAskOmicsAbstraction =s"${rootPathDatabasesHdfsCluster}/askomics/"
                val dirProvData = s"${rootPathDatabasesHdfsCluster}/prov/"

                bw.write("#!/bin/bash\n")

                bw.write(s"$hdfs dfs -mkdir -p ${dirData}\n")
                bw.write(s"$hdfs dfs -mkdir -p ${dirAskOmicsAbstraction}\n")
                bw.write(s"$hdfs dfs -mkdir -p ${dirProvData}\n")
                files.filter(
                        x => x.matches("^(http|https|ftp)://.*$")
                ).foreach(
                        x => bw.write(s"wget $x\n")
                )
                bw.write(s"$hdfs dfs -put -f ${files.map(x => "$(basename "+x+")").mkString(" ")} ${dirData}\n")

                abstraction_askomics match {
                        case Some(file) if file.endsWith(".ttl") =>
                                if ( file.matches("^(http|https|ftp)://.*$"))
                                        bw.write(s"wget $file\n")
                                bw.write(s"$hdfs dfs -put -f "+"$("+s"basename $file) " +
                                  s"${dirAskOmicsAbstraction}/${category}-${databaseName}-${release}-askomics.ttl\n")
                        case Some( f ) => System.err.println(s"Can not manage this Askomics extension file ${f}")
                        case None => System.err.println(s"None askomics abstraction is provided . ")
                }

                val fileProv = s"${category}-${databaseName}-${release}-prov.jsonld"
                /* !! create file inside the output script on the current directory (should be /tmp/CI/{CI_ID_JOB})!! */
                bw.write("cat << EOF > $PWD/"+s"${fileProv}\n")
                bw.write(ProvenanceBuilder.build(category,databaseName,release,soft,startDate))
                bw.write("\nEOF\n")
                bw.write(s"$hdfs dfs -put -f ${fileProv} ${dirProvData}\n")

                bw.close()
                println("output script file:" + output.getPath)

        }
}