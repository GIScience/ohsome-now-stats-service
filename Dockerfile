FROM eclipse-temurin:17 AS buildstage

WORKDIR /tmp/build-ocs

# copy and cache gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
RUN ./gradlew --version

# copy source files and build
COPY config config
COPY hurl hurl
COPY src src
RUN ./gradlew --no-daemon clean assemble

FROM eclipse-temurin:17-alpine

COPY --from=buildstage /tmp/build-ocs/build/libs/*.jar app.jar
