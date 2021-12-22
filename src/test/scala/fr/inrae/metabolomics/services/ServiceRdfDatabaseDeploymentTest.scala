package fr.inrae.metabolomics.services

import utest.{TestSuite, Tests, test}

import java.io.{BufferedWriter, FileWriter}
import java.util.Date
import scala.io.Source.fromFile
import scala.reflect.io.File
import scala.sys.process.Process
import scala.util.{Failure, Success, Try}

object ServiceRdfDatabaseDeploymentTest extends TestSuite {
  val uuid = java.util.UUID.randomUUID.toString
  val script_out="test_"+uuid+".sh"
  /**
   * Get base args
   * @return
   */
  def get_base_args(): Array[String] = {
    val date : String = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
      Array(
        "--output",script_out,
        "--category","CATEGORY",
        "--release","RELEASE",
        "--ci-project-url","http://url-project",
        "--ci-pipeline-url","http://url-pipeline",
        "--url-release","http://url-release",
        "--soft","SOFT",
        "--database","DATABASE",
        "--provjsonld","prov.jsonld",
        "--start-date",date
      )
  }

  def get_complex_category_args(): Array[String] = {
    val date : String = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
    Array(
      "--output",script_out,
      "--category","CATE/GORY",
      "--release","RELEASE",
      "--ci-project-url","http://url-project",
      "--ci-pipeline-url","http://url-pipeline",
      "--url-release","http://url-release",
      "--soft","SOFT",
      "--database","DATABASE",
      "--provjsonld","prov.rdf",
      "--start-date",date
    )
  }

  def get_complex_release_args(): Array[String] = {
    val date : String = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
    Array(
      "--output",script_out,
      "--category","CATEGORY",
      "--release","RELE/ASE",
      "--ci-project-url","http://url-project",
      "--ci-pipeline-url","http://url-pipeline",
      "--url-release","http://url-release",
      "--soft","SOFT",
      "--database","DATABASE",
      "--provjsonld","prov.ttl",
      "--start-date",date
    )
  }

  def get_complex_database_args(): Array[String] = {
    val date : String = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
    Array(
      "--output",script_out,
      "--category","CATEGORY",
      "--release","RELEASE",
      "--ci-project-url","http://url-project",
      "--ci-pipeline-url","http://url-pipeline",
      "--url-release","http://url-release",
      "--soft","SOFT",
      "--database","DATA/BASE",
      "--provjsonld","prov.n3",
      "--start-date",date
    )
  }

  def getScriptString() : Seq[String] = {
    fromFile(script_out).getLines().toSeq
  }

  def process_script(lines : Seq[String]) = {
   // println(lines.mkString("\n"))
    val bw = new BufferedWriter(new FileWriter(new java.io.File(script_out)))
    lines.map(
      l => l.replace("/usr/local/hadoop/bin/hdfs","echo")
    ).map(
      l => bw.write(l+"\n")
    )
    bw.close()

    val proc = Process(s"bash $script_out", new java.io.File("."))
    assert(proc.! == 0)
  }

  def checkBasic(lines : Seq[String]) = {

    println(s"=================================$script_out===============================================")
    println(getScriptString().mkString("\n"))
    println("=============================================================================================")
    // all mkdir should have -p option to avoid an error
    assert(!lines.filter( x => x.contains("mkdir")).exists(x => !x.contains(" -p")))
    assert(!lines.exists( x => x.contains("hdfs") && !x.contains("/usr/local/hadoop/bin") ),"Forgot /usr/local/hadoop/bin")
  }

  override def utestAfterAll(): Unit = {
    println(s" --delete $script_out --")
    File(script_out).deleteRecursively()
  }

  val tests = Tests {
    test("ServiceRdfDatabaseDeployment - help") {
      Try(ServiceRdfDatabaseDeployment.main(Array(""))) match {
        case Success(_) => assert(true)
        case Failure(_) => assert(false)
      }
    }
    def base(path: String) = {
      ServiceRdfDatabaseDeployment.main(get_base_args() ++ Array(path))
      checkBasic(getScriptString())
      process_script(getScriptString())
    }

    def complex_category(path: String) = {
      ServiceRdfDatabaseDeployment.main(get_complex_database_args() ++ Array(path))
      checkBasic(getScriptString())
      process_script(getScriptString())
    }

    def complex_release(path: String) = {
      ServiceRdfDatabaseDeployment.main(get_complex_release_args() ++ Array(path))
      checkBasic(getScriptString())
      process_script(getScriptString())
    }

    def complex_database(path: String) = {
      ServiceRdfDatabaseDeployment.main(get_complex_database_args() ++ Array(path))
      checkBasic(getScriptString())
      process_script(getScriptString())
    }

    def askomics(path: String) = {
      ServiceRdfDatabaseDeployment.main(get_base_args() ++
        Array("askomics-abstraction", "something_askomics.ttl" ,path))
      checkBasic(getScriptString())
      process_script(getScriptString())
    }

    test("build - local files") {
      base("/path/to/dir/test.ttl")
    }

    test("build - local files- COMPLEX CATEGORY ") {
      complex_category("/path/to/dir/test.ttl")
    }

    test("build - local files - COMPLEX DATABASE ") {
      complex_database("/path/to/dir/test.ttl")
    }

    test("build - local files - RELEASE RELEASE ") {
      complex_release("/path/to/dir/test.ttl")
    }

    test("build - local files askomics_abstraction") {
      askomics("/path/to/dir/test.ttl")
    }

    test("build - local gz files") {
      base("/path/to/dir/test.ttl.gz")
    }
    test("build - local gz files") {
      complex_category("/path/to/dir/test.ttl.gz")
    }
    test("build - local gz files - COMPLEX DATABASE ") {
      complex_database("/path/to/dir/test.ttl.gz")
    }

    test("build - local gz files - COMPLEX RELEASE ") {
      complex_release("/path/to/dir/test.ttl.gz")
    }

    test("build - local gz files askomics_abstraction") {
      askomics("/path/to/dir/test.ttl.gz")
    }

    test("build - local regex gz files") {
      base("/path/to/dir/test*.ttl.gz")
    }

    test("build - local files- COMPLEX CATEGORY ") {
      complex_category("/path/to/dir/test*.ttl.gz")
    }

    test("build - local files - COMPLEX DATABASE ") {
      complex_database("/path/to/dir/test*.ttl.gz")
    }

    test("build - local files - RELEASE RELEASE ") {
      complex_release("/path/to/dir/test*.ttl.gz")
    }

    test("build - local files askomics_abstraction") {
      askomics("/path/to/dir/test*.ttl.gz")
    }

    test("build - http regex gz files") {
      base("https:/path/to/dir/test*.ttl.gz")
    }

    test("build - http files- COMPLEX CATEGORY ") {
      complex_category("https://path/to/dir/test*.ttl.gz")
    }

    test("build - http files - COMPLEX DATABASE ") {
      complex_database("https://path/to/dir/test*.ttl.gz")
    }

    test("build - http files - RELEASE RELEASE ") {
      complex_release("https://path/to/dir/test*.ttl.gz")
    }

    test("build - http files askomics_abstraction") {
      askomics("https://path/to/dir/test*.ttl.gz")
    }
  }
}
