import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.ktor.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.xml)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.constraintlayout.compose.multiplatform)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)

            implementation(libs.jsoup)
            implementation(libs.sqlite.jdbc)
        }
        commonTest.dependencies {
            implementation("org.testng:testng:7.1.0")
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            implementation("org.junit.jupiter:junit-jupiter:5.10.2")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

    }

}

android {
    namespace = "github.rikacelery.learnlearnjlu"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].jniLibs {
        this
        srcDir("src/androidMain/jniLibs")
    }
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "github.rikacelery.learnlearnjlu"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            //Missing class org.slf4j.impl.StaticLoggerBinder (referenced from: void org.slf4j.LoggerFactory.bind() and 3 other contexts)

        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        buildTypes.release.proguard {
            configurationFiles.from(files("proguard.pro"))
            version.set("7.2.2")
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "github.rikacelery.learnlearnjlu"
            packageVersion = "1.0.0"
        }
    }
}
