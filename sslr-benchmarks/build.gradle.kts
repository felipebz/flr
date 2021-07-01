dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.15")
    implementation(project(":sslr-core"))
    testImplementation("org.easytesting:fest-assert:1.4")
}

description = "SonarSource :: Language Recognizer :: Benchmarks"
