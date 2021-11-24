# service-rdf-database-deployment

RDF Konwoledge Graph deployment on the HDFS storage

- RDF files is deploy according the following rules:
  - output directory : rdf/{category}/{database}/${release}

- manage Askomics Abstraction 
  - mode 1: abstraction is provided by the user
  - ~~mode 2: abstraction is generated from the turtle file~~
  - output file : rdf/askomics/{category}-{database}-${release}-askomics.<format>

- Provenance Generation
  - output directory : rdf/prov/{category}-{database}-{release}-prov.ttl
  
## command

sbt run "--soft <string> --start-date <string> --category [category:metabohub/ext] --database [database] --release <string> --askomics-abstraction <file> --output <script.bash> <file1,file2,...>"

## options

- soft : software in charge of generation
- files : lists of RDF files
- askomics-abstraction askomics abstraction
- release : name of the release
- category  <string> ( ext, metabohub )
- database : <string>
- output : <pathString> script to deploy

## example

```bash
sbt run "--soft mtbls-metadata-reuse-in-agronomy --start-date 2021-07-14T01:01:01Z  --category metabohub --database metabolights --release test --output test.sh ./something/test.rdf
```