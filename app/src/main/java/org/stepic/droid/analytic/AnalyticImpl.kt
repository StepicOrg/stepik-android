package org.stepic.droid.analytic

import android.content.Context
import android.os.Bundle

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crash.FirebaseCrash
import com.yandex.metrica.YandexMetrica

class AnalyticImpl(context: Context) : Analytic {
    override fun reportEventWithId(eventName: String, id: String) {
        reportEventWithIdName(eventName, id, null)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
    }

    override fun reportEventWithIdName(eventName: String, id: String, name: String?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        if (name != null) {
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        }
        reportEvent(eventName, bundle)
    }

    private val firebaseAnalytics: FirebaseAnalytics

    init {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun reportEvent(eventName: String, bundle: Bundle?) {
        YandexMetrica.reportEvent(eventName)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    override fun reportEvent(eventName: String) {
        reportEvent(eventName, null)
    }

    override fun reportError(message: String, throwable: Throwable) {
        FirebaseCrash.report(throwable);
        YandexMetrica.reportError(message, throwable)
    }
}
