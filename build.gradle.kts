import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

plugins {
    java // TODO java launcher tasks
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.21"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

paperweight {
    upstreams.paper {
        ref = providers.gradleProperty("paperCommit")

        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("purpur-server/build.gradle.kts")
            patchFile = file("purpur-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "paper-api/build.gradle.kts"
            outputFile = file("purpur-api/build.gradle.kts")
            patchFile = file("purpur-api/build.gradle.kts.patch")
        }
        patchDir("paperApi") {
            upstreamPath = "paper-api"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("purpur-api/paper-patches")
            outputDir = file("paper-api")
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
        options.isFork = true
        options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven("https://repo.purpurmc.org/snapshots") {
                name = "purpur"
                credentials(PasswordCredentials::class)
            }
        }
    }
}

tasks.matching { it.name == "applyAllPatches" }.configureEach {
    dependsOn(
        ":honormc-baslatici:processResources",
        ":honormc-baslatici:compileJava",
        ":honormc-baslatici:jar"
    )
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printPurpurVersion") {
    doLast {
        println(project.version)
    }
}

abstract class PrintHonorMCVersion : DefaultTask() {
    @get:Input
    abstract val honorMCVersion: Property<String>

    @get:Input
    abstract val minecraftVersion: Property<String>

    @get:Input
    abstract val upstreamProject: Property<String>

    @get:Input
    abstract val upstreamVersion: Property<String>

    @TaskAction
    fun printVersion() {
        println("HonorMC ${honorMCVersion.get()} / Minecraft ${minecraftVersion.get()} / upstream ${upstreamProject.get()} ${upstreamVersion.get()}")
    }
}

tasks.register<PrintHonorMCVersion>("printHonorMCVersion") {
    honorMCVersion.set(providers.gradleProperty("honorMCVersion"))
    minecraftVersion.set(providers.gradleProperty("mcVersion"))
    upstreamProject.set(providers.gradleProperty("honorMCUpstreamProject"))
    upstreamVersion.set(providers.gradleProperty("honorMCUpstreamVersion"))
}

abstract class PackageHonorMCJar : DefaultTask() {
    @get:InputFiles
    abstract val inputJars: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputJar: RegularFileProperty

    @TaskAction
    fun packageJar() {
        val sourceJar = inputJars.files.singleOrNull { it.name.contains("bundler") && it.extension == "jar" }
            ?: throw GradleException("Purpur bundler jar bulunamadi.")
        val targetJar = outputJar.get().asFile

        targetJar.parentFile.mkdirs()
        Files.copy(sourceJar.toPath(), targetJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
        patchStartupLine(targetJar)
    }

    private fun patchStartupLine(jarFile: File) {
        val paperclipClass = "io/papermc/paperclip/Paperclip.class"
        val needle = "Starting \u0001".toByteArray(Charsets.UTF_8)
        val replacement = "HonorMC: \u0001".toByteArray(Charsets.UTF_8)
        require(needle.size == replacement.size) { "Paperclip bootstrap replacement must keep the class constant length." }

        val temporaryJar = jarFile.resolveSibling("${jarFile.name}.tmp")
        var patched = false

        ZipInputStream(jarFile.inputStream().buffered()).use { input ->
            ZipOutputStream(temporaryJar.outputStream().buffered()).use { output ->
                generateSequence { input.nextEntry }.forEach { entry ->
                    val newEntry = ZipEntry(entry.name)
                    newEntry.time = entry.time
                    newEntry.comment = entry.comment
                    newEntry.extra = entry.extra
                    output.putNextEntry(newEntry)

                    if (!entry.isDirectory && entry.name == paperclipClass) {
                        val bytes = input.readBytes()
                        val offset = bytes.indexOfBytes(needle)
                        if (offset >= 0) {
                            replacement.copyInto(bytes, offset)
                            patched = true
                        }
                        output.write(bytes)
                    } else {
                        input.copyTo(output)
                    }

                    output.closeEntry()
                    input.closeEntry()
                }
            }
        }

        if (!patched) {
            temporaryJar.delete()
            throw GradleException("HonorMC bootstrap metni jar icinde degistirilemedi: $paperclipClass")
        }

        Files.move(temporaryJar.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    private fun ByteArray.indexOfBytes(needle: ByteArray): Int {
        if (needle.isEmpty() || needle.size > this.size) {
            return -1
        }

        outer@ for (offset in 0..(this.size - needle.size)) {
            for (index in needle.indices) {
                if (this[offset + index] != needle[index]) {
                    continue@outer
                }
            }
            return offset
        }

        return -1
    }
}

val packageHonorMCJar = tasks.register<PackageHonorMCJar>("packageHonorMCJar") {
    group = "honormc"
    description = "Builds the runnable server jar and copies it as Honor-<mcVersion>.jar."

    dependsOn(":purpur-server:createBundlerJar")

    inputJars.from(project(":purpur-server").layout.buildDirectory.dir("libs").map {
        it.asFileTree.matching {
            include("*bundler*.jar")
        }
    })
    outputJar.set(layout.buildDirectory.file(providers.gradleProperty("mcVersion").map { "honormc/Honor-$it.jar" }))
}

tasks.register<Sync>("paketleHonorMCDagitim") {
    group = "honormc"
    description = "HonorMC icin temiz Turkce dagitim klasorunu hazirlar."

    dependsOn(packageHonorMCJar)
    dependsOn(":honormc-baslatici:jar")

    from(layout.projectDirectory.dir("honormc-dagitim")) {
        exclude(
            "cekirdek/Honor-*.jar",
            "libraries/**",
            "versions/**",
            "logs/**",
            "crash-reports/**",
            "banned-ips.json",
            "banned-players.json",
            "ops.json",
            "whitelist.json",
            "version_history.json",
            "help.yml",
            "dunyalar/ana-dunya/**",
            "dunyalar/usercache.json",
            "eklentiler/bStats/**",
            "eklentiler/spark/**",
            "ayarlar/paper/legacy-backup/**",
            "ayarlar/paper/paper-global.yml",
            "ayarlar/paper/paper-world-defaults.yml",
            "ayarlar/paper-eski-uyumluluk.yml-README.txt",
        )
    }
    from(packageHonorMCJar.flatMap { it.outputJar }) {
        into("cekirdek")
    }
    from(project(":honormc-baslatici").tasks.named<Jar>("jar").flatMap { it.archiveFile }) {
        into("baslatici")
        rename { "HonorMC-Baslatici.jar" }
    }
    into(layout.buildDirectory.dir("honormc-dagitim"))
}

tasks.register("packageHonorMCDistribution") {
    group = "honormc"
    description = "Alias for paketleHonorMCDagitim."
    dependsOn("paketleHonorMCDagitim")
}

tasks.register<Zip>("zipHonorMCDagitim") {
    group = "honormc"
    description = "HonorMC Turkce dagitim klasorunu zip olarak paketler."

    dependsOn("paketleHonorMCDagitim")

    archiveFileName.set(providers.gradleProperty("mcVersion").map { "Honor-$it-dagitim.zip" })
    destinationDirectory.set(layout.buildDirectory.dir("honormc"))
    from(layout.buildDirectory.dir("honormc-dagitim"))
}
