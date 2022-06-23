package fr.inrae.semantic_web

import org.apache.spark.sql.SparkSession
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.util.Values.literal
import org.eclipse.rdf4j.model.vocabulary._
import org.eclipse.rdf4j.rio.{RDFFormat, Rio, WriterConfig}

import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date

case object ProvenanceBuilder {
  val mapPrefix = Map(
    "rdfs"          -> "http://www.w3.org/2000/01/rdf-schema#",
    "rdf"           -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "xsd"           -> "http://www.w3.org/2000/10/XMLSchema#",
    "owl"           -> "http://www.w3.org/2002/07/owl#",
    "prov"          -> "http://www.w3.org/ns/prov#",
    ""              -> "http://www.metabohub.fr/msd#"
    )

  val formatterXsd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

  val builder : ModelBuilder = new ModelBuilder()

  mapPrefix map { case (k,v) => {
    builder.setNamespace(k,v)
  }}

  val baseUrlSparkHistory : String ="https://spark.metabolomics-datalake.ara.inrae.fr/history"


  def getStringFromModelBuilder(builder : ModelBuilder,extension:String="ttl") : String = {
    val config : WriterConfig = new WriterConfig()
   // config.set(BasicWriterSettings.PRETTY_PRINT, true)

    val stringWriter = new StringWriter()

    val format : RDFFormat = extension match {
      case "jsonld" => RDFFormat.JSONLD
      case "ttl"    => RDFFormat.TURTLE
      case "trig"   => RDFFormat.TRIG
      case "nt"     => RDFFormat.NTRIPLES
      case "n3"     => RDFFormat.N3
      case "rdf"    => RDFFormat.RDFXML
      case _ => throw new IllegalArgumentException(s"Unknown extension : $extension ")
    }

    Rio.write(builder.build(), stringWriter, format, config)
    stringWriter.toString()
  }

  def provSparkSubmit(
                 projectUrl : String = "https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake",
                 category : String,
                 database : String,
                 release : String,
                 startDate:Date,
                spark : SparkSession) : String = {

    val ciProjectUrl = projectUrl+"/"+category+"/"+database
    val ciPipelineUrl = baseUrlSparkHistory+"/"+spark.sparkContext.applicationId
    val urlRelease = projectUrl+"/"+category+"/"+database + "/" + release

      builder
      .subject(s"$ciProjectUrl")
      .add(RDF.TYPE, PROV.ENTITY)
      .add(RDF.TYPE, DCAT.DATASET)
      .add(DCTERMS.TITLE,database)
      .add(DCTERMS.DESCRIPTION,s"Category $category / Database $database" )
      .add(DCTERMS.MODIFIED,literal(formatterXsd.format(startDate),XSD.DATETIME) )
      .add(PROV.WAS_GENERATED_BY, spark.sparkContext.sparkUser)
      .add(DCAT.DISTRIBUTION,s"$urlRelease")
    /*
      Release is a Prov:Entity
    */

    builder
      .subject(s"$urlRelease")
      .add(RDF.TYPE, PROV.ENTITY)
      .add(RDF.TYPE, DCAT.DISTRIBUTION)
      .add(DCTERMS.TITLE,release)
      .add(DCTERMS.MODIFIED,literal(formatterXsd.format(startDate),XSD.DATETIME) )
      .add(PROV.WAS_GENERATED_BY, s"$ciPipelineUrl")
      .add(DCAT.ACCESS_URL,s"hdfs://rdf/${category}/${database}/${release}")

    val endString : String = formatterXsd.format(new Date())

    builder
      .subject(s"$ciPipelineUrl")
      .add(RDF.TYPE, PROV.ACTIVITY)
      .add(PROV.USED,s"$ciProjectUrl")
      .add(PROV.STARTED_AT_TIME,literal(formatterXsd.format(startDate),XSD.DATETIME))
      .add(PROV.ENDED_AT_TIME,literal(endString,XSD.DATETIME))

    getStringFromModelBuilder(builder)

  }

  def build(
             ciProjectUrl:String,
             ciPipelineUrl:String,
             urlRelease:String,
             category : String,
             database : String,
             release : String,
             startDate:String,
             extension : String
           ) : String = {

    builder
      .subject(s"$ciProjectUrl")
      .add(RDF.TYPE, PROV.ENTITY)
      .add(RDF.TYPE, DCAT.DATASET)
      .add(DCTERMS.TITLE,database)
      .add(DCTERMS.DESCRIPTION,s"Category $category / Database $database" )
      .add(DCTERMS.MODIFIED,literal(startDate,XSD.DATETIME) )
      .add(PROV.WAS_GENERATED_BY, "https://github.com/p2m2/service-rdf-database-deployment/")
      .add(DCAT.DISTRIBUTION,s"$urlRelease")
    /*
      Release is a Prov:Entity
      This release is generated by the gitlab origin CI/CD project
    */

    builder
      .subject(s"$urlRelease")
      .add(RDF.TYPE, PROV.ENTITY)
      .add(RDF.TYPE, DCAT.DISTRIBUTION)
      .add(DCTERMS.TITLE,release)
      .add(DCTERMS.MODIFIED,literal(startDate,XSD.DATETIME) )
      .add(PROV.WAS_GENERATED_BY, s"$ciPipelineUrl")
      .add(DCAT.ACCESS_URL,s"hdfs://rdf/${category}/${database}/${release}")

    val endString : String = formatterXsd.format(new Date())

    builder
      .subject(s"$ciPipelineUrl")
      .add(RDF.TYPE, PROV.ACTIVITY)
      .add(PROV.USED,s"$ciProjectUrl")
      .add(PROV.STARTED_AT_TIME,literal(startDate,XSD.DATETIME))
      .add(PROV.ENDED_AT_TIME,literal(endString,XSD.DATETIME))

    getStringFromModelBuilder(builder,extension)
  }
}
