plugins {
    kotlin("jvm") version "1.9.22"
}

allprojects {
    group = "dev.box"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin {
        jvmToolchain(17)
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    }

    tasks.test {
        useJUnitPlatform()
    }

    java {
        withSourcesJar()
    }
}
