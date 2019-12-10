package org.stepik.android.view.injection.social_profile

import dagger.Subcomponent

@Subcomponent(modules = [
    SocialProfileDataModule::class
])
interface SocialProfileComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SocialProfileComponent
    }
}