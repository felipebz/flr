dependencies {
    implementation(project(":flr-core"))
    implementation(project(":flr-toolkit"))
    api(libs.assertj)
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter)
}

description = "FLR :: Testing Harness"
