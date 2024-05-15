object Versions {
    const val asm = "9.7"
    const val assertj = "3.25.3"
    const val jaxen = "2.0.0"
    const val junit = "5.10.2"
    const val mockito = "5.12.0"
    const val mockito_kotlin = "5.3.1"
}

object Libs {
    const val asm = "org.ow2.asm:asm:${Versions.asm}"
    const val assertj = "org.assertj:assertj-core:${Versions.assertj}"
    const val jaxen = "jaxen:jaxen:${Versions.jaxen}"
    const val junit_bom = "org.junit:junit-bom:${Versions.junit}"
    const val junit_jupiter = "org.junit.jupiter:junit-jupiter"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockito_kotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockito_kotlin}"
}
