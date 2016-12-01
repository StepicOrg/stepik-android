package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.NotificationTimeModule
import org.stepic.droid.core.modules.ProfileModule
import org.stepic.droid.ui.fragments.ProfileFragment


@PerFragment
@Subcomponent(modules = arrayOf(ProfileModule::class, NotificationTimeModule::class))
interface ProfileComponent {
    fun inject(profileFragment: ProfileFragment)
}
