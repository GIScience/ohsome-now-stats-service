# ohsome-contributions-stats-service

## Purpose

* REST service providing endpoints for OSM contribution statistics


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

### Running the service

The service can be run directly on the OS or within a docker image

### Running Locally

#### Secret management

For local execution, the application requires a (git-ignored) properties file here: `/src/main/resources/secrets.properties`.

It contains the database password in Java property-file format:

```properties
spring.datasource.password=<clickhouse db password here>
```

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

In order to run the service within a Docker environment, run the following commands:

```shell

./gradlew build

docker build -t heigit/hot-api .
docker run -p 8080:8080 heigit/hot-api

```

This relies on the  [Dockerfile](./Dockerfile) in this repository.

**Note:** The database credentials will be injected into the dockerized app via environment variables (soon). 
















