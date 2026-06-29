plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":box-core"))
    implementation(project(":box-api"))
    implementation(project(":box-reader"))
    implementation(project(":box-writer"))
    implementation(project(":box-validator"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
