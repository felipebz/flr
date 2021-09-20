import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    jacoco
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.dokka") version ("1.4.32")
    id("com.github.hierynomus.license") version "0.16.1"
    id("org.sonarqube") version "3.3"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
    }

    java {
        withSourcesJar()
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    group = "com.felipebz.flr"
    version = "1.24-SNAPSHOT"
}

subprojects {
    apply(plugin = "com.github.hierynomus.license")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        testImplementation(platform(Libs.junit_bom))
        testImplementation(Libs.junit_jupiter)
    }

    kotlin {
        explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            this.jvmTarget = "1.8"
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    jacoco {
        toolVersion = "0.8.7"
    }

    tasks.jacocoTestReport {
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
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    publishing {
        repositories {
            maven {
                name = "AzureArtifacts"
                url = uri("https://pkgs.dev.azure.com/felipebz/z-plsql-analyzer/_packaging/public_feed/maven/v1")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("DEPLOY_USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("DEPLOY_TOKEN")
                }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                if (dokka.archiveFile.get().asFile.exists()) {
                    artifact(dokka)
                }

                pom {
                    description.set(project.description)
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

sonarqube {
    properties {
        property("sonar.projectName", "FLR")
    }
}
