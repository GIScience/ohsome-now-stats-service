import net.researchgate.release.ReleaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI
import dev.detekt.gradle.Detekt


plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"

    id("dev.detekt") version "2.0.0-alpha.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.2"
    id("io.gatling.gradle") version "3.13.5.4"

    // manages releases, i.e. maven version number and git tags (not artifact publication)
    id("net.researchgate.release") version "3.1.0"
    // shows which deps can be updated
    id("com.github.ben-manes.versions") version "0.53.0"
    // manages publication of snapshot and release artifacts to respective maven repos (not release management)
    `maven-publish`

    id("org.sonarqube") version "6.1.0.5360"
}

group = "org.heigit.ohsome.now.stats"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.clickhouse:clickhouse-jdbc:0.7.2")
    implementation("org.jdbi:jdbi3-core:3.49.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.3")
    testImplementation("org.testcontainers:testcontainers-clickhouse:2.0.3")
    testImplementation("org.mockito:mockito-core")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xemit-jvm-type-annotations"))
        jvmTarget.set(JvmTarget.JVM_21)
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}


// only build the 'fat' jar
tasks.named<Jar>("jar") {
    enabled = false
}

tasks {
    koverXmlReport {
        check(true)
    }
    koverHtmlReport {
        check(true)
    }
    koverVerify {
        kover {
            reports {
                verify {
                    rule {
                        // Minimal line coverage in %
                        minBound(80)
                    }
                }
            }
        }
    }
}

tasks.named("test") {
    finalizedBy(tasks.named("koverXmlReport"))
    finalizedBy(tasks.named("koverHtmlReport"))
}

tasks.withType(Detekt::class.java).configureEach {
    ignoreFailures = false
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

    failOnUnversionedFiles.set(false)
    failOnUpdateNeeded.set(false)
    failOnCommitNeeded.set(false)
    failOnPublishNeeded.set(false)

}


