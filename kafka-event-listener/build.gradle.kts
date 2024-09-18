import java.util.*

plugins {
    id("java")
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val keycloakVersion: String by rootProject.extra


dependencies {
    compileOnly("org.keycloak:keycloak-services:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-server-spi:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-server-spi-private:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-quarkus-server:${keycloakVersion}")
//    compileOnly("org.apache.kafka:kafka-clients:3.4.1")

    compileOnly(files("../lib/kafka-clients-3.4.1.jar"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    destinationDirectory.set(File("../build/libs"))
}