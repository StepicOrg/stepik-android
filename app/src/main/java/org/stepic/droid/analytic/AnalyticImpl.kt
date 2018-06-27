package org.stepic.droid.analytic

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.NotificationManagerCompat
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.base.App
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import java.util.*
import javax.inject.Inject

@AppSingleton
class AnalyticImpl
@Inject constructor(
        context: Context,
        config: Config
) : Analytic {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val amplitude = Amplitude.getInstance()
            .initialize(context, config.amplitudeApiKey)
            .enableForegroundTracking(App.application)

    init {
        amplitude.identify(Identify()
                .add(Analytic.Amplitude.Properties.APPLICATION_ID, context.packageName)
                .add(Analytic.Amplitude.Properties.PUSH_PERMISSION, if (NotificationManagerCompat.from(context).areNotificationsEnabled()) "granted" else "not_granted")
        )
    }

    // Amplitude properties
    override fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
        amplitude.identify(Identify().add(Analytic.Amplitude.Properties.USER_ID, userId))
    }

    override fun setCoursesCount(coursesCount: Int) =
        amplitude.identify(Identify().add(Analytic.Amplitude.Properties.COURSES_COUNT, coursesCount))

    override fun setSubmissionsCount(submissionsCount: Long) =
        amplitude.identify(Identify().add(Analytic.Amplitude.Properties.SUBMISSIONS_MADE, submissionsCount))

    override fun setScreenOrientation(orientation: Int) =
        amplitude.identify(Identify().add(Analytic.Amplitude.Properties.COURSES_COUNT, if (orientation == Configuration.ORIENTATION_PORTRAIT) "portrait" else "landscape"))

    override fun setStreaksNotificationsEnabled(isEnabled: Boolean) =
        amplitude.identify(Identify().add(Analytic.Amplitude.Properties.STREAKS_NOTIFICATIONS_ENABLED, if (isEnabled) "enabled" else "disabled"))
    // End of amplitude properties

    override fun reportEventValue(eventName: String, value: Long) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.VALUE, value)
        reportEvent(eventName, bundle)
    }

    override fun reportEvent(eventName: String, bundle: Bundle?) {
        val map: HashMap<String, String> = HashMap()
        bundle?.keySet()?.forEach {
            map[it] = bundle[it].toString()
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
        Crashlytics.logException(throwable)
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
