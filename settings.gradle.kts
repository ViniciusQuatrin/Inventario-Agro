pluginManagement {
    repositories {
        gradlePluginPortal()   // Onde mora o com.google.devtools.ksp
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                useModule(
                    "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:" +
                            requested.version
                )
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            files("gradle/libs.versions.toml")
        }
    }
}

rootProject.name = "InventarioAgro"
include(":app")