package org.stepic.droid.core.earlystreak

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.earlystreak.contract.EarlyStreakListener
import org.stepic.droid.core.earlystreak.contract.EarlyStreakPoster
import javax.inject.Inject

class EarlyStreakPosterImpl
@Inject
constructor(
        private val listenerContainer: ListenerContainer<EarlyStreakListener>,
        private val firebaseRemoteConfig: FirebaseRemoteConfig
) : EarlyStreakPoster {
    override fun showStreakSuggestion() {
        if (firebaseRemoteConfig.getBoolean(RemoteConfig.showStreakDialogAfterLogin)) {
            listenerContainer.asIterable().forEach { it.onShowStreakSuggestion() }
        }
    }
}

