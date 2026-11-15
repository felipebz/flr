import com.diffplug.gradle.spotless.KotlinExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    `maven-publish`
    signing
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.jreleaser)
}

val publishedProjects = setOf(
    ":flr-core",
    ":flr-xpath",
    ":flr-toolkit",
    ":flr-testing-harness",
    ":flr-examples",
    ":flr-tests",
)

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    group = "com.felipebz.flr"
    version = providers.gradleProperty("version").get()
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.dokka-javadoc")
    apply(plugin = "signing")

    kotlin {
        explicitApi()
    }

    testing {
        suites {
            val test by getting(JvmTestSuite::class) {
                useJUnitJupiter(rootProject.libs.versions.junit)

                dependencies {
                    implementation(rootProject.libs.assertj)
                    implementation(rootProject.libs.mockito)
                    implementation(rootProject.libs.mockito.kotlin)
                }
            }
        }
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    jacoco {
        toolVersion = "0.8.16"
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
        reports {
            xml.required.set(true)
        }
    }

    configure<SpotlessExtension> {
        // The default (Git attributes) loads JGit, which is pinned to the version JReleaser needs.
        lineEndings = LineEnding.PRESERVE
        val lines = rootProject.file("LICENSE_HEADER").readLines().filter { it.isNotBlank() }
        fun header(vararg extra: String) = (extra.toList() + lines).joinToString("", prefix = "//\n", postfix = "//\n\n") { "// $it\n" }
        val sources = fileTree("src") { include("**/*.kt") }
        val sslrDerived = sources.filter { file ->
            file.useLines { text -> text.takeWhile { !it.startsWith("package ") }.any { "SonarSource SA" in it } }
        }
        kotlin {
            target(sources.minus(sslrDerived))
            licenseHeader(header())
        }
        format("kotlinDerivedFromSslr", KotlinExtension::class.java) {
            target(sslrDerived)
            licenseHeader(header("SPDX-FileCopyrightText: 2010-2021 SonarSource SA"))
        }
    }

    tasks.withType<Jar>().configureEach {
        into("META-INF") {
            from(rootProject.file("LICENSE"), rootProject.file("COPYING"))
        }
    }

    val dokka by tasks.register<Jar>("dokka") {
        dependsOn(tasks.dokkaGenerateModuleJavadoc)
        from(tasks.dokkaGenerateModuleJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    if (path in publishedProjects) {
        publishing {
            repositories {
                maven {
                    url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
                }
            }
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    artifact(dokka)
                    pom {
                        name.set(provider { project.description })
                        description.set(provider { project.description })
                        url.set("https://github.com/felipebz/flr")
                        organization {
                            name.set("Felipe Zorzo")
                            url.set("https://felipezorzo.com.br")
                        }
                        licenses {
                            license {
                                name.set("GNU LGPL 3")
                                url.set("https://www.gnu.org/licenses/lgpl.txt")
                                distribution.set("repo")
                            }
                        }
                        scm {
                            url.set("https://github.com/felipebz/flr")
                            connection.set("scm:git:https://github.com/felipebz/flr.git")
                            developerConnection.set("scm:git:https://github.com/felipebz/flr.git")
                        }
                        developers {
                            developer {
                                id.set("felipebz")
                                name.set("Felipe Zorzo")
                                url.set("https://felipezorzo.com.br")
                            }
                        }
                    }
                }
            }
        }
    }
}

jreleaser {
    project {
        description.set("FLR")
        authors.set(listOf("felipebz"))
        license.set("LGPL-3.0")
        links {
            homepage.set("https://github.com/felipebz/flr")
        }
        inceptionYear.set("2021")
        snapshot {
            fullChangelog.set(true)
        }
    }
    release {
        github {
            token.set("unused") // JReleaser requires a configured provider even for deploy tasks.
            skipTag.set(true)
            skipRelease.set(true)
            artifacts.set(false)
            files.set(false)
            checksums.set(false)
            catalogs.set(false)
            signatures.set(false)
            uploadAssets.set(org.jreleaser.model.Active.NEVER)
        }
    }
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    snapshotSupported.set(true)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("build/staging-deploy")
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active.set(org.jreleaser.model.Active.SNAPSHOT)
                    url.set("https://central.sonatype.com/repository/maven-snapshots")
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots")
                    applyMavenCentralRules.set(true)
                    snapshotSupported.set(true)
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectName", "FLR")
    }
}
