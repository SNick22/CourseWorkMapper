plugins {
    kotlin("jvm") version "2.1.0"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    ksp(project(":mapper_generator_processor"))
    implementation(project(":mapper_generator_annotations"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}