package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.GridLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.GoogleApiClient
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import kotlinx.android.synthetic.main.activity_launch.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.ProgressHandler
import org.stepic.droid.core.presenters.LoginPresenter
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.model.Credentials
import org.stepic.droid.social.ISocialType
import org.stepic.droid.social.SocialManager
import org.stepic.droid.ui.adapters.SocialAuthAdapter
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.getMessageFor
import org.stepic.droid.web.Api
import org.stepik.android.view.base.ui.span.TypefaceSpanCompat
import javax.inject.Inject


class LaunchActivity : SmartLockActivityBase(), LoginView {
    companion object {
        private const val TAG = "LaunchActivity"
        const val WAS_LOGOUT_KEY = "wasLogoutKey"
        private const val SOCIAL_ADAPTER_STATE_KEY = "socialAdapterStateKey"
    }


    private var progressLogin: LoadingProgressDialog? = null
    private lateinit var progressHandler: ProgressHandler
    private lateinit var callbackManager: CallbackManager

    @Inject
    lateinit var loginPresenter: LoginPresenter

    @Inject
    internal lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        App.componentManager().loginComponent(TAG).inject(this)
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)

        launchSignUpButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_UP)
            screenManager.showRegistration(this@LaunchActivity, courseFromExtra)
        }

        signInWithEmail.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN)
            screenManager.showLogin(this@LaunchActivity, courseFromExtra, null)
        }

        initGoogleApiClient(true, GoogleApiClient.OnConnectionFailedListener {
            Toast.makeText(this@LaunchActivity, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
        })

        val recyclerState = savedInstanceState?.getSerializable(SOCIAL_ADAPTER_STATE_KEY)
        if (recyclerState is SocialAuthAdapter.State) {
            initSocialRecycler(recyclerState)
        } else {
            initSocialRecycler()
        }

        val signInString = getString(R.string.sign_in)
        val signInWithSocial = getString(R.string.sign_in_with_social_suffix)

        val spannableSignIn = SpannableString(signInString + signInWithSocial)
        val typefaceSpan = TypefaceSpanCompat(ResourcesCompat.getFont(this, R.font.roboto_medium))

        spannableSignIn.setSpan(typefaceSpan, 0, signInString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signInText.text = spannableSignIn

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
                analytic.reportError(Analytic.Login.FACEBOOK_ERROR, exception)
                onInternetProblems()
            }
        })

        progressLogin = LoadingProgressDialog(this)

        loginPresenter.attachView(this)

        if (checkPlayServices()) {
            googleApiClient?.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {
                    val wasLogout = intent?.getBooleanExtra(WAS_LOGOUT_KEY, false) ?: false
                    if (wasLogout) {
                        Auth.CredentialsApi.disableAutoSignIn(googleApiClient)
                    }

                    requestCredentials()
                }

                override fun onConnectionSuspended(cause: Int) {
                }
            })
        }

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data != null) {
            redirectFromSocial(intent)
        }
    }

    private fun initSocialRecycler(state: SocialAuthAdapter.State = SocialAuthAdapter.State.NORMAL) {
        socialListRecyclerView.layoutManager = GridLayoutManager(this, 3)

        socialListRecyclerView.itemAnimator = FadeInDownAnimator()
            .apply {
                removeDuration = 0
            }

        val adapter = SocialAuthAdapter(this::onSocialItemClicked, state)
        showMore.setOnClickListener {
            showMore.visibility = View.GONE
            showLess.visibility = View.VISIBLE
            adapter.showMore()
        }

        showLess.setOnClickListener {
            showLess.visibility = View.GONE
            showMore.visibility = View.VISIBLE
            adapter.showLess()
        }

        when (state) {
            SocialAuthAdapter.State.EXPANDED -> {
                showLess.visibility = View.VISIBLE
                showMore.visibility = View.GONE
            }
            SocialAuthAdapter.State.NORMAL -> {
                showMore.visibility = View.VISIBLE
                showLess.visibility = View.GONE
            }
        }

        socialListRecyclerView.adapter = adapter
    }

    private fun onSocialItemClicked(type: ISocialType) {
        analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_SOCIAL, type.identifier)
        when(type) {
            SocialManager.SocialType.google -> {
                if (googleApiClient == null) {
                    analytic.reportEvent(Analytic.Interaction.GOOGLE_SOCIAL_IS_NOT_ENABLED)
                    Toast.makeText(this, R.string.google_services_late, Toast.LENGTH_SHORT).show()
                } else {
                    val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
                    startActivityForResult(signInIntent, AppConstants.REQUEST_CODE_GOOGLE_SIGN_IN)
                }
            }

            SocialManager.SocialType.facebook ->
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

            SocialManager.SocialType.vk ->
                VKSdk.login(this, VKScope.EMAIL)

            else -> {
                loginPresenter.onClickAuthWithSocialProviderWithoutSDK(type)
                api.loginWithSocial(this, type)
            }
        }
    }

    override fun onDestroy() {
        loginPresenter.detachView(this)
        signInWithEmail.setOnClickListener(null)
        launchSignUpButton.setOnClickListener(null)
        showMore.setOnClickListener(null)
        showLess.setOnClickListener(null)
        if (isFinishing) {
            App.componentManager().releaseLoginComponent(TAG)
        }
        super.onDestroy()
    }

    private fun redirectFromSocial(intent: Intent) {
        try {
            val code = intent.data?.getQueryParameter("code") ?: ""
            loginPresenter.loginWithCode(code)
        } catch (t: Throwable) {
            analytic.reportError(Analytic.Error.CALLBACK_SOCIAL, t)
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)
    }

    override fun onCredentialRetrieved(credential: Credential) {
        val accountType = credential.accountType
        if (accountType == null) {
            // Sign the user in with information from the Credential.
            loginPresenter.login(credential.id, credential.password ?: "", credential)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(result: VKAccessToken) {
                loginPresenter.loginWithNativeProviderCode(result.accessToken, SocialManager.SocialType.vk, result.email)
            }

            override fun onError(error: VKError?) {
                if (error?.errorCode == VKError.VK_REQUEST_HTTP_FAILED) {
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
                val authCode = result.signInAccount?.serverAuthCode
                if (authCode == null) {
                    analytic.reportEvent(Analytic.Login.GOOGLE_AUTH_CODE_NULL)
                    onInternetProblems()
                    return
                }

                loginPresenter.loginWithNativeProviderCode(authCode, SocialManager.SocialType.google)
            } else {
                // check statusCode here https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes
                val statusCode = result?.status?.statusCode?.toString() ?: "was null"
                analytic.reportEvent(Analytic.Login.GOOGLE_FAILED_STATUS, statusCode)
                onInternetProblems()
            }
        }
    }

    private fun onInternetProblems() {
        Toast.makeText(applicationContext, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        val fromMainFeed = intent?.extras?.getBoolean(AppConstants.FROM_MAIN_FEED_FLAG) ?: false
        val index = intent?.extras?.getInt(MainFeedActivity.CURRENT_INDEX_KEY) ?: MainFeedActivity.defaultIndex

        if (fromMainFeed) {
            screenManager.showMainFeed(this, index)
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
        if (googleApiClient?.isConnected ?: false) {
            Auth.CredentialsApi.delete(googleApiClient, credential).setResultCallback { status ->
                if (status.isSuccess) {
                    analytic.reportEvent(Analytic.SmartLock.CREDENTIAL_DELETED_SUCCESSFUL)
                    //do not show some message because E-mail is not correct was already shown
                } else {
                    analytic.reportEventWithName(Analytic.SmartLock.CREDENTIAL_DELETED_FAIL, status.statusMessage)
                }
            }
        }
    }

    override fun onSuccessLogin(credentials: Credentials?) {
        progressHandler.dismiss()
        openMainFeed()
    }

    override fun onSocialLoginWithExistingEmail(email: String) {
        screenManager.showLogin(this, courseFromExtra, email)
    }

    private fun openMainFeed() {
        screenManager.showMainFeedAfterLogin(this, courseFromExtra)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        val adapter = socialListRecyclerView.adapter
        if (adapter is SocialAuthAdapter) {
            outState?.putSerializable(SOCIAL_ADAPTER_STATE_KEY, adapter.state)
        }
        super.onSaveInstanceState(outState)
    }

}
