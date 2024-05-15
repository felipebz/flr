dependencies {
    implementation(project(":flr-core"))
    testImplementation(Libs.assertj)
    testImplementation(Libs.mockito)
    testImplementation(project(":flr-testing-harness"))
}

description = "FLR :: Examples"
