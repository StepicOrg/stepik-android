package org.stepik.android.domain.banner.interactor

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.banner.model.Banner
import java.lang.Exception
import javax.inject.Inject

class BannerInteractor
@Inject
constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val gson: Gson
) {
    fun getBanners(screen: Banner.Screen): Single<List<Banner>> =
        zip(
            Single.just(sharedPreferenceHelper.languageForFeatured),
            Single.just(firebaseRemoteConfig.getString(RemoteConfig.BANNERS_ANDROID))
        ) { language, bannersJson ->
            if (bannersJson.isEmpty()) {
                emptyList()
            } else {
                try {
                    val banners =
                        gson.fromJson<List<Banner>>(
                            bannersJson,
                            TypeToken.getParameterized(
                                ArrayList::class.java,
                                Banner::class.java
                            ).type
                        )

                    banners.filter {
                        it.type != null &&
                            it.screen != null &&
                            it.language == language &&
                            it.screen == screen
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
}