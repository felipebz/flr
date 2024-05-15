dependencies {
    testImplementation(project(":flr-core"))
    testImplementation(project(":flr-xpath"))
    testImplementation(project(":flr-testing-harness"))
    testImplementation(Libs.hamcrest)
    testImplementation(Libs.mockito)
    testImplementation(Libs.mockito_kotlin)
}

description = "FLR :: Tests"
