plugins {
    java
    idea
    eclipse
    antlr
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

application {
    mainClassName = "poulet.Main"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.7.2")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testCompile("org.junit.jupiter:junit-jupiter-params:5.3.2")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}