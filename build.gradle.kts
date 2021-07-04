import java.util.Calendar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.hierynomus.gradle.license.tasks.LicenseFormat
import com.hierynomus.gradle.license.tasks.LicenseCheck

plugins {
    `java-library`
    `maven-publish`
    jacoco
    kotlin("jvm") version "1.5.20"
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
    }

    group = "org.sonarsource.sslr"
    version = "1.24-SNAPSHOT"
}

subprojects {
    apply(plugin = "com.github.hierynomus.license")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        testImplementation("junit:junit:4.13.1")
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            this.jvmTarget = "1.8"
        }
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

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    description.set(project.description)
                    organization {
                        name.set("SonarSource")
                        url.set("http://www.sonarsource.com")
                    }
                    licenses {
                        license {
                            name.set("GNU LGPL 3")
                            url.set("http://www.gnu.org/licenses/lgpl.txt")
                            distribution.set("repo")
                        }
                    }
                    scm {
                        url.set("https://github.com/SonarSource/sslr")
                    }
                    developers {
                        developer {
                            id.set("dbolkensteyn")
                            name.set("Dinesh Bolkensteyn")
                            email.set("dinesh.bolkensteyn@sonarsource.com")
                            organization.set("SonarSource")
                        }
                        developer {
                            id.set("godin")
                            name.set("Evgeny Mandrikov")
                            email.set("evgeny.mandrikov@sonarsource.com")
                            organization.set("SonarSource")
                        }
                    }
                }
            }
        }
    }
}
