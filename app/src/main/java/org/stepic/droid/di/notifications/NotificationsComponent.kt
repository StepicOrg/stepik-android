package org.stepic.droid.di.notifications

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.NotificationListFragment

@Subcomponent
@NotificationsScope
interface NotificationsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): NotificationsComponent
    }

    fun inject(notificationListFragment: NotificationListFragment)
}
