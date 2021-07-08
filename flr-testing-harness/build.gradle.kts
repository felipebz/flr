dependencies {
    implementation(project(":flr-core"))
    implementation(project(":flr-toolkit"))
    implementation("org.easytesting:fest-assert:1.4")
    implementation("org.hamcrest:hamcrest:2.2")
    implementation("junit:junit:4.13.1")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
}

description = "FLR :: Testing Harness"
