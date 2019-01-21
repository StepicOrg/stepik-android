package org.stepic.droid.core.internetstate

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.internetstate.contract.InternetEnabledListener
import org.stepic.droid.core.internetstate.contract.InternetEnabledPoster
import javax.inject.Inject

class InternetEnabledPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<InternetEnabledListener>
) : InternetEnabledPoster {

    override fun internetEnabled() {
        listenerContainer.asIterable().forEach(InternetEnabledListener::onInternetEnabled)
    }

}
