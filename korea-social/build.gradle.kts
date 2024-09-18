plugins {
    id("java")
}

group = "org.example"
version = "0.0.3-SNAPSHOT"
val keycloakVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.keycloak:keycloak-services:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-server-spi:${keycloakVersion}")
    compileOnly("org.keycloak:keycloak-server-spi-private:${keycloakVersion}")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    destinationDirectory.set(File("../build/libs"))
}