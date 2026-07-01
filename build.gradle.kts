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

abstract class GenerateHonorMCBaslatBat : DefaultTask() {
    @get:Input
    abstract val minecraftVersion: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val version = minecraftVersion.get().trim()
        val batFile = outputFile.get().asFile
        batFile.parentFile.mkdirs()
        val content = """
            @echo off
            chcp 65001 >nul
            setlocal

            cd /d "%~dp0"

            if exist "%~dp0baslatici\HonorMC-Baslatici.jar" (
              java -jar "%~dp0baslatici\HonorMC-Baslatici.jar" --ayar "%~dp0ayarlar\baslatici.properties" %*
              set "HONOR_EXIT=%ERRORLEVEL%"
              pause
              exit /b %HONOR_EXIT%
            )

            set "HONOR_JAR=%~dp0Honor-$version.jar"
            if not exist "%HONOR_JAR%" (
              echo HonorMC cekirdek jar dosyasi bulunamadi: %HONOR_JAR%
              echo Bu bat dosyasini Honor-$version.jar ile ayni klasore koyup tekrar deneyin.
              pause
              exit /b 1
            )

            if "%HONORMC_MIN_RAM%"=="" set "HONORMC_MIN_RAM=8G"
            if "%HONORMC_MAX_RAM%"=="" set "HONORMC_MAX_RAM=16G"

            if not exist "ayarlar" mkdir "ayarlar"
            if not exist "ayarlar\oyuncular" mkdir "ayarlar\oyuncular"
            if not exist "ayarlar\paper" mkdir "ayarlar\paper"
            if not exist "eklentiler" mkdir "eklentiler"
            if not exist "dunyalar" mkdir "dunyalar"
            if not exist "kayitlar" mkdir "kayitlar"
            if not exist "kayitlar\html" mkdir "kayitlar\html"
            if not exist "yedekler" mkdir "yedekler"
            if not exist "altyapi" mkdir "altyapi"
            if not exist "altyapi\bundler" mkdir "altyapi\bundler"
            if not exist "ayarlar\eula.txt" (
              echo # Mojang EULA kabul dosyasi.> "ayarlar\eula.txt"
              echo eula=true>> "ayarlar\eula.txt"
            )

            echo HonorMC baslatiliyor...
            echo RAM: %HONORMC_MIN_RAM% - %HONORMC_MAX_RAM%
            java ^
              --enable-native-access=ALL-UNNAMED ^
              --illegal-native-access=allow ^
              --sun-misc-unsafe-memory-access=allow ^
              -Xms%HONORMC_MIN_RAM% ^
              -Xmx%HONORMC_MAX_RAM% ^
              -Dfile.encoding=UTF-8 ^
              -Duser.language=tr ^
              -Duser.country=TR ^
              -DbundlerRepoDir=altyapi\bundler ^
              -jar "%HONOR_JAR%" ^
              --nogui ^
              --config "ayarlar\sunucu.properties" ^
              --plugins "eklentiler" ^
              --world-dir "dunyalar" ^
              --world "ana-dunya" ^
              --bukkit-settings "ayarlar\bukkit-uyumluluk.yml" ^
              --spigot-settings "ayarlar\spigot-uyumluluk.yml" ^
              --paper-settings-directory "ayarlar\paper" ^
              --paper-settings "ayarlar\paper-eski-uyumluluk.yml" ^
              --purpur-settings "ayarlar\purpur-uyumluluk.yml" ^
              --commands-settings "ayarlar\komutlar.yml"
            set "HONOR_EXIT=%ERRORLEVEL%"
            pause
            exit /b %HONOR_EXIT%
        """.trimIndent().replace("\n", "\r\n") + "\r\n"
        batFile.writeText(content, Charsets.UTF_8)
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

val honorMCBaslatBat = layout.buildDirectory.file(
    providers.gradleProperty("mcVersion").map { "honormc/Honor-$it-baslat.bat" }
)

val hazirlaHonorMCBaslatBat = tasks.register<GenerateHonorMCBaslatBat>("hazirlaHonorMCBaslatBat") {
    group = "honormc"
    description = "HonorMC icin surume ozel Windows baslatma dosyasini hazirlar."

    minecraftVersion.set(providers.gradleProperty("mcVersion"))
    outputFile.set(honorMCBaslatBat)
}

tasks.register<Sync>("paketleHonorMCDagitim") {
    group = "honormc"
    description = "HonorMC icin temiz Turkce dagitim klasorunu hazirlar."

    dependsOn(packageHonorMCJar)
    dependsOn(hazirlaHonorMCBaslatBat)
    dependsOn(":honormc-baslatici:jar")

    from(layout.projectDirectory.dir("honormc-dagitim")) {
        exclude(
            "cekirdek/Honor-*.jar",
            "libraries/**",
            "versions/**",
            "altyapi/bundler/**",
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

val zipHonorMCDagitim = tasks.register<Zip>("zipHonorMCDagitim") {
    group = "honormc"
    description = "HonorMC Turkce dagitim klasorunu zip olarak paketler."

    dependsOn("paketleHonorMCDagitim")

    archiveFileName.set(providers.gradleProperty("mcVersion").map { "Honor-$it-dagitim.zip" })
    destinationDirectory.set(layout.buildDirectory.dir("honormc"))
    from(layout.buildDirectory.dir("honormc-dagitim"))
}

val honorMCSonSurumCiktiYolu = providers.gradleProperty("honorMCOutputDir")
    .orElse(providers.environmentVariable("HONORMC_OUTPUT_DIR"))
    .orElse(providers.systemProperty("user.home").map { "$it/Documents/HonorMC" })
val honorMCSonSurumCiktiKlasoru = layout.dir(honorMCSonSurumCiktiYolu.map { file(it) })

val kopyalaHonorMCSonSurum = tasks.register<Copy>("kopyalaHonorMCSonSurum") {
    group = "honormc"
    description = "Son HonorMC jar, baslatici ve dagitim zip ciktisini Belgeler/HonorMC klasorune kopyalar."
    doNotTrackState("Belgeler/HonorMC aktif dunya session.lock dosyalari icerebilir.")

    dependsOn(packageHonorMCJar)
    dependsOn(hazirlaHonorMCBaslatBat)
    dependsOn(":honormc-baslatici:jar")
    dependsOn("paketleHonorMCDagitim")
    dependsOn(zipHonorMCDagitim)

    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE

    from(layout.buildDirectory.dir("honormc-dagitim"))
    from(packageHonorMCJar.flatMap { it.outputJar })
    from(honorMCBaslatBat)
    from(zipHonorMCDagitim.flatMap { it.archiveFile })
    from(packageHonorMCJar.flatMap { it.outputJar }) {
        into("cekirdek")
    }
    from(project(":honormc-baslatici").tasks.named<Jar>("jar").flatMap { it.archiveFile }) {
        into("baslatici")
        rename { "HonorMC-Baslatici.jar" }
    }
    from(files(
        "honormc-dagitim/HONORMC-BASLATICI.bat",
        "honormc-dagitim/HONORMC-BASLATICI.ps1",
        "honormc-dagitim/BENI-OKU.md"
    ))
    from(layout.projectDirectory.dir("honormc-dagitim/ayarlar")) {
        include("baslatici.properties", "komutlar.yml", "honormc.yml")
        into("ayarlar")
    }
    into(honorMCSonSurumCiktiKlasoru)
}

zipHonorMCDagitim.configure {
    finalizedBy(kopyalaHonorMCSonSurum)
}
