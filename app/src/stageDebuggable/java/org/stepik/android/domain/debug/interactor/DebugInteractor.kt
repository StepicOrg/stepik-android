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
import javax.inject.Inject

class DebugInteractor
@Inject
constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val logoutManager: StepikLogoutManager
) {

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
        Single.create { emitter ->
            firebaseMessaging
                .token
                .addOnSuccessListener { result -> emitter.onSuccess(result) }
                .addOnFailureListener(emitter::onError)
        }

    private fun getEndpointConfig(): Single<EndpointConfig> =
        Single.fromCallable {
            EndpointConfig.values()[sharedPreferenceHelper.endpointConfig]
        }
}