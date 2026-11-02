plugins {
    id("com.budgienet.paper-plugin")
}

dependencies {
    implementation(libs.commandapi)
    implementation(libs.nbtapi)
    compileOnly(libs.placeholderapi)
    compileOnly("com.budgienet:budgiecore:2.0.0") // soft: only hook/BudgieCoreHook touches it

    testImplementation("com.budgienet:budgiecore:2.0.0")
}
