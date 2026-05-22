package org.stepik.android.domain.debug.interactor

import com.google.firebase.messaging.FirebaseMessaging
import com.vk.api.sdk.VK
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.stepic.droid.core.StepikLogoutManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.EndpointConfig
import org.stepik.android.domain.debug.model.DebugSettings
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DebugInteractor
@Inject
constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val logoutManager: StepikLogoutManager
) {
    private companion object {
        const val FCM_TOKEN_TIMEOUT_SECONDS = 5L
        const val FCM_TOKEN_UNAVAILABLE = "Unavailable"
    }

    fun fetchDebugSettings(): Single<DebugSettings> =
        Singles.zip(
            getFirebaseToken(),
            getEndpointConfig()
        ) { fcmToken, endpointConfig ->
            DebugSettings(fcmToken, endpointConfig, endpointConfigSelection = endpointConfig.ordinal)
        }

    fun updateEndpointConfig(endpointConfig: EndpointConfig): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.putEndpointConfig(endpointConfig.ordinal)
        }.andThen(
            logoutManager.logoutCompletable {
//                LoginManager.getInstance().logOut()
                VK.logout()
            }
        )

    private fun getFirebaseToken(): Single<String> =
        Single.create<String> { emitter ->
            firebaseMessaging
                .token
                .addOnSuccessListener { result ->
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(result ?: FCM_TOKEN_UNAVAILABLE)
                    }
                }
                .addOnFailureListener { error ->
                    if (!emitter.isDisposed) {
                        emitter.onError(error)
                    }
                }
        }
            .timeout(FCM_TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .onErrorReturnItem(FCM_TOKEN_UNAVAILABLE)

    private fun getEndpointConfig(): Single<EndpointConfig> =
        Single.fromCallable {
            EndpointConfig.values()[sharedPreferenceHelper.endpointConfig]
        }
}