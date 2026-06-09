-keep class dev.honcho.android.network.models.** { *; }
-keepclassmembers class dev.honcho.android.network.models.** { *; }

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-dontwarn okhttp3.**
-dontwarn okio.**

-keepclassmembers @com.squareup.moshi.JsonClass class ** { *; }
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
