# Keep line numbers for readable release stack traces (upload mapping.txt
# to Play Console so crash reports get de-obfuscated automatically).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic type info and annotations — needed by Kotlin reflection,
# Compose, and Coroutines internals that inspect generic signatures.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

# --- Kotlin ---
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# --- AndroidX Lifecycle / ViewModel ---
# viewModel() in Compose can reach constructors via reflection on some
# factory paths — keep public constructors of your ViewModels explicitly
# so R8 never strips or renames them away.
-keep class com.mmushtaq.orm.allinone.**.*ViewModel {
    public <init>(...);
}

# --- CameraX ---
-dontwarn androidx.camera.**
-keep class androidx.camera.camera2.** { *; }

# --- Coil (image loading) ---
-dontwarn coil.**

# --- Your data model classes ---
# UnitDef/UnitCategory etc. are constructed directly in code (no
# reflection/serialization), so no explicit keep is needed for them.
# If you later add Gson/Moshi/kotlinx.serialization for persistence,
# add -keep rules for those model classes at that point.