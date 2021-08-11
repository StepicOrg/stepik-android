package org.stepik.android.domain.debug.interactor

import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import javax.inject.Inject

class DebugInteractor
@Inject
constructor(
    private val firebaseInstanceId: FirebaseInstanceId
) {
    fun getFirebaseToken(): Single<String> =
        Single.create { emitter ->
            firebaseInstanceId
                .instanceId
                .addOnSuccessListener { instanceIdResult -> emitter.onSuccess(instanceIdResult.token) }
                .addOnFailureListener(emitter::onError)
        }
}