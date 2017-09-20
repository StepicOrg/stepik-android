package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.common.api.Scope
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.model.AuthData
import timber.log.Timber

abstract class SmartLockActivityBase : FragmentActivityBase() {
    companion object {
        private const val resolvingAccountKey = "resolvingAccountKey"
        private const val requestFromSmartLockCode = 314
        private const val RC_SAVE = 356
    }

    private var resolvingWasShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolvingWasShown = savedInstanceState?.getBoolean(resolvingAccountKey) ?: false
    }

    protected var googleApiClient: GoogleApiClient? = null

    protected fun initGoogleApiClient(withAuth: Boolean = false, autoManage: OnConnectionFailedListener = OnConnectionFailedListener {}) {
        if (checkPlayServices()) {
            val builder = GoogleApiClient.Builder(this)
                    .enableAutoManage(this, autoManage)
                    .addApi(Auth.CREDENTIALS_API)

            if (withAuth) {
                val serverClientId = config.googleServerClientId
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Scope(Scopes.EMAIL), Scope(Scopes.PROFILE))
                        .requestServerAuthCode(serverClientId)
                        .build()
                builder.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            }

            googleApiClient = builder.build()
        }
    }

    protected fun requestCredentials() {
        val credentialRequest = CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build()

        Auth.CredentialsApi.request(googleApiClient, credentialRequest).setResultCallback { credentialRequestResult ->
            if (credentialRequestResult.status.isSuccess) {
                // Successfully read the credential without any user interaction, this
                // means there was only a single credential and the user has auto
                // sign-in enabled.
                analytic.reportEvent(Analytic.SmartLock.READ_CREDENTIAL_WITHOUT_INTERACTION)
                onCredentialRetrieved(credentialRequestResult.credential)
            } else {
                if (credentialRequestResult.status.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    // Prompt the user to choose a saved credential; do not show the hint
                    // selector.
                    try {
                        if (!resolvingWasShown) {
                            analytic.reportEvent(Analytic.SmartLock.PROMPT_TO_CHOOSE_CREDENTIALS)
                            resolvingWasShown = true
                            credentialRequestResult.status.startResolutionForResult(this, requestFromSmartLockCode)
                        }
                    } catch (e: IntentSender.SendIntentException) {
                        Timber.e(e, "STATUS: Failed to send resolution.")
                    }

                } else {
                    Timber.d("STATUS: Failed to send resolution.")
                    // The user must create an account or sign in manually.
                }
            }
        }
    }

    protected fun requestToSaveCredentials(authData: AuthData) {
        val credential = Credential
                .Builder(authData.login)
                .setPassword(authData.password)
                .build()

        Auth.CredentialsApi.save(googleApiClient, credential)
                .setResultCallback { status ->
                    if (!status.isSuccess && status.hasResolution()) {
                        analytic.reportEvent(Analytic.SmartLock.SHOW_SAVE_LOGIN)
                        status.startResolutionForResult(this, RC_SAVE)
                    } else {
                        analytic.reportEventWithName(Analytic.SmartLock.DISABLED_LOGIN, status.statusMessage)
                        onCredentialSaved()
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            requestFromSmartLockCode -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    analytic.reportEvent(Analytic.SmartLock.LAUNCH_CREDENTIAL_RETRIEVED_PROMPT)
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    onCredentialRetrieved(credential)
                } else {
                    analytic.reportEvent(Analytic.SmartLock.LAUNCH_CREDENTIAL_CANCELED_PROMPT)
                    Timber.d("Credential Read not ok: canceled or no internet")
                }
            }

            RC_SAVE -> {
                if (resultCode == RESULT_OK) {
                    analytic.reportEvent(Analytic.SmartLock.
                            LOGIN_SAVED)
                } else {
                    analytic.reportEvent(Analytic.SmartLock.LOGIN_NOT_SAVED)
                }
                onCredentialSaved()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(resolvingAccountKey, resolvingWasShown)
        super.onSaveInstanceState(outState)
    }

    protected fun signOutFromGoogle() {
        if (googleApiClient?.isConnected == true) {
            Auth.GoogleSignInApi.signOut(googleApiClient)
        }
    }

    protected open fun onCredentialRetrieved(credential: Credential) {}
    protected open fun onCredentialSaved() {}
}