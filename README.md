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
  
=> template [gitlab-ci](./msd-deploy.yml)

## MSD template

### .gitlab-ci template

```yaml
include:
  - remote: 'https://raw.githubusercontent.com/p2m2/service-rdf-database-deployment/lastest/msd-deploy.yml'

fetch_info_database:
  stage: version
  tags: [bash]
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH
    - if: $CI_COMMIT_TAG
  only:
    - tags
    - main
  script:
    - echo "CATEGORY=$CATEGORY" >> build.env
    - echo "DATABASE=$DATABASE" >> build.env
    - echo "RDF_INPUT_FILES=$RDF_INPUT_FILES" >> build.env
    - echo "VERSION=$VERSION" >> build.env
  artifacts:
    reports:
      dotenv: build.env
```
### Variables to defined bu the main YAML gitlab file

- CATEGORY: category of the database
- DATABASE: database name
- RDF_INPUT_FILES: local or remote files (http/ftp). possibility to use wildcard
- RDF_ASKOMICS_INPUT_FILES: a single file

### Prerequisite GITLAB Variables

- SSH_HOST
- SSH_USER
- SSH_PATH
- SSH_OPTS = " -q -t "
- SSH_PRIVATE_KEY 
- PRIVATE_TOKEN (user token must be defined)



#### SSH_PRIVATE_KEY

`cat my_private_key | base64 -w0`

#### PRIVATE_TOKEN

- go to http://<GITLAB_URL>/-/profile/personal_access_tokens to defined an access token
- 

### Wariable which can be overloaded

- SSH_HOST_WORK_DIR (default "/uploads/CI")


## command
```sh
sbt "run --ci-project-url ${CI_PROJECT_URL} \
         --ci-pipeline-url ${CI_PIPELINE_URL} \
         --url-release  ${CI_PROJECT_URL}/tags/${VERSION} \
         --start-date $(date '+%Y-%m-%dT%T') \
         --category ${CATEGORY} \
         --database ${DATABASE} \
         --release ${CI_COMMIT_REF_NAME} \ 
         --output ${OUTPUT_SCRIPT_FILE} \
         --provjsonld ${PROV_FILE_NAME} \
         --askomics-abstraction ${RDF_ASKOMICS_INPUT_FILE} \
         ${RDF_INPUT_FILES}"
```

### options

- `ci-project-url`       : URL of gitlab project
- `ci-pipeline-url`      : lists of RDF files
- `url-release`          : future gitlab tag to create
- `start-date`           : current date
- `askomics-abstraction` : askomics abstraction
- `release`              : release name
- `category`             : database provider
- `database`             : database name
- `provjsonld`           : prov/dcat information about database generation on the MSD cluster
- `output`               : script to deploy database on the MSD Haddop cluster

### generation example

```sh
export CI_PROJECT_URL="https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment"
export CI_PIPELINE_URL="https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment/-/pipelines/31503" 
export CI_PROJECT_URL="https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment"
export CATEGORY="internal"
export VERSION="test-version"
export DATABASE="database-test"
export CI_COMMIT_REF_NAME="test-version"
export OUTPUT_SCRIPT_FILE="out.sh"
export PROV_FILE_NAME="$CATEGORY-$DATABASE-$VERSION.ttl"
export RDF_ASKOMICS_INPUT_FILE="http://metabolomics-datalake.ara.inrae.fr/askomics/database-test/abstraction.ttl"
export RDF_INPUT_FILES="http://metabolomics-datalake.ara.inrae.fr/database-test/*.ttl"
```

#### 'out.sh'

```bash
#!/bin/bash
/usr/local/hadoop/bin/hdfs dfs -mkdir -p /rdf/internal/database-test/test-version
/usr/local/hadoop/bin/hdfs dfs -mkdir -p /rdf/askomics/
/usr/local/hadoop/bin/hdfs dfs -mkdir -p /rdf/prov/
FILES=$(wget --spider -nv -r -nd --no-parent -e robots=off -A "$(basename http://metabolomics-datalake.ara.inrae.fr/database-test/*.ttl)" $(dirname http://metabolomics-datalake.ara.inrae.fr/database-test/*.ttl)/ 2>&1 | egrep "200[[:blank:]]+OK$" | awk '{print $4}')
for file in $FILES
do
if [ "${file: -3}" == ".gz" ]; then
wget -q -O - $file | gunzip -c | /usr/local/hadoop/bin/hdfs dfs -put - /rdf/internal/database-test/test-version/$(basename ${file%.gz})
else
wget -q -O - $file | /usr/local/hadoop/bin/hdfs dfs -put - /rdf/internal/database-test/test-version/$(basename $file)
fi
done
/usr/local/hadoop/bin/hdfs dfs -put -f $(basename \) /rdf/internal/database-test/test-version
wget http://metabolomics-datalake.ara.inrae.fr/askomics/database-test/abstraction.ttl
/usr/local/hadoop/bin/hdfs dfs -put -f $(basename http://metabolomics-datalake.ara.inrae.fr/askomics/database-test/abstraction.ttl) /rdf/askomics//$(basename http://metabolomics-datalake.ara.inrae.fr/askomics/database-test/abstraction.ttl)
cat << EOF > $PWD/internal-database-test-test-version.ttl
@prefix : <http://www.metabohub.fr/msd#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xsd: <http://www.w3.org/2000/10/XMLSchema#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix prov: <http://www.w3.org/ns/prov#> .

<https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment>
  a prov:Entity, <http://www.w3.org/ns/dcat#Dataset>;
  <http://purl.org/dc/terms/title> "database-test";
  <http://purl.org/dc/terms/description> "Category internal / Database database-test";
  <http://purl.org/dc/terms/modified> "2021-12-23T09:22:07"^^<http://www.w3.org/2001/XMLSchema#dateTime>;
  prov:wasGeneratedBy "https://github.com/p2m2/service-rdf-database-deployment/";
  <http://www.w3.org/ns/dcat#Distribution> "https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment/tags/test-version" .

<https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment/tags/test-version>
  a prov:Entity, <http://www.w3.org/ns/dcat#Distribution>;
  <http://purl.org/dc/terms/title> "test-version";
  <http://purl.org/dc/terms/modified> "2021-12-23T09:22:07"^^<http://www.w3.org/2001/XMLSchema#dateTime>;
  prov:wasGeneratedBy "https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment/-/pipelines/31503";
  <http://www.w3.org/ns/dcat#accessURL> "hdfs://rdf/internal/database-test/test-version" .

<https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment/-/pipelines/31503>
  a prov:Activity;
  prov:used "https://services.pfem.clermont.inrae.fr/gitlab/metabosemdatalake/databases/database-test-deployment";
  prov:startedAtTime "2021-12-23T09:22:07"^^<http://www.w3.org/2001/XMLSchema#dateTime>;
  prov:endedAtTime "2021-12-23T09:22:12"^^<http://www.w3.org/2001/XMLSchema#dateTime> .

EOF
/usr/local/hadoop/bin/hdfs dfs -put -f internal-database-test-test-version.ttl /rdf/prov/
```