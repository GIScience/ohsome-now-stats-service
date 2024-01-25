import net.researchgate.release.ReleaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI


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

    id("org.sonarqube") version "4.3.1.3277"
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
    implementation("org.springframework.boot:spring-boot-starter-cache")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.clickhouse:clickhouse-jdbc:0.4.6:all")
    implementation("org.jdbi:jdbi3-core:3.37.1")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
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
            name = "heigitNexus"
            url = URI(project.findProperty("repositoryUrl").toString())

            credentials(PasswordCredentials::class)
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


