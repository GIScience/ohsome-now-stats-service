import net.researchgate.release.ReleaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.spring") version "1.8.10"

    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("io.gatling.gradle") version "3.9.5.1"


    // manages releases, i.e. maven version number and git tags (not artifact publication)
    id("net.researchgate.release") version "3.0.2"

    // manages publication of snapshot and release artifacts to respective maven repos (not release management)
    `maven-publish`

}

group = "org.heigit.ohsome.now.stats"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.clickhouse:clickhouse-jdbc:0.4.1:all")
    implementation("org.jdbi:jdbi3-core:3.37.1")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.18.0")
    testImplementation("org.testcontainers:clickhouse:1.18.0")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


// only build the 'fat' jar
tasks.named<Jar>("jar") {
    enabled = false
}


publishing {
    publications {
        create<MavenPublication>("bootJava") {
            artifact(tasks.named("bootJar"))
        }
    }

    repositories {
        maven {
            name = "artifactory-maven"

            val releasesRepoUrl = "https://repo.heigit.org/artifactory/libs-release-local"
            val snapshotsRepoUrl = "https://repo.heigit.org/artifactory/libs-snapshot-local"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)


            //credentials from personal global gradle properties (`.gradle/gradle.properties`) or from environment variables
            credentials {
                username = project.findProperty("artifactory.username")
                    ?.toString()
                    ?: System.getenv("ARTIFACTORY_USERNAME")

                password = project.findProperty("artifactory.password")
                    ?.toString()
                    ?: System.getenv("ARTIFACTORY_PASSWORD")

            }
        }
    }
}


configure<ReleaseExtension> {

//    failOnUnversionedFiles.set(false)
//    failOnUpdateNeeded.set(false)
//    failOnCommitNeeded.set(false)
//    failOnPublishNeeded.set(false)

}


kover {

    htmlReport {
        onCheck.set(true)
    }

    verify {
        onCheck.set(true)
        rule {
            isEnabled = true
            bound {
                minValue = 80
            }
        }
    }

}


