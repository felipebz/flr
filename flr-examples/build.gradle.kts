dependencies {
    implementation(project(":flr-core"))
    testImplementation(Libs.fest_assert)
    testImplementation(Libs.mockito)
    testImplementation(project(":flr-testing-harness"))
}

description = "FLR :: Examples"
