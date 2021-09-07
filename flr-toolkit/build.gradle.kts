dependencies {
    implementation(project(":flr-xpath"))
    api(project(":flr-core"))
    testImplementation(Libs.fest_assert)
    testImplementation(Libs.mockito)
    testImplementation(Libs.mockito_kotlin)
}

description = "FLR :: Toolkit"
