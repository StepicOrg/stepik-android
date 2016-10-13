package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.NotificationModule
import org.stepic.droid.ui.fragments.NotificationListFragment

@Subcomponent(modules = arrayOf(NotificationModule::class))
@PerFragment
interface NotificationComponent {
    fun inject(notificationListFragment: NotificationListFragment)
}
