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
        this.extra.set("year", Calendar.getInstance().get(Calendar.YEAR))
        this.header = File(this.project.rootDir, "LICENSE_HEADER")
        this.include("**/*.kt")
    }

    tasks.withType<LicenseCheck>().configureEach {
        this.extra.set("year", Calendar.getInstance().get(Calendar.YEAR))
        this.header = File(this.project.rootDir, "LICENSE_HEADER")
        this.include("**/*.kt")
    }
}
