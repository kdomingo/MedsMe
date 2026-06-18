plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

import java.util.Properties

fun loadVersionProps(rootDir: File): Properties {
    val props = Properties()
    val f = File(rootDir, "version.properties")
    if (f.exists()) {
        f.inputStream().use(props::load)
    }
    return props
}

fun versionCodeFrom(props: Properties): Int =
    props.getProperty("VERSION_CODE")?.toIntOrNull() ?: 1

fun versionNameFrom(props: Properties): String =
    props.getProperty("VERSION_NAME") ?: "1.0.0"

android {
    namespace = "com.domtech.medtracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.domtech.medtracker"
        minSdk = 26
        targetSdk = 35
        val versionProps = loadVersionProps(rootProject.rootDir)
        versionCode = versionCodeFrom(versionProps)
        versionName = versionNameFrom(versionProps)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/medsme-release.jks")
            storePassword = "MedsMePass123!"
            keyAlias = "medsme-alias"
            keyPassword = "MedsMePass123!"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false
        }
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        val versionProps = loadVersionProps(rootProject.rootDir)
        val vName = versionNameFrom(versionProps)
        val vCode = versionCodeFrom(versionProps)

        variant.outputs.forEach { output ->
            val out = output as? com.android.build.api.variant.impl.VariantOutputImpl
            if (out != null) {
                out.outputFileName = "MedsMe-${variant.name}-v${vName}(${vCode}).apk"
            }
        }
    }
}

val incrementVersionCode by tasks.registering {
    group = "versioning"
    description = "Increment VERSION_CODE in version.properties"

    val propsFile = rootProject.file("version.properties")
    doLast {
        val f = propsFile
        val props = Properties()
        if (f.exists()) {
            f.inputStream().use(props::load)
        }
        val current = props.getProperty("VERSION_CODE")?.toIntOrNull() ?: 0
        props["VERSION_CODE"] = (current + 1).toString()
        if (!props.containsKey("VERSION_NAME")) {
            props["VERSION_NAME"] = "1.0.0"
        }
        f.outputStream().use { out ->
            props.store(out, "Auto-updated by Gradle")
        }
        println("Bumped VERSION_CODE: $current -> ${current + 1}")
    }
}

// Auto-increment and rename artifacts when producing installable files for staging/release.
tasks.configureEach {
    val n = name.lowercase()
    val isAssembleOrBundle = n.startsWith("assemble") || n.startsWith("bundle")
    val isStagingOrRelease = n.contains("staging") || n.contains("release")
    
    if (isAssembleOrBundle && isStagingOrRelease) {
        dependsOn(incrementVersionCode)
    }

    // Rename .aab files after they are generated
    if (name.startsWith("bundle") && !name.contains("Test")) {
        val variantName = name.removePrefix("bundle").replaceFirstChar { it.lowercase() }
        val bundleDirProvider = layout.buildDirectory.dir("outputs/bundle/$variantName")
        val propsFile = rootProject.file("version.properties")
        
        doLast {
            val props = Properties()
            if (propsFile.exists()) {
                propsFile.inputStream().use { props.load(it) }
            }
            val vName = props.getProperty("VERSION_NAME") ?: "1.0.0"
            val vCode = props.getProperty("VERSION_CODE") ?: "1"
            
            val dir = bundleDirProvider.get().asFile
            val bundleFile = File(dir, "app-$variantName.aab")
            if (bundleFile.exists()) {
                val newFile = File(dir, "MedsMe-$variantName-v${vName}(${vCode}).aab")
                bundleFile.renameTo(newFile)
                println("Bundle renamed to: ${newFile.name}")
            }
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.activity:activity-compose:1.10.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-compiler:2.55")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // Splashscreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // MVVM
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")

    // Room
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Networking
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-client-logging:2.3.12")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.robolectric:robolectric:4.14")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("io.ktor:ktor-client-mock:2.3.12")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.55")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.55")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
