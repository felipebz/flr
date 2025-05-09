import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    signing
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.license)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.jreleaser)
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

    group = "com.felipebz.flr"
    version = "1.6.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "com.github.hierynomus.license")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.dokka-javadoc")
    apply(plugin = "signing")

    kotlin {
        explicitApi()
        jvmToolchain(11)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
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
        toolVersion = "0.8.13"
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
        reports {
            xml.required.set(true)
        }
    }

    tasks.withType<LicenseFormat>().configureEach {
        extra.set("year", Calendar.getInstance().get(Calendar.YEAR))
        header = File(this.project.rootDir, "LICENSE_HEADER")
        include("**/*.kt")
        skipExistingHeaders = true
    }

    tasks.withType<LicenseCheck>().configureEach {
        extra.set("year", Calendar.getInstance().get(Calendar.YEAR))
        header = File(this.project.rootDir, "LICENSE_HEADER")
        include("**/*.kt")
        skipExistingHeaders = true
    }

    val dokka by tasks.register<Jar>("dokka") {
        dependsOn(tasks.dokkaGenerateModuleJavadoc)
        from(tasks.dokkaGenerateModuleJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    signing {
        setRequired({
            gradle.taskGraph.hasTask("publish")
        })
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }

    publishing {
        repositories {
            maven {
                val releaseRepo = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotRepo = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotRepo else releaseRepo
                credentials {
                    username = project.findProperty("ossrh.user") as String? ?: System.getenv("OSSRH_USERNAME")
                    password = project.findProperty("ossrh.password") as String? ?: System.getenv("OSSRH_PASSWORD")
                }
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
            overwrite.set(true)
            tagName.set("{{projectVersion}}")
            draft.set(true)
            changelog {
                formatted.set(org.jreleaser.model.Active.ALWAYS)
                preset.set("conventional-commits")
                format.set("- {{commitShortHash}} {{commitTitle}}")
                contributors {
                    enabled.set(false)
                }
                hide {
                    uncategorized.set(true)
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
