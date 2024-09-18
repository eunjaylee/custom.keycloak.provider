plugins {
    java
    `maven-publish`
}

extra["keycloakVersion"] = "25.0.2"

val keycloakVersion: String by extra

allprojects {
    group = "kyobobook.keycloak"
    version = "0.0.2-SNAPSHOT"
    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }
    }
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.keycloak:keycloak-core:${keycloakVersion}")
}

tasks.withType<Jar> {
    enabled = false
}