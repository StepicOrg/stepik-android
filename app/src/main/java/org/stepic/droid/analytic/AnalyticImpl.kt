package org.stepic.droid.analytic

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.UserProfile
import org.json.JSONObject
import org.stepic.droid.base.App
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.util.isARSupported
import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.base.analytic.AnalyticSource
import ru.nobird.android.view.base.ui.extension.isNightModeEnabled
import java.util.HashMap
import java.util.Locale
import javax.inject.Inject

@AppSingleton
class AnalyticImpl
@Inject
constructor(
    context: Context,
    config: Config,
    private val stepikAnalytic: StepikAnalytic
) : Analytic {
    private companion object {
        private const val FIREBASE_USER_PROPERTY_NAME_LIMIT = 24
        private const val FIREBASE_USER_PROPERTY_VALUE_LIMIT = 36
        private const val FIREBASE_LENGTH_LIMIT = 40

        inline fun updateYandexUserProfile(mutation: UserProfile.Builder.() -> Unit) {
            val userProfile = UserProfile.newBuilder()
                .apply(mutation)
                .build()
            YandexMetrica.reportUserProfile(userProfile)
        }
    }

    private val firebaseAnalytics = Firebase.analytics
    private val firebaseCrashlytics = FirebaseCrashlytics.getInstance()

    private val amplitude = Amplitude.getInstance()
            .initialize(context, config.amplitudeApiKey)
            .enableForegroundTracking(App.application)

    init {
        val isNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

        amplitude.identify(Identify()
                .set(AmplitudeAnalytic.Properties.APPLICATION_ID, context.packageName)
                .set(AmplitudeAnalytic.Properties.PUSH_PERMISSION, if (isNotificationsEnabled) "granted" else "not_granted")
                .set(AmplitudeAnalytic.Properties.IS_NIGHT_MODE_ENABLED, context.isNightModeEnabled().toString())
                .set(AmplitudeAnalytic.Properties.IS_AR_SUPPORTED, context.isARSupported().toString())
        )

        updateYandexUserProfile {
            apply(Attribute.notificationsEnabled().withValue(isNotificationsEnabled))
            apply(Attribute.customBoolean(AmplitudeAnalytic.Properties.IS_NIGHT_MODE_ENABLED).withValue(context.isNightModeEnabled()))
            apply(Attribute.customBoolean(AmplitudeAnalytic.Properties.IS_AR_SUPPORTED).withValue(context.isARSupported()))
        }

        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.PUSH_PERMISSION, if (isNotificationsEnabled) "granted" else "not_granted")
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.IS_NIGHT_MODE_ENABLED, context.isNightModeEnabled().toString())
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.IS_AR_SUPPORTED, context.isARSupported().toString())
    }

    // Amplitude properties
    private fun syncAmplitudeProperties() {
        setScreenOrientation(Resources.getSystem().configuration.orientation)
    }

    override fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
        firebaseCrashlytics.setUserId(userId)
        YandexMetrica.setUserProfileID(userId)
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.STEPIK_ID, userId))
    }

    override fun setCoursesCount(coursesCount: Int) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.COURSES_COUNT, coursesCount))
        updateYandexUserProfile { apply(Attribute.customNumber(AmplitudeAnalytic.Properties.COURSES_COUNT).withValue(coursesCount.toDouble())) }
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.COURSES_COUNT, coursesCount.toString())
    }

    override fun setSubmissionsCount(submissionsCount: Long, delta: Long) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.SUBMISSIONS_COUNT, submissionsCount + delta))
        updateYandexUserProfile { apply(Attribute.customCounter(AmplitudeAnalytic.Properties.SUBMISSIONS_COUNT).withDelta(delta.toDouble())) }
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.SUBMISSIONS_COUNT,  (submissionsCount + delta).toString())
    }

    override fun setScreenOrientation(orientation: Int) {
        val orientationName = if (orientation == Configuration.ORIENTATION_PORTRAIT) "portrait" else "landscape"
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.SCREEN_ORIENTATION, orientationName))
        updateYandexUserProfile { apply(Attribute.customString(AmplitudeAnalytic.Properties.SCREEN_ORIENTATION).withValue(orientationName))  }
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.SCREEN_ORIENTATION, orientationName)
    }

    override fun setStreaksNotificationsEnabled(isEnabled: Boolean) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.STREAKS_NOTIFICATIONS_ENABLED, if (isEnabled) "enabled" else "disabled"))
        updateYandexUserProfile { apply(Attribute.customBoolean(AmplitudeAnalytic.Properties.STREAKS_NOTIFICATIONS_ENABLED).withValue(isEnabled)) }
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.STREAKS_NOTIFICATIONS_ENABLED.substring(0, 24), if (isEnabled) "enabled" else "disabled")
    }

    override fun setTeachingCoursesCount(coursesCount: Int) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.TEACHING_COURSES_COUNT, coursesCount))
        updateYandexUserProfile { apply(Attribute.customNumber(AmplitudeAnalytic.Properties.TEACHING_COURSES_COUNT).withValue(coursesCount.toDouble())) }
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.TEACHING_COURSES_COUNT, coursesCount.toString())
    }

    override fun setGoogleServicesAvailable(isAvailable: Boolean) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.IS_GOOGLE_SERVICES_AVAILABLE, isAvailable.toString()))
        updateYandexUserProfile { apply(Attribute.customBoolean(AmplitudeAnalytic.Properties.IS_GOOGLE_SERVICES_AVAILABLE).withValue(isAvailable)) }
        firebaseAnalytics.setUserProperty(AmplitudeAnalytic.Properties.IS_GOOGLE_SERVICES_AVAILABLE.substring(0, 24), isAvailable.toString())
    }

    override fun report(analyticEvent: AnalyticEvent) {
        if (AnalyticSource.YANDEX in analyticEvent.sources) {
            YandexMetrica.reportEvent(analyticEvent.name, analyticEvent.params)
        }

        if (AnalyticSource.AMPLITUDE in analyticEvent.sources) {
            syncAmplitudeProperties()
            val properties = JSONObject()
            for ((k, v) in analyticEvent.params.entries) {
                properties.put(k, v)
            }
            amplitude.logEvent(analyticEvent.name, properties)
            firebaseCrashlytics.log("${analyticEvent.name}=${analyticEvent.params}")
        }

        if (AnalyticSource.FIREBASE in analyticEvent.sources) {
            val bundle = bundleOf(*analyticEvent.params.map { (a, b) -> a to b }.toTypedArray())
            val firebaseEventName = castStringToFirebaseEvent(analyticEvent.name)
            firebaseAnalytics.logEvent(firebaseEventName, bundle)
        }

        if (AnalyticSource.STEPIK_API in analyticEvent.sources) {
            stepikAnalytic.logEvent(analyticEvent.name, analyticEvent.params)
        }
    }

    override fun reportAmplitudeEvent(eventName: String) = reportAmplitudeEvent(eventName, null)
    override fun reportAmplitudeEvent(eventName: String, params: Map<String, Any>?) {
        syncAmplitudeProperties()
        val properties = JSONObject()
        params?.let {
            for ((k, v) in it.entries) {
                properties.put(k, v)
            }
        }
        amplitude.logEvent(eventName, properties)
        YandexMetrica.reportEvent(eventName, params)
        firebaseCrashlytics.log("$eventName=$params")

        val bundle = bundleOf(*params?.map { (a, b) -> a to b }?.toTypedArray() ?: emptyArray())
        val firebaseEventName = castStringToFirebaseEvent(eventName)
        firebaseAnalytics.logEvent(firebaseEventName, bundle)
    }

    override fun setUserProperty(name: String, value: String) {
        amplitude.identify(Identify().set(name, value))
        updateYandexUserProfile { apply(Attribute.customString(name).withValue(value)) }
        firebaseCrashlytics.setCustomKey(name, value)
        firebaseAnalytics.setUserProperty(name.take(FIREBASE_USER_PROPERTY_NAME_LIMIT), value.take(FIREBASE_USER_PROPERTY_VALUE_LIMIT))
    }
    // End of amplitude properties

    override fun reportEventValue(eventName: String, value: Long) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.VALUE, value)
        reportEvent(eventName, bundle)
    }

    override fun reportEvent(eventName: String, bundle: Bundle?) {
        val map: HashMap<String, String> = HashMap()
        bundle?.keySet()?.forEach {
            map[it] = java.lang.String.valueOf(bundle[it]) // handle null as bundle[it].toString() calls object.toString() and cause NPE instead of Any?.toString()
        }
        if (map.isEmpty()) {
            YandexMetrica.reportEvent(eventName)
        } else {
            YandexMetrica.reportEvent(eventName, map as Map<String, Any>?)
        }

        val eventNameLocal = castStringToFirebaseEvent(eventName)
        firebaseAnalytics.logEvent(eventNameLocal, bundle)
    }

    override fun reportEvent(eventName: String) {
        reportEvent(eventName, null)
    }

    override fun reportError(message: String, throwable: Throwable) {
        firebaseCrashlytics.recordException(throwable)
        YandexMetrica.reportError(message, throwable)
    }

    override fun reportEvent(eventName: String, id: String) {
        reportEventWithIdName(eventName, id, null)
    }

    override fun reportEventWithName(eventName: String, name: String?) {
        val bundle = Bundle()
        if (name != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        }
        reportEvent(eventName, bundle)
    }

    override fun reportEventWithIdName(eventName: String, id: String, name: String?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        if (name != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        }
        reportEvent(eventName, bundle)
    }

    private fun castStringToFirebaseEvent(eventName: String): String {
        val firebaseEventName = eventName
            .decapitalize(Locale.ENGLISH)
            .replace(' ', '_')

        return firebaseEventName.take(FIREBASE_LENGTH_LIMIT)
    }
}
