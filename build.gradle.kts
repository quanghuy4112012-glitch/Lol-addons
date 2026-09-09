plugins {
    id("fabric-loom") version "1.10.0"
    `maven-publish`
    java
}

version = project.property("mod_version").toString()
group = project.property("maven_group").toString()

base {
    archivesName.set(project.property("archives_base_name").toString())
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.meteorclient.com/releases")
    maven("https://maven.meteorclient.com/snapshots")
}

dependencies {
    // Fabric & Minecraft
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    // Meteor Client
    modImplementation("meteordevelopment:meteor-client:${project.property("meteor_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
