package dev.box.samples

import dev.box.reader.BoxReaderImpl
import dev.box.writer.BoxWriterImpl
import java.io.File

fun main() {
    println("=== Mera BOX Banane Wala App ===\n")

    // 1. BOX banao
    val writer = BoxWriterImpl()
    val result = writer.create("mera_box.box")
    if (result.isFailure()) {
        println("Error: ${result.error().message}")
        return
    }
    val box = result.value()

    // Metadata dalo
    box.setMetadata(4, "Rishav")        // Author
    box.setMetadata(5, "Mera pehla BOX") // Description
    box.setMetadata(9, "MyBox App")      // Tool Name
    box.setMetadata(10, "1.0")           // Tool Version

    // Folder banao
    box.addFolder("/photos")
    box.addFolder("/documents")
    box.addFolder("/documents/notes")

    // Files dalo
    box.addFile("/hello.txt", "Hello! Ye mera pehla BOX hai!".toByteArray())
    box.addFile("/documents/readme.md", "# Mera Project\nYe ek BOX file hai.".toByteArray())
    box.addFile("/documents/notes/today.txt", "Aaj bahut accha din hai!".toByteArray())
    box.addFile("/data.json", """{"name": "Rishav", "age": 25}""".toByteArray())

    // Save karo
    val saveResult = box.save()
    if (saveResult.isFailure()) {
        println("Save error: ${saveResult.error().message}")
        return
    }
    box.close()
    println("mera_box.box ban gaya! (${File("mera_box.box").length()} bytes)\n")

    // 2. BOX padho
    println("--- BOX khol ke dekho ---\n")
    val reader = BoxReaderImpl()
    val readResult = reader.open("mera_box.box")
    if (readResult.isFailure()) {
        println("Error: ${readResult.error().message}")
        return
    }
    val readBox = readResult.value()

    println("Files: ${readBox.fileCount}")
    println("Folders: ${readBox.folderCount}")
    println()

    // Metadata dikhao
    println("Metadata:")
    for ((key, value) in readBox.metadata()) {
        println("  $key = $value")
    }
    println()

    // Sab entries dikhao
    println("Sab files/folders:")
    for (entry in readBox.entries()) {
        println("  ${entry.path}")
    }
    println()

    // Extract karo
    readBox.extractAll("mera_box_output/")
    readBox.close()
    println("Sab kuch 'mera_box_output/' folder mein extract ho gaya!")
}
