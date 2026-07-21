plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.files("detekt.yml"))
    basePath.set(rootProject.projectDir)
    baseline = file("detekt-baseline.xml")
    parallel = true
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                classes(
                    // Data-layer non-logic
                    "com.mandrecode.tempo.*.model.*",
                    "com.mandrecode.tempo.*.entity.*",
                    "com.mandrecode.tempo.core.di.*",
                    "com.mandrecode.tempo.core.data.local.*",
                    // Presentation / Compose UI (tested via instrumented tests)
                    "com.mandrecode.tempo.features.*.presentation.*Content*",
                    "com.mandrecode.tempo.features.*.presentation.*Screen*",
                    "com.mandrecode.tempo.features.*.presentation.*Contract*",
                    "com.mandrecode.tempo.features.*.presentation.*Components*",
                    "com.mandrecode.tempo.features.settings.presentation.CompletedTaskRetentionSection*",
                    "com.mandrecode.tempo.features.settings.presentation.BackupSection*",
                    "com.mandrecode.tempo.features.settings.presentation.ImportModeDialog*",
                    "com.mandrecode.tempo.features.*.presentation.*ExternalActions*",
                    "com.mandrecode.tempo.*.presentation.*Content*",
                    "com.mandrecode.tempo.*.presentation.*Screen*",
                    "com.mandrecode.tempo.*.presentation.*Contract*",
                    "com.mandrecode.tempo.*.presentation.components.*",
                    "com.mandrecode.tempo.core.ui.components.*",
                    "com.mandrecode.tempo.core.ui.navigation.*",
                    "com.mandrecode.tempo.core.ui.theme.*",
                    // Android entry points & broadcast receivers
                    "com.mandrecode.tempo.MainActivity",
                    "com.mandrecode.tempo.TempoApp",
                    "com.mandrecode.tempo.infrastructure.reminders.receivers.*",
                    // Compose-generated classes & previews
                    "*ComposableSingletons*",
                    "*Previews*",
                    // Hilt/Dagger generated classes
                    "hilt_aggregated_deps.*",
                    "dagger.hilt.internal.aggregatedroot.codegen.*",
                    "*_Factory",
                    "*_Factory$*",
                    "*_MembersInjector",
                    "*_HiltModules*",
                    "*_LazyMapKey",
                    "*_ProvideFactory",
                    // Android-dependent classes (require Context/SDK, not unit-testable)
                    "com.mandrecode.tempo.infrastructure.permissions.*",
                    "com.mandrecode.tempo.infrastructure.reminders.scheduler.android.*",
                    "com.mandrecode.tempo.core.ui.util.AnimationUtils*",
                    "com.mandrecode.tempo.core.ui.util.FrozenState*",
                    "com.mandrecode.tempo.util.AppVersionProviderImpl",
                    "com.mandrecode.tempo.util.DataMode",
                    "com.mandrecode.tempo.util.DateTimeFormatter*",
                    // Preview parameter providers (Compose tooling, not runtime)
                    "*PreviewParameterProvider*",
                    "*PreviewFixtures*",
                )
            }
        }

        verify {
            rule {
                bound {
                    minValue = 80
                    coverageUnits = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
                }
            }
            rule {
                bound {
                    minValue = 70
                    coverageUnits = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
                }
            }
        }
    }
}

val appVersionName: String = rootProject.file("version.txt").readText().trim()
val versionParts = appVersionName.split(".")

if (versionParts.size != 3) {
    throw GradleException("version.txt must contain a version in format X.Y.Z, but found '$appVersionName'")
}

val major =
    versionParts[0].toIntOrNull()
        ?: throw GradleException("Invalid major version: ${versionParts[0]}")
val minor =
    versionParts[1].toIntOrNull()
        ?: throw GradleException("Invalid minor version: ${versionParts[1]}")
val patch =
    versionParts[2].toIntOrNull()
        ?: throw GradleException("Invalid patch version: ${versionParts[2]}")

// Deterministic version code derived from the semantic version.
// Monotonically increasing for Google Play ordering.
val calculatedVersionCode =
    major.toLong() * 1_000_000L +
        minor.toLong() * 1_000L +
        patch.toLong()

if (calculatedVersionCode > Int.MAX_VALUE.toLong()) {
    throw GradleException(
        "Version code $calculatedVersionCode exceeds ${Int.MAX_VALUE}. " +
            "Adjust version.txt (max ~2147.483.647).",
    )
}

val appVersionCode: Int = calculatedVersionCode.toInt()

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "com.mandrecode.tempo"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mandrecode.tempo"
        minSdk = 24
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField(
            "String",
            "FEEDBACK_FORM_URL",
            "\"https://docs.google.com/forms/d/e/1FAIpQLSciCv8egchpVXvw9e9jfOVLj1YFrktpULWyOM-Cmq1JW9g24A/viewform?usp=pp_url\"",
        )
        buildConfigField("String", "FEEDBACK_VERSION_ENTRY", "\"entry.863266237\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        localeFilters += listOf("en", "es")
    }

    signingConfigs {
        if (System.getenv("KEYSTORE_FILE") != null) {
            create("release") {
                storeFile = file(System.getenv("KEYSTORE_FILE")!!)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Tempo (Debug)")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
        }
    }

    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    testOptions {
        animationsDisabled = true
    }
    lint {
        // Translation parity is enforced for all translatable string resources:
        // every translatable resource in values/ MUST have a matching
        // translation in every values-<locale>/, and vice versa. CI runs
        // `./gradlew lintDebug` and fails on these issues.
        error += listOf("MissingTranslation", "ExtraTranslation")
        abortOnError = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    ktlintRuleset(libs.compose.rules.ktlint)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material.icons.core)

    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite)
    implementation(libs.android.hilt)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)

    // Navigation 3 dependencies
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.android.hilt.compiler)
    ksp(libs.androidx.room.compiler)
    ksp(libs.androidx.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.collections.immutable)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
