pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.gradle.develocity") version "4.5.0"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"

        if (System.getenv("CI") != null) {
            tag("CI")

            val server = System.getenv("GITHUB_SERVER_URL") ?: "https://github.com"
            val repo = System.getenv("GITHUB_REPOSITORY")
            val runId = System.getenv("GITHUB_RUN_ID")

            if (repo != null && runId != null) {
                 link("GitHub Action", "$server/$repo/actions/runs/$runId")
            }

            System.getenv("GITHUB_REF_NAME")?.let { value("Branch", it) }
            System.getenv("GITHUB_SHA")?.let { value("Commit", it) }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Tempo"
include(":app")
include(":benchmark")
