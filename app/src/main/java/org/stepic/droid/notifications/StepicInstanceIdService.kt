package org.stepic.droid.notifications

import com.google.firebase.iid.FirebaseInstanceIdService
import org.stepic.droid.base.App
import org.stepic.droid.core.StepikDevicePoster
import javax.inject.Inject

class StepicInstanceIdService : FirebaseInstanceIdService() {
    private val hacker = HackerFcmInstanceId()

    override fun onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        hacker.stepikDevicePoster.registerDevice()
    }
}

class HackerFcmInstanceId() {
    @Inject
    lateinit var stepikDevicePoster: StepikDevicePoster


    init {
        App.component().inject(this)
    }
}