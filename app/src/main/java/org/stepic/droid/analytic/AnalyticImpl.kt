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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.google.firebase.ktx.Firebase
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.UserProfile
import org.json.JSONObject
import org.stepic.droid.base.App
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.util.isARSupported
import org.stepic.droid.util.isNightModeEnabled
import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.base.analytic.AnalyticSource
import java.util.HashMap
import javax.inject.Inject

@AppSingleton
class AnalyticImpl
@Inject
constructor(
    context: Context,
    config: Config
) : Analytic {
    private companion object {
        inline fun updateYandexUserProfile(mutation: UserProfile.Builder.() -> Unit) {
            val userProfile = UserProfile.newBuilder()
                .apply(mutation)
                .build()
            YandexMetrica.reportUserProfile(userProfile)
        }
    }

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
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
    }

    override fun setSubmissionsCount(submissionsCount: Long, delta: Long) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.SUBMISSIONS_COUNT, submissionsCount + delta))
        updateYandexUserProfile { apply(Attribute.customCounter(AmplitudeAnalytic.Properties.SUBMISSIONS_COUNT).withDelta(delta.toDouble())) }
    }

    override fun setScreenOrientation(orientation: Int) {
        val orientationName = if (orientation == Configuration.ORIENTATION_PORTRAIT) "portrait" else "landscape"
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.SCREEN_ORIENTATION, orientationName))
        updateYandexUserProfile { apply(Attribute.customString(AmplitudeAnalytic.Properties.SCREEN_ORIENTATION).withValue(orientationName))  }
    }

    override fun setStreaksNotificationsEnabled(isEnabled: Boolean) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.STREAKS_NOTIFICATIONS_ENABLED, if (isEnabled) "enabled" else "disabled"))
        updateYandexUserProfile { apply(Attribute.customBoolean(AmplitudeAnalytic.Properties.STREAKS_NOTIFICATIONS_ENABLED).withValue(isEnabled)) }
    }

    override fun setTeachingCoursesCount(coursesCount: Int) {
        amplitude.identify(Identify().set(AmplitudeAnalytic.Properties.TEACHING_COURSES_COUNT, coursesCount))
        updateYandexUserProfile { apply(Attribute.customNumber(AmplitudeAnalytic.Properties.TEACHING_COURSES_COUNT).withValue(coursesCount.toDouble())) }
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
            firebaseAnalytics.logEvent(analyticEvent.name, bundle)
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
    }

    override fun setUserProperty(name: String, value: String) {
        amplitude.identify(Identify().set(name, value))
        updateYandexUserProfile { apply(Attribute.customString(name).withValue(value)) }
        firebaseCrashlytics.setCustomKey(name, value)
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
        var eventNameLocal =
                if (eventName == Analytic.Interaction.SUCCESS_LOGIN) {
                    FirebaseAnalytics.Event.LOGIN
                } else {
                    eventName
                }

        val sb = StringBuilder()
        eventNameLocal.forEach {
            if (Character.isLetterOrDigit(it) && !Character.isWhitespace(it)) {
                sb.append(it)
            } else {
                sb.append("_")
            }
        }
        eventNameLocal = sb.toString()

        if (eventNameLocal.length > 32L) {
            eventNameLocal = eventNameLocal.substring(0, 32)
        }
        return eventNameLocal
    }
}
