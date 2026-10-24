pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/BudgieNet/*") {
            // com.budgienet plugins and libraries - needs gpr.user / gpr.key (read:packages token) in ~/.gradle/gradle.properties
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
                password = providers.gradleProperty("gpr.key").orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
            }
            content { includeGroupByRegex("com\\.budgienet.*") }
        }
    }
}

plugins {
    id("com.budgienet.settings") version "1.1.0"
}

rootProject.name = "BudgieSets"
