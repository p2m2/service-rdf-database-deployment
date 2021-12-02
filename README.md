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
  - remote: 'https://raw.githubusercontent.com/p2m2/service-rdf-database-deployment/1.0.6/msd-deploy.yml'

fetch_info_database:
  stage: version
  tags: [bash]
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
sbt run "--soft <string> --start-date <string> --category [category:metabohub/ext] --database [database] --release <string> --askomics-abstraction <file> --output <script.bash> <file1,file2,...>"
```

```sh
sbt "run --soft mtbls-metadata-reuse-in-agronomy --start-date $(date '+%Y-%m-%dT%T')  --category metabohub --database metabolights --release test --output test.sh --askomics-abstraction test.ttl ./something/test.rdf"
```

```sh
sbt "run --soft mtbls-metadata-reuse-in-agronomy --start-date $(date '+%Y-%m-%dT%T')  --category metabohub --database metabolights --release test --output test.sh --askomics-abstraction http://test*.ttl http://something/test.rdf.gz"
```
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
sbt run "--soft mtbls-metadata-reuse-in-agronomy --start-date 2021-07-14T01:01:01Z  --category metabohub --database metabolights --release test --output test.sh ./something/test.rdf"
```