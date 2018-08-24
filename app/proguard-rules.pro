-keepnames class * { @butterknife.BindDrawable *;}
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

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
-keep class com.appsflyer.** { *; }
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

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
-keep class android.support.v7.widget.SearchView { *; }
-keep class org.stepic.droid.ui.custom.AutoCompleteSearchView { *; }

#keep configs names
-keep class org.stepic.droid.configuration.** { *; }
-keep interface org.stepic.droid.configuration.** { *; }
-dontwarn org.stepic.droid.configuration.**

#keep javascript interfaces
-keepattributes JavascriptInterface
-keep public class org.stepic.droid.ui.custom.LatexSupportableWebView$OnScrollWebListener
-keepclassmembers class org.stepic.droid.ui.custom.LatexSupportableWebView$OnScrollWebListener {
    public *;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keeppackagenames org.jsoup.nodes