dependencies {
    testImplementation(project(":sslr-core"))
    testImplementation(project(":sslr-xpath"))
    testImplementation(project(":sslr-testing-harness"))
    testImplementation("commons-io:commons-io:2.4")
    testImplementation("org.easytesting:fest-assert:1.4")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("org.mockito:mockito-core:3.11.1")
}

description = "SonarSource :: Language Recognizer :: Tests"
