plugins {
    id("com.android.library") version "8.5.2"
    id("org.jetbrains.kotlin.android") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

android {
    namespace = "com.aiia.plugin.sdk"
    compileSdk = 35
    defaultConfig { minSdk = 29 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

tasks.register<Zip>("packageAiip") {
    group = "aiia"
    description = "Package plugin.dex and manifest.json as an .aiip archive"
    dependsOn("assembleRelease")
    archiveFileName.set("example.aiip")
    destinationDirectory.set(layout.buildDirectory.dir("aiip"))
    from(layout.buildDirectory.dir("outputs/aar")) {
        include("*.aar")
    }
    doLast {
        val output = archiveFile.get().asFile
        output.parentFile.mkdirs()
        java.util.zip.ZipOutputStream(output.outputStream()).use { zip ->
            fun add(name: String, bytes: ByteArray) {
                zip.putNextEntry(java.util.zip.ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
            add("manifest.json", """{"id":"example","name":"Example","version":"1.0","entryClass":"com.aiia.plugin.example.ExamplePlugin","permissions":[],"apiVersion":1}""".toByteArray())
            val aar = fileTree(layout.buildDirectory.dir("outputs/aar")).matching { include("*.aar") }.singleFile
            val classes = java.util.zip.ZipInputStream(aar.inputStream()).use { input ->
                val out = java.io.ByteArrayOutputStream()
                var entry = input.nextEntry
                while (entry != null) {
                    if (entry.name == "classes.jar") input.copyTo(out)
                    entry = input.nextEntry
                }
                out.toByteArray()
            }
            add("plugin.dex", classes)
        }
    }
}
