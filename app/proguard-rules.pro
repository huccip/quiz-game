# ── Stack traces ─────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin Serialization ──────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# ── Gson / Retrofit ───────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ── Data models (Gson deserialization — keep all fields from R8 renaming) ─────
-keep class com.example.quiz_game.data.** { *; }

# ── AdMob / Google Mobile Ads ─────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }

# ── ML Kit Translate ──────────────────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ── Coil ──────────────────────────────────────────────────────────────────────
-dontwarn coil.**

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Play Core (In-App Review) ─────────────────────────────────────────────────
-keep class com.google.android.play.core.** { *; }

# ── R8 missing-class suppressions (auto-generated) ───────────────────────────
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
