object Versions {
    const val asm = "9.2"
    const val commons_io = "2.4"
    const val fest = "1.4"
    const val hamcrest = "2.2"
    const val jaxen = "1.2.0"
    const val junit = "5.8.1"
    const val mockito = "4.0.0"
    const val mockito_kotlin = "4.0.0"
}

object Libs {
    const val asm = "org.ow2.asm:asm:${Versions.asm}"
    const val commons_io = "commons-io:commons-io:${Versions.commons_io}"
    const val fest_assert = "org.easytesting:fest-assert:${Versions.fest}"
    const val hamcrest = "org.hamcrest:hamcrest-library:${Versions.hamcrest}"
    const val jaxen = "jaxen:jaxen:${Versions.jaxen}"
    const val junit_bom = "org.junit:junit-bom:${Versions.junit}"
    const val junit_jupiter = "org.junit.jupiter:junit-jupiter"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockito_kotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockito_kotlin}"
}
