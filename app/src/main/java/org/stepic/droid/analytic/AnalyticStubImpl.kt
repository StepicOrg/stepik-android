package org.stepic.droid.analytic

import android.os.Bundle
import org.stepik.android.domain.base.analytic.AnalyticEvent
import javax.inject.Inject

class AnalyticStubImpl
@Inject
constructor() : Analytic {
    override fun reportEvent(eventName: String?, bundle: Bundle?) {}

    override fun reportEvent(eventName: String?, id: String?) {}

    override fun reportEventWithIdName(eventName: String?, id: String?, name: String?) {}

    override fun reportEventWithName(eventName: String?, name: String?) {}

    override fun reportEvent(eventName: String?) {}

    override fun reportError(message: String?, throwable: Throwable) {}

    override fun setUserId(userId: String) {}

    override fun setCoursesCount(coursesCount: Int) {}

    override fun setSubmissionsCount(submissionsCount: Long, delta: Long) {}

    override fun setScreenOrientation(orientation: Int) {}

    override fun setStreaksNotificationsEnabled(isEnabled: Boolean) {}

    override fun setTeachingCoursesCount(coursesCount: Int) {}

    override fun reportAmplitudeEvent(eventName: String, params: MutableMap<String, Any>?) {}

    override fun reportAmplitudeEvent(eventName: String) {}

    override fun report(analyticEvent: AnalyticEvent) {}

    override fun setUserProperty(name: String, value: String) {}

    override fun reportEventValue(eventName: String?, value: Long) {}
}