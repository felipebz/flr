dependencies {
    testImplementation(project(":flr-core"))
    testImplementation(project(":flr-xpath"))
    testImplementation(project(":flr-testing-harness"))
    testImplementation("commons-io:commons-io:2.4")
    testImplementation("org.easytesting:fest-assert:1.4")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("org.mockito:mockito-core:3.11.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
}

description = "FLR :: Tests"
