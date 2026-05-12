import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.util.Properties
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.0.21"
}

// ---------------------------------------------------------------------------
// Generate app icon mipmap PNGs from a single high-res source image.
// Run once with:  ./gradlew generateAppIcons
// ---------------------------------------------------------------------------
tasks.register("generateAppIcons") {
    val src = file("src/main/res/drawable/quiz_game_icon_foreground.jpg")

    // (density → legacy px, foreground px)
    val sizes = mapOf(
        "mdpi"    to Pair(48,  108),
        "hdpi"    to Pair(72,  162),
        "xhdpi"   to Pair(96,  216),
        "xxhdpi"  to Pair(144, 324),
        "xxxhdpi" to Pair(192, 432)
    )

    doLast {
        val original = ImageIO.read(src)

        // Center-crop to square
        val side    = minOf(original.width, original.height)
        val cropX   = (original.width  - side) / 2
        val cropY   = (original.height - side) / 2
        val cropped = original.getSubimage(cropX, cropY, side, side)

        fun resizeTo(size: Int): BufferedImage {
            val out = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val g   = out.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON)
            g.drawImage(cropped, 0, 0, size, size, null)
            g.dispose()
            return out
        }

        fun writePng(img: BufferedImage, dest: File) {
            dest.parentFile.mkdirs()
            ImageIO.write(img, "png", dest)
            println("  → ${dest.relativeTo(projectDir)}  (${dest.length()/1024}KB)")
        }

        sizes.forEach { (density, pair) ->
            val (legacyPx, fgPx) = pair
            val dir = file("src/main/res/mipmap-$density")

            // Legacy square icon
            writePng(resizeTo(legacyPx), File(dir, "ic_launcher.png"))
            // Round icon (same image — Android clips it to circle)
            writePng(resizeTo(legacyPx), File(dir, "ic_launcher_round.png"))
            // Adaptive foreground (108dp + bleed)
            writePng(resizeTo(fgPx),     File(dir, "ic_launcher_foreground.png"))
        }
        println("generateAppIcons: done.")
    }
}

// ---------------------------------------------------------------------------
// Compress category images at build time
// Originals live in src/main/category-images/ (not packaged).
// This task resizes them to ≤800 px wide and re-encodes at JPEG quality 85,
// writing results to build/generated/res/compressed/drawable/ which is
// registered as a real res source-set below.
// ---------------------------------------------------------------------------
val compressCategoryImages by tasks.registering {
    val srcDir  = file("src/main/category-images")
    val outDir  = layout.buildDirectory.dir("generated/res/compressed/drawable").get().asFile

    inputs.files(fileTree(srcDir) { include("img_category_*.jpg") })
    outputs.dir(outDir)

    doLast {
        outDir.mkdirs()
        val maxWidth  = 800
        val quality   = 0.85f

        fileTree(srcDir) { include("img_category_*.jpg") }.forEach { src ->
            val out      = File(outDir, src.name)
            val original = ImageIO.read(src)

            val scale = minOf(1.0, maxWidth.toDouble() / original.width)
            val newW  = (original.width  * scale).toInt()
            val newH  = (original.height * scale).toInt()

            val scaled = BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB)
            val g = scaled.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON)
            g.drawImage(original, 0, 0, newW, newH, null)
            g.dispose()

            val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
            val params = writer.defaultWriteParam.apply {
                compressionMode    = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = quality
            }
            val ios = ImageIO.createImageOutputStream(out)
            writer.output = ios
            writer.write(null, IIOImage(scaled, null, null), params)
            ios.close()
            writer.dispose()

            val savedKb = (src.length() - out.length()) / 1024
            println("  ${src.name}: ${src.length()/1024}KB → ${out.length()/1024}KB  (-${savedKb}KB)")
        }
    }
}

android {
    namespace = "com.example.quiz_game"
    compileSdk = 35

    // ── Signing config — credentials loaded from local.properties ─────────
    val localProps = Properties().also { props ->
        val f = rootProject.file("local.properties")
        if (f.exists()) props.load(f.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile     = file(localProps.getProperty("RELEASE_STORE_FILE", "kwikkwiz-release.jks"))
            storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD", "")
            keyAlias      = localProps.getProperty("RELEASE_KEY_ALIAS",      "kwikkwiz-key")
            keyPassword   = localProps.getProperty("RELEASE_KEY_PASSWORD",   "")
        }
    }

    // Add the compressed-images generated folder as a res source-set
    sourceSets["main"].res.srcDir(
        layout.buildDirectory.dir("generated/res/compressed")
    )

    defaultConfig {
        applicationId = "com.hucciproduction.kwikkwiz"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "***REMOVED***"
            buildConfigField("String", "ADMOB_APP_ID",           "\"***REMOVED***\"")
            buildConfigField("String", "ADMOB_BANNER_ID",        "\"***REMOVED***\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID",  "\"***REMOVED***\"")
            buildConfigField("String", "ADMOB_REWARDED_ID",      "\"***REMOVED***\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["admobAppId"] = "***REMOVED***"
            buildConfigField("String", "ADMOB_APP_ID",           "\"***REMOVED***\"")
            buildConfigField("String", "ADMOB_BANNER_ID",        "\"***REMOVED***\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID",  "\"***REMOVED***\"")
            buildConfigField("String", "ADMOB_REWARDED_ID",      "\"***REMOVED***\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Run compress task before anything else touches resources
    tasks.named("preBuild") { dependsOn(compressCategoryImages) }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.gson)

    implementation(libs.translate)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    implementation(libs.play.services.ads)

    // UMP (GDPR consent)
    implementation(libs.user.messaging.platform)

    // In-App Review
    implementation(libs.review.ktx)

    // Shimmer for skeleton loaders (compose-shimmer)
    implementation(libs.compose.shimmer)

    // Splashscreen API
    implementation(libs.androidx.core.splashscreen)

    // LottieFiles
    implementation(libs.lottie)
}
