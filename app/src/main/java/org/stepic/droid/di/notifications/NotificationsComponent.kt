package org.stepic.droid.di.notifications

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.NotificationListFragment
import org.stepik.android.view.injection.user.UserDataModule

@Subcomponent(modules = [UserDataModule::class])
@NotificationsScope
interface NotificationsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): NotificationsComponent
    }

    fun inject(notificationListFragment: NotificationListFragment)
}
