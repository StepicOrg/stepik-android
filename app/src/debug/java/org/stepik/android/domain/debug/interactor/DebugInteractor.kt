package org.stepik.android.domain.debug.interactor

import com.facebook.login.LoginManager
import com.google.firebase.iid.FirebaseInstanceId
import com.vk.api.sdk.VK
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.stepic.droid.core.StepikLogoutManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.EndpointConfig
import org.stepik.android.domain.debug.model.DebugSettings
import javax.inject.Inject

class DebugInteractor
@Inject
constructor(
    private val firebaseInstanceId: FirebaseInstanceId,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val logoutManager: StepikLogoutManager
) {

    fun fetchDebugSettings(): Single<DebugSettings> =
        Singles.zip(
            getFirebaseToken(),
            getEndpointConfig()
        ) { fcmToken, debugBaseUrl ->
            DebugSettings(fcmToken, debugBaseUrl)
        }

    fun updateEndpointConfig(endpointConfig: EndpointConfig): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.putEndpointConfig(endpointConfig.ordinal)
            logoutManager.logout {
                LoginManager.getInstance().logOut()
                VK.logout()
            }
        }

    private fun getFirebaseToken(): Single<String> =
        Single.create { emitter ->
            firebaseInstanceId
                .instanceId
                .addOnSuccessListener { instanceIdResult -> emitter.onSuccess(instanceIdResult.token) }
                .addOnFailureListener(emitter::onError)
        }

    private fun getEndpointConfig(): Single<EndpointConfig> =
        Single.fromCallable {
            EndpointConfig.values()[sharedPreferenceHelper.endpointConfig]
        }
}