plugins {
    alias(libs.plugins.jmh)
}

dependencies {
    val coreJar = providers.gradleProperty("flrBenchmarkCoreJar").orNull
    add("jmh", if (coreJar == null) project(":flr-core") else files(coreJar))
}

jmh {
    jmhVersion.set("1.37")
    profilers.add("gc")
    resultFormat.set("JSON")
}

description = "FLR :: Benchmarks"
