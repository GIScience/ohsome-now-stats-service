# ohsome-contributions-stats-service

## Purpose

* REST service providing endpoints for OSM contribution statistics

## Technology

* JVM: Java 17
* Base framework: Spring Boot 6
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

## Running Locally

The service does not have a deployment yet,
but can be run locally on every machine with a Java Runtime (JDK 17 or higher.)

To do so run the following start script (on *nix systems):

    ./gradlew bootRun   

or (on Windows systems):

    ./gradlew.bat bootRun   

Once the system has started,
the API documentation including links to endpoints is available here:

http://localhost:8080/doc.html











