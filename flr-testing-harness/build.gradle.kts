dependencies {
    implementation(project(":flr-core"))
    implementation(project(":flr-toolkit"))
    api(Libs.assertj)
    api(Libs.hamcrest)
    implementation(platform(Libs.junit_bom))
    implementation(Libs.junit_jupiter)
    testImplementation(Libs.hamcrest)
}

description = "FLR :: Testing Harness"
