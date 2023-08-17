# ohsome-now-stats-service
[![LICENSE](https://img.shields.io/github/license/GIScience/ohsome-now-stats-service)](LICENSE)

The **ohsomeNow stats API** offers a REST service to retrieve up-to-date and global scale overview statistics on mapping activity in OpenStreetMap (OSM).

The ohsomeNow stats API builds upon a ClickHouse DB which contains all OSM edits for which a changeset hashtag has been used. The REST service allows you to get insights into the number of contributors, total map edits, added buildings and added road length for a given time range and OSM changesets hashtag filter. You can use the REST service to report mapping statistics for any time range starting from 2009-04-21 when the OSM-API version 0.6 introduced changesets.

Check out the [API documentation]() get in contact with us in case you are planning to embed the API in your services or websites. 

The core features are:
* **Overview Statistics**: The `stats` endpoint provides mapping activity statistics summarized into a single line for a single or multiple OSM changeset hashtags.
* **Trending Hashtags**: Get a list of the `mostUsedHashtags` in your chosen time-interval. The list is sorted by the number of distinct OSM contributors per hashtag.
* **Country Stats**: Use the `/stats/{hashtag}/country` endpoint to get insights into contributors and edits per OSM changeset hashtags for all countries. 
* **Timeline**: Get insights about the dynamics in mapping activity over time with the `/stats/{hashtag}/interval` endpoint.

Features for HOT Tasking Manager statistics:
* **User Statistics**: Access to statistics for individual users is restricted to OSM contributors who are logged in the HOT Tasking Manager. These statistics only consider mapping activity for related changeset hashtags (filter: `hotosm-project-*`). 

For details about the ohsomeNow stats website check [GIScience/ohsome-now-stats-frontend](https://github.com/GIScience/ohsome-now-stats-frontend).

# For Developers
## Technology

* JVM: Java 17
* Base framework: Spring Boot 3
* Build tool: Gradle 7.6
* Implementation language: Kotlin 1.8
* Test Framework: JUnit Jupiter 5.9
* additional HTTP integration and performance tests: Hurl 1.8
* API documentation: OpenAPI / Swagger

## Code Quality Tools

* Metrics: Detekt
    * method length <= 17
    * cyclomatic complexity <= 4
* Code Coverage: Kover
    * test coverage > 80%

## Running the service

The service can be run directly on the OS or within a docker image

### Running Locally

#### Starting the service

The service can be run on every machine with a Java Runtime (JDK 17 or higher.)

To do so run the following start script (on *nix systems):

```shell
./gradlew bootRun   
```

or (on Windows systems):

```shell
./gradlew.bat bootRun   
```

Once the system has started,
the API documentation including links to endpoints is available here:

http://localhost:8080/doc.html

### Running within Docker

In order to run the service and a development database with dummy data within a Docker environment, run the following commands:

```shell
docker compose up -d
```

If you want to only run a development database in Docker and run the API natively (i.e. with gradle), run the following commands:
```shell
# start dev database
docker compose up -d clickhouse-database
# run API using the gradle wrapper
./gradlew bootRun
```

This relies on the  [Dockerfile](./Dockerfile),  in this repository.

**Note:** The database credentials will be injected into the dockerized app via environment variables (soon). The Docker database setup currently relies on an empty password set in `secrets.properties` (see above).

## Release and artifact publication

These steps are achieved by the following 2 plugins:

* release plugin: https://plugins.gradle.org/plugin/net.researchgate.release
* maven-publish plugin: https://docs.gradle.org/current/userguide/publishing_maven.html

### Release plugin

This plugin is used to:

* check for a clean workspace and assure that the local and remote git repos are in sync
* read current (snapshot) version from [./gradle.properties](./gradle.properties)
* create a git tag for this version (by removing the `-SNAPSHOT` postfix)
* set the next (snapshot) version in [./gradle.properties](./gradle.properties)

The plugin is run locally via the command line:

`./gradlew release`

The version scheme defaults can be overridden interactively if neccessary.

### Maven-publish plugin

This plugin publishes all *release* and *snapshot* artifacts to the respective Artifactory maven repos.

Please note:

* This plugin is not intended to be started locally, but runs in the CI server. 
























