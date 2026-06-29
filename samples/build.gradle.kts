plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("dev.box.samples.DemoKt")
}

dependencies {
    implementation(project(":box-core"))
    implementation(project(":box-api"))
    implementation(project(":box-reader"))
    implementation(project(":box-writer"))
    implementation(project(":box-validator"))
}
