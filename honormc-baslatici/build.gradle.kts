plugins {
    application
}

application {
    mainClass.set("tr.honormc.baslatici.HonorMCBaslatici")
}

tasks.jar {
    archiveBaseName.set("HonorMC-Baslatici")
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get(),
            "Implementation-Title" to "HonorMC Baslatici",
            "Implementation-Vendor" to "HonorMC",
        )
    }
}
