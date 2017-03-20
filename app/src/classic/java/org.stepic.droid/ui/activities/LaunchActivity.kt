package org.stepic.droid.ui.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.graphics.Point
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.method.LinkMovementMethod
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.activity_launch.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.ProgressHandler
import org.stepic.droid.core.presenters.LoginPresenter
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.model.AuthData
import org.stepic.droid.social.SocialManager
import org.stepic.droid.ui.adapters.SocialAuthAdapter
import org.stepic.droid.ui.decorators.SpacesItemDecorationHorizontal
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DpPixelsHelper
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.getMessageFor
import timber.log.Timber
import javax.inject.Inject


class LaunchActivity : BackToExitActivityBase(), LoginView {
    companion object {
        private val TAG = "LaunchActivity"
        val wasLogoutKey = "wasLogoutKey"
        private val resolvingAccountKey = "resolvingAccountKey"
    }

    private val requestFromSmartLockCode = 314

    val termsMessageHtml: String by lazy {
        resources.getString(R.string.terms_message_launch)
    }

    private var googleApiClient: GoogleApiClient? = null
    private var progressLogin: ProgressDialog? = null
    private lateinit var progressHandler: ProgressHandler
    private lateinit var callbackManager: CallbackManager
    private var resolvingWasShown = false

    @Inject
    lateinit var loginPresenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        window.setBackgroundDrawable(null)
        App.getComponentManager().loginComponent(TAG).inject(this)
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)

        resolvingWasShown = savedInstanceState?.getBoolean(resolvingAccountKey) ?: false

        findCoursesButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_FIND_COURSE_LAUNCH)
            shell.screenProvider.showFindCourses(this@LaunchActivity)
            this@LaunchActivity.finish()
        }

        launchSignUpButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_UP)
            shell.screenProvider.showRegistration(this@LaunchActivity, courseFromExtra)
        }

        signInWithEmail.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN)
            shell.screenProvider.showLogin(this@LaunchActivity, courseFromExtra)
        }


        if (checkPlayServices()) {
            val serverClientId = config.googleServerClientId
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Scope(Scopes.EMAIL), Scope(Scopes.PROFILE))
                    .requestServerAuthCode(serverClientId)
                    .build()
            googleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this) {
                        Toast.makeText(this@LaunchActivity, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
                    }
                    .addApi(Auth.CREDENTIALS_API)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build()
        }

        initSocialRecycler(googleApiClient)

        termsPrivacyLaunchTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyLaunchTextView.text = textResolver.fromHtml(termsMessageHtml)

        progressHandler = object : ProgressHandler {
            override fun activate() {
                hideSoftKeypad()
                ProgressHelper.activate(progressLogin)
            }

            override fun dismiss() {
                ProgressHelper.dismiss(progressLogin)
            }
        }

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(loginResult: LoginResult) {
                loginPresenter.loginWithNativeProviderCode(loginResult.accessToken.token,
                        SocialManager.SocialType.facebook)
            }

            override fun onCancel() {

            }

            override fun onError(exception: FacebookException) {
                onInternetProblems()
            }
        })

        progressLogin = LoadingProgressDialog(this)

        loginPresenter.attachView(this)

        val intent = intent
        if (intent.data != null) {
            redirectFromSocial(intent)
        }
        if (checkPlayServices()) {
            googleApiClient?.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {
                    val wasLogout = intent?.getBooleanExtra(wasLogoutKey, false) ?: false
                    if (wasLogout) {
                        Auth.CredentialsApi.disableAutoSignIn(googleApiClient)
                    }

                    requestCredentials()
                }

                override fun onConnectionSuspended(cause: Int) {
                }
            })
        }

    }

    private fun initSocialRecycler(googleApiClient: GoogleApiClient?) {
        val pixelForPadding = DpPixelsHelper.convertDpToPixel(4f, this)//pixelForPadding * (count+1)
        val widthOfItem = resources.getDimension(R.dimen.height_of_social)//width == height
        val count = SocialManager.SocialType.values().size
        val widthOfAllItems = widthOfItem * count + pixelForPadding * (count + 1)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val widthOfScreen = size.x


        socialListRecyclerView.addItemDecoration(SpacesItemDecorationHorizontal(pixelForPadding.toInt()))//30 is ok
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        if (widthOfScreen > widthOfAllItems) {
            val padding = (widthOfScreen - widthOfAllItems).toInt() / 2
            socialListRecyclerView.setPadding(padding, 0, 0, 0)
        }

        socialListRecyclerView.layoutManager = layoutManager
        socialListRecyclerView.adapter = SocialAuthAdapter(this, googleApiClient)
    }

    override fun onDestroy() {
        loginPresenter.detachView(this)
        signInWithEmail.setOnClickListener(null)
        launchSignUpButton.setOnClickListener(null)
        findCoursesButton.setOnClickListener(null)
        if (isFinishing) {
            App.getComponentManager().releaseLoginComponent(TAG)
        }
        super.onDestroy()
    }

    private fun redirectFromSocial(intent: Intent) {
        try {
            val code = intent.data.getQueryParameter("code")
            loginPresenter.loginWithCode(code)
        } catch (t: Throwable) {
            analytic.reportError(Analytic.Error.CALLBACK_SOCIAL, t)
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)
    }

    private fun requestCredentials() {
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

    private fun onCredentialRetrieved(credential: Credential) {
        val accountType = credential.accountType
        if (accountType == null) {
            // Sign the user in with information from the Credential.
            loginPresenter.login(credential.id, credential.password ?: "", credential)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == requestFromSmartLockCode) {
            if (resultCode == Activity.RESULT_OK) {
                analytic.reportEvent(Analytic.SmartLock.LAUNCH_CREDENTIAL_RETRIEVED_PROMPT)
                val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                onCredentialRetrieved(credential)
            } else {
                analytic.reportEvent(Analytic.SmartLock.LAUNCH_CREDENTIAL_CANCELED_PROMPT)
                Timber.d("Credential Read: NOT OK")
            }
        }

        if (VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken) {
                loginPresenter.loginWithNativeProviderCode(res.accessToken, SocialManager.SocialType.vk)
            }

            override fun onError(error: VKError) {
                if (error.errorCode == VKError.VK_REQUEST_HTTP_FAILED) {
                    onInternetProblems()
                }
            }
        })) {
            //vk will handle at callback
            return
        }

        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQUEST_CODE_GOOGLE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            // here is not only fail due to Internet, fix it. see: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInResult
            if (result.isSuccess) {
                val account = result.signInAccount
                if (account == null) {
                    onInternetProblems()
                    return
                }
                val authCode = account.serverAuthCode
                if (authCode == null) {
                    onInternetProblems()
                    return
                }

                loginPresenter.loginWithNativeProviderCode(authCode,
                        SocialManager.SocialType.google)
            } else {
                onInternetProblems()
            }
        }
    }

    private fun onInternetProblems() {
        Toast.makeText(applicationContext, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        val fromMainFeed = intent?.extras?.getBoolean(AppConstants.FROM_MAIN_FEED_FLAG) ?: false
        val index = intent?.extras?.getInt(MainFeedActivity.KEY_CURRENT_INDEX) ?: MainFeedActivity.DEFAULT_START_INDEX

        if (fromMainFeed) {
            shell.screenProvider.showMainFeed(this, index)
        } else {
            super.onBackPressed()
        }
    }

    override fun onLoadingWhileLogin() {
        progressHandler.activate()
    }

    override fun onFailLogin(type: LoginFailType, credential: Credential?) {
        progressHandler.dismiss()
        Toast.makeText(this, getMessageFor(type), Toast.LENGTH_SHORT).show()

        // logout from socials
        VKSdk.logout()
        if (googleApiClient?.isConnected ?: false) {
            Auth.GoogleSignInApi.signOut(googleApiClient)
        }
        //fb:
        LoginManager.getInstance().logOut()

        if (credential != null) {
            deleteCredential(credential)
        }
    }

    private fun deleteCredential(credential: Credential) {
        Auth.CredentialsApi.delete(googleApiClient,
                credential).setResultCallback { status ->
            if (status.isSuccess) {
                analytic.reportEvent(Analytic.SmartLock.CREDENTIAL_DELETED_SUCCESSFUL)
                //do not show some message because E-mail is not correct was already shown
            } else {
                analytic.reportEventWithName(Analytic.SmartLock.CREDENTIAL_DELETED_FAIL, status.statusMessage)
            }
        }
    }

    override fun onSuccessLogin(authData: AuthData?) {
        progressHandler.dismiss()
        openMainFeed()
    }

    private fun openMainFeed() {
        shell.screenProvider.showMainFeed(this, courseFromExtra)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(resolvingAccountKey, resolvingWasShown)
        super.onSaveInstanceState(outState)
    }

}
