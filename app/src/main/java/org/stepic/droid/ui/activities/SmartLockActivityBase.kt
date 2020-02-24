package org.stepic.droid.ui.activities

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
import org.stepic.droid.model.Credentials
import timber.log.Timber

abstract class SmartLockActivityBase : FragmentActivityBase() {
    companion object {
        private const val RESOLVING_ACCOUNT_KEY = "RESOLVING_ACCOUNT_KEY"
        private const val REQUEST_FROM_SMART_LOCK_CODE = 314
        private const val REQUEST_SAVE_TO_SMART_LOCK_CODE = 356

        private fun Credential.toCredentials(): Credentials =
            Credentials(id, password ?: "")

        private fun Credentials.toCredential(): Credential =
            Credential
                .Builder(login)
                .setPassword(password)
                .build()
    }

    private var resolvingWasShown = false

    protected var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolvingWasShown = savedInstanceState?.getBoolean(RESOLVING_ACCOUNT_KEY) ?: false
    }

    protected fun initGoogleApiClient(withAuth: Boolean = false, autoManage: OnConnectionFailedListener? = null) {
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
                onCredentialsRetrieved(credentialRequestResult.credential.toCredentials())
            } else {
                if (credentialRequestResult.status.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                    // Prompt the user to choose a saved credential; do not show the hint
                    // selector.
                    try {
                        if (!resolvingWasShown) {
                            analytic.reportEvent(Analytic.SmartLock.PROMPT_TO_CHOOSE_CREDENTIALS)
                            resolvingWasShown = true
                            credentialRequestResult.status.startResolutionForResult(this, REQUEST_FROM_SMART_LOCK_CODE)
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

    protected fun requestToSaveCredentials(credentials: Credentials) {
        Auth.CredentialsApi.save(googleApiClient, credentials.toCredential())
            .setResultCallback { status ->
                if (!status.isSuccess && status.hasResolution()) {
                    analytic.reportEvent(Analytic.SmartLock.SHOW_SAVE_LOGIN)
                    status.startResolutionForResult(this, REQUEST_SAVE_TO_SMART_LOCK_CODE)
                } else {
                    analytic.reportEventWithName(Analytic.SmartLock.DISABLED_LOGIN, status.statusMessage)
                    onCredentialsSaved()
                }
            }
    }

    protected fun requestToDeleteCredentials(credentials: Credentials) {
        if (googleApiClient?.isConnected == true) {
            Auth.CredentialsApi.delete(googleApiClient, credentials.toCredential()).setResultCallback { status ->
                if (status.isSuccess) {
                    analytic.reportEvent(Analytic.SmartLock.CREDENTIAL_DELETED_SUCCESSFUL)
                    //do not show some message because E-mail is not correct was already shown
                } else {
                    analytic.reportEventWithName(Analytic.SmartLock.CREDENTIAL_DELETED_FAIL, status.statusMessage)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_FROM_SMART_LOCK_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    analytic.reportEvent(Analytic.SmartLock.PROMPT_CREDENTIAL_RETRIEVED)
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    onCredentialsRetrieved(credential.toCredentials())
                }
            }

            REQUEST_SAVE_TO_SMART_LOCK_CODE -> {
                if (resultCode == RESULT_OK) {
                    analytic.reportEvent(Analytic.SmartLock.LOGIN_SAVED)
                }
                onCredentialsSaved()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(RESOLVING_ACCOUNT_KEY, resolvingWasShown)
        super.onSaveInstanceState(outState)
    }

    protected fun signOutFromGoogle() {
        if (googleApiClient?.isConnected == true) {
            Auth.GoogleSignInApi.signOut(googleApiClient)
        }
    }

    protected open fun onCredentialsRetrieved(credentials: Credentials) {}
    protected open fun onCredentialsSaved() {}
}