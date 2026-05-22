##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

-dontwarn okio.**
-dontwarn javax.annotation.**

# Optional TLS providers used by OkHttp when present on a JVM. They are not
# packaged with the Android app, so R8 can safely ignore the absent classes.
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

## Joda Time 2.3

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

##---------------Begin: proguard configuration for Retrofit 2  ----------
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Retrofit inspects service return type arguments at runtime. R8 full mode can
# rewrite them to raw types unless the generic wrapper declarations are kept.
-keep,allowshrinking,allowobfuscation interface retrofit2.Call
-keep,allowshrinking,allowobfuscation class retrofit2.Response
-keep,allowshrinking,allowobfuscation class retrofit2.adapter.rxjava2.Result
-keep,allowshrinking,allowobfuscation class io.reactivex.Single
-keep,allowshrinking,allowobfuscation class io.reactivex.Maybe
-keep,allowshrinking,allowobfuscation class io.reactivex.Observable
-keep,allowshrinking,allowobfuscation class io.reactivex.Flowable
##---------------End: proguard configuration for Retrofit 2  ----------


##OTTO
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# Picaso
-dontwarn com.squareup.okhttp.**

#Yandex metrica
-keep class com.yandex.metrica.impl.* { *; }
-dontwarn com.yandex.metrica.impl.*
-keep class com.yandex.metrica.* { *; }
-dontwarn com.yandex.metrica.*

#Install Referrer
-dontwarn com.android.installreferrer
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

#Branch
-dontwarn com.google.firebase.appindexing.**

#Keep POJO
-keep class org.stepic.droid.model.** { *; }
-keep interface org.stepic.droid.model.** { *; }

-keep class org.stepic.droid.adaptive.model.** { *; }
-keep interface org.stepic.droid.adaptive.model.** { *; }

-keep class org.stepic.droid.features.deadlines.model.** { *; }
-keep interface org.stepic.droid.features.deadlines.model.** { *; }

-keep class org.stepic.droid.web.** { *; }
-keep interface org.stepic.droid.web.** { *; }
-dontwarn org.stepic.droid.web.**
-dontwarn org.stepic.droid.model.**

-keep class org.stepik.android.model.** { *; }
-keep interface org.stepik.android.model.** { *; }
-keep public enum org.stepik.android.**{ *;}

-keep class org.stepik.android.remote.**.model.** { *; }
-keep interface org.stepik.android.remote.**.model.** { *; }
-keep class org.stepik.android.domain.catalog.model.** { *; }
-keep interface org.stepik.android.domain.catalog.model.** { *; }
-keep class org.stepik.android.domain.course_recommendations.model.** { *; }
-keep interface org.stepik.android.domain.course_recommendations.model.** { *; }

#Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

#Keep all enums
-keep public enum org.stepic.droid.**{
    *;
}

-keep class org.stepic.droid.notifications.** { *; }
-keep interface org.stepic.droid.notifications.** { *; }
-dontwarn org.stepic.droid.notifications.**

#for saving search view https://stackoverflow.com/questions/18407171/searchview-getactionview-returning-null
-keep class androidx.appcompat.widget.SearchView { *; }
-keep class org.stepic.droid.ui.custom.AutoCompleteSearchView { *; }

#keep configs names
-keep class org.stepic.droid.configuration.** { *; }
-keep interface org.stepic.droid.configuration.** { *; }
-dontwarn org.stepic.droid.configuration.**

#keep javascript interfaces
-keepattributes JavascriptInterface

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keeppackagenames org.jsoup.nodes

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class com.facebook.jni.** { *; }
-keep class com.facebook.flipper.** { *; }
