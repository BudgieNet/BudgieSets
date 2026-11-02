plugins {
    id("com.budgienet.paper-plugin")
}

dependencies {
    implementation(libs.commandapi)
    implementation(libs.nbtapi)
    compileOnly(libs.placeholderapi)
    compileOnly(files("libs/BudgieCore-2.0.0-core-SNAPSHOT.jar")) // soft: only hook/BudgieCoreHook touches it

    testImplementation(files("libs/BudgieCore-2.0.0-core-SNAPSHOT.jar"))
}
