dependencies {
    implementation(project(":flr-core"))
    implementation(Libs.jaxen)
    testImplementation(Libs.assertj)
    testImplementation(Libs.mockito)
    testImplementation(Libs.mockito_kotlin)
}

description = "FLR :: XPath"
