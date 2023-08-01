import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.spring") version "1.8.10"

    id("io.gitlab.arturbosch.detekt").version("1.21.0")
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("io.gatling.gradle") version "3.9.5.1"
}

group = "org.heigit.ohsome.now.stats"
version = "0.0.3-SNAPSHOT"
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


