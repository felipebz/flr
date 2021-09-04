dependencies {
    implementation(project(":flr-core"))
    implementation(project(":flr-toolkit"))
    implementation("org.easytesting:fest-assert:1.4")
    implementation("org.hamcrest:hamcrest:2.2")
    implementation(platform("org.junit:junit-bom:5.7.2"))
    implementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
}

description = "FLR :: Testing Harness"
