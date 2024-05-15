dependencies {
    implementation(project(":flr-xpath"))
    api(project(":flr-core"))
    testImplementation(Libs.assertj)
    testImplementation(Libs.mockito)
    testImplementation(Libs.mockito_kotlin)
}

description = "FLR :: Toolkit"
