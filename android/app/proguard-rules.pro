# ProGuard Rules for MyUPSI Portal Staff
# Optimized for Capacitor apps with WebView

# General Android
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===== WebView + JavaScript Interface =====
# CRITICAL: Keep JavaScript interface for WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep MainActivity and WebAppInterface (our JS bridge)
-keep class my.edu.upsi.portal.staff.MainActivity { *; }
-keep class my.edu.upsi.portal.staff.MainActivity$WebAppInterface { *; }
-keepclassmembers class my.edu.upsi.portal.staff.MainActivity$WebAppInterface {
    public *;
}

# Keep WebView classes
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}

-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# ===== Capacitor Framework =====
-keep class com.getcapacitor.** { *; }
-keep @com.getcapacitor.annotation.CapacitorPlugin class * { *; }
-keepclassmembers class * {
    @com.getcapacitor.annotation.CapacitorMethod <methods>;
    @com.getcapacitor.PluginMethod <methods>;
}

# Keep Capacitor plugins
-keep class com.getcapacitor.plugin.** { *; }

# ===== AndroidX =====
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# AndroidX Core
-keep class androidx.core.** { *; }
-keep class androidx.appcompat.** { *; }
-keep class androidx.coordinatorlayout.** { *; }
-keep class androidx.swiperefreshlayout.** { *; }

# ===== Cordova =====
-keep class org.apache.cordova.** { *; }
-keep public class * extends org.apache.cordova.CordovaPlugin
-keep class org.apache.cordova.CordovaArgs { *; }

# ===== Firebase (if used) =====
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ===== Gson / JSON =====
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep JSON classes (if using org.json)
-keep class org.json.** { *; }

# ===== OkHttp / Networking =====
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ===== Remove Logging =====
# Remove all Log.v, Log.d, Log.i for production
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# Keep Log.w and Log.e for production debugging
-keep class android.util.Log {
    public static *** w(...);
    public static *** e(...);
}

# ===== Optimization =====
-optimizationpasses 5
-dontpreverify
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ===== Ignore Warnings =====
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.**
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

# ===== Keep R classes =====
-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class **.R$* { *; }
