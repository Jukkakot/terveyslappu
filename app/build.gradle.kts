import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// The short commit goes into the version so a log line tells which build wrote it.
val gitSha: String = providers.environmentVariable("GITHUB_SHA").map { it.take(7) }
    .orElse(
        providers.exec {
            commandLine("git", "rev-parse", "--short=7", "HEAD")
            isIgnoreExitValue = true
        }.standardOutput.asText.map { it.trim().ifEmpty { "dev" } },
    )
    .get()

fun gitCount(vararg paths: String): Int = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD", "--", *paths)
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().toIntOrNull() ?: 1 }.get()

// The version code is the number of all commits, so every pushed build installs over the previous one.
val commitCount: Int = gitCount()

// The build number counts only commits that build something: CI ignores these same paths (ci.yml).
val buildNumber: Int = gitCount(".", ":!openspec", ":!.claude", ":!*.md")

// MAJOR.MINOR from gradle.properties, then the build number, e.g. 1.0.24 (app-version-numbers).
val appVersionName = "${providers.gradleProperty("appVersion").get()}.$buildNumber"

/**
 * Release signing: keystore.properties next to the project (never committed) or, in CI, the
 * RELEASE_* environment variables. Without either, the release build is signed with the debug key.
 */
val releaseSigning: Map<String, String>? = run {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        val props = Properties().apply { file.inputStream().use { load(it) } }
        props.stringPropertyNames().associateWith { props.getProperty(it) }
    } else {
        val path = providers.environmentVariable("RELEASE_STORE_FILE").orNull
        if (path == null) {
            null
        } else {
            mapOf(
                "storeFile" to path,
                "storePassword" to providers.environmentVariable("RELEASE_STORE_PASSWORD").get(),
                "keyAlias" to providers.environmentVariable("RELEASE_KEY_ALIAS").get(),
                "keyPassword" to providers.environmentVariable("RELEASE_KEY_PASSWORD").get(),
            )
        }
    }
}

android {
    namespace = "fi.jukkakot.terveyslappu"
    compileSdk = 37

    defaultConfig {
        applicationId = "fi.jukkakot.terveyslappu"
        minSdk = 34
        targetSdk = 37
        versionCode = commitCount
        versionName = appVersionName
        buildConfigField("String", "GIT_SHA", "\"$gitSha\"")
    }

    androidResources {
        localeFilters += listOf("fi")
    }

    signingConfigs {
        if (releaseSigning != null) {
            create("release") {
                storeFile = rootProject.file(releaseSigning.getValue("storeFile"))
                storePassword = releaseSigning.getValue("storePassword")
                keyAlias = releaseSigning.getValue("keyAlias")
                keyPassword = releaseSigning.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            // Where Robolectric downloads its Android jars; a cloud container whose Maven Central is
            // rate-limited sets robolectricRepoUrl in ~/.gradle/gradle.properties.
            val repoUrl = providers.gradleProperty("robolectricRepoUrl").orNull
            all { if (repoUrl != null) it.systemProperty("robolectric.dependency.repo.url", repoUrl) }
        }
    }

    lint {
        abortOnError = true
        checkDependencies = true
        disable += listOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "OldTargetApi")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.poi)
    implementation(libs.health.connect)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.vico.compose.m3)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
}

// CI reads the version for the release title.
tasks.register("printVersionName") {
    val name = "v$appVersionName ($gitSha)"
    doLast { println(name) }
}
