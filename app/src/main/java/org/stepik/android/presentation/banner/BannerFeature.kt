package org.stepik.android.presentation.banner

import org.stepik.android.domain.banner.model.Banner

interface BannerFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        data class Content(val banners: List<Banner>) : State()
    }

    sealed class Message {
        data class InitMessage(
            val language: String,
            val screen: Banner.Screen,
            val forceUpdate: Boolean = false
        ) : Message()
        object BannersError : Message()
        data class BannersResult(val banners: List<Banner>) : Message()
    }

    sealed class Action {
        data class LoadBanners(val language: String, val screen: Banner.Screen) : Action()
    }
}