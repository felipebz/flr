dependencies {
    implementation(project(":flr-core"))
    implementation(project(":flr-toolkit"))
    api(Libs.assertj)
    implementation(platform(Libs.junit_bom))
    implementation(Libs.junit_jupiter)
}

description = "FLR :: Testing Harness"
