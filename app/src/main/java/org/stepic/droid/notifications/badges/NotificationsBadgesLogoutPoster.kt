package org.stepic.droid.notifications.badges

import android.content.Context
import me.leolin.shortcutbadger.ShortcutBadger
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.di.AppSingleton
import javax.inject.Inject


/**
 * Lightweight class to post logout event to badge listeners
 */
@AppSingleton
class NotificationsBadgesLogoutPoster
@Inject
constructor(
        private val context: Context,
        private val listenerContainer: ListenerContainer<NotificationsBadgesListener>
) {
    /**
     * Used to clear counters on logout
     */
    fun clearCounter() {
        ShortcutBadger.applyCount(context, 0)
        listenerContainer.asIterable().forEach(NotificationsBadgesListener::onBadgeShouldBeHidden)
    }
}