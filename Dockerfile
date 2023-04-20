
# simple setup according to https://spring.io/guides/topicals/spring-boot-docker/
# could also be set up 'layered' to speed up build and startup times


# this base image was picked for no special reason except being compatible with Mac M1
# feel free to replace with better/smaller/safer variant
FROM eclipse-temurin:17.0.6_10-jre-focal
VOLUME /tmp

COPY build/libs/*.jar app.jar

# database credentials will soon be injected here via environment variables
ENTRYPOINT ["java","-jar","/app.jar"]



