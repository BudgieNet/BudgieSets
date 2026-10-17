plugins {
    id("com.budgienet.paper-plugin")
}

dependencies {
    implementation(libs.commandapi)
    implementation(libs.nbtapi)
    compileOnly(libs.placeholderapi)
}
