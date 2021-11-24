package fr.inrae.semantic_web

import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.util.Values.literal
import org.eclipse.rdf4j.model.vocabulary.{PROV, RDF, XSD}
import org.eclipse.rdf4j.rio.{RDFFormat, Rio, WriterConfig}

import java.io.StringWriter
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

  val builder : ModelBuilder = new ModelBuilder()

  mapPrefix map { case (k,v) => {
    builder.setNamespace(k,v)
  }}

  def build(category : String, database : String, release : String, soft : String, startDate:String) : String = {

    val graphNamed : String = "http://www.metabohub.fr/msd/prov/" +
      category + "/" +
      database + "/" +
      release + "/"

    builder
      .namedGraph(graphNamed)
      .subject((":"+s"$category"))
      .add(RDF.TYPE, PROV.ENTITY)

    builder
      .namedGraph(graphNamed)
      .subject(":"+s"$database")
      .add(PROV.WAS_ASSOCIATED_WITH, (s":$category"))
      .add(RDF.TYPE, PROV.ENTITY)

    val newBase : String = s":${database}_${release}_rdf"
    val currentActivitySoft : String = s":${soft}_${release}"

    builder
      .namedGraph(graphNamed)
      .subject(newBase)
      .add(RDF.TYPE, PROV.ENTITY)
      .add(PROV.WAS_GENERATED_BY, s":$currentActivitySoft")

    val endString : String = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())

    builder
      .namedGraph(graphNamed)
      .subject(s":$soft")
      .add(RDF.TYPE, PROV.ACTIVITY)
      .add(PROV.USED,(":"+s"$database"))

    builder
      .namedGraph(graphNamed)
      .subject(currentActivitySoft)
      .add(RDF.TYPE, PROV.ACTIVITY)
      .add(PROV.WAS_DERIVED_FROM,s":$soft")
      .add(PROV.STARTED_AT_TIME,literal(startDate,XSD.DATETIME))
      .add(PROV.ENDED_AT_TIME,literal(endString,XSD.DATETIME))


    val config : WriterConfig = new WriterConfig()
   // config.set(BasicWriterSettings.PRETTY_PRINT, true)

    val stringWriter = new StringWriter()
    Rio.write(builder.build(), stringWriter, RDFFormat.JSONLD, config)

    stringWriter.toString()
  }
}
