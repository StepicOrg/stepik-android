package org.stepik.android.view.onboarding.mapper

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.view.onboarding.model.OnboardingGoal
import javax.inject.Inject

class OnboardingRemoteConfigMapper
@Inject
constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) {
    fun buildOnboardingGoals(): List<OnboardingGoal> {
        val onboardingGoalsString = firebaseRemoteConfig.getString(RemoteConfig.PERSONALIZED_ONBOARDING_COURSE_LISTS)
        return gson.fromJson(onboardingGoalsString, TypeToken.getParameterized(ArrayList::class.java, OnboardingGoal::class.java).type)
    }
}