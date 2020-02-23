package org.stepik.android.view.auth.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
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
import org.stepic.droid.analytic.experiments.DeferredAuthSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.model.Credentials
import org.stepic.droid.social.ISocialType
import org.stepic.droid.social.SocialManager
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.activities.SmartLockActivityBase
import org.stepic.droid.ui.adapters.SocialAuthAdapter
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.getMessageFor
import org.stepik.android.presentation.auth.SocialAuthPresenter
import org.stepik.android.presentation.auth.SocialAuthView
import org.stepik.android.view.base.ui.span.TypefaceSpanCompat
import javax.inject.Inject

class LaunchActivity : SmartLockActivityBase(), SocialAuthView {
    companion object {
        const val WAS_LOGOUT_KEY = "wasLogoutKey"

        private const val KEY_SOCIAL_ADAPTER_STATE = "social_adapter_state_key"
        private const val KEY_SELECTED_SOCIAL_TYPE = "selected_social_type"
    }

    @Inject
    lateinit var deferredAuthSplitTest: DeferredAuthSplitTest

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var socialAuthPresenter: SocialAuthPresenter

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private lateinit var callbackManager: CallbackManager

    private var selectedSocialType: ISocialType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        injectComponent()
        socialAuthPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(SocialAuthPresenter::class.java)

        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)

        dismissButton.setOnClickListener {
            onBackPressed()
        }

        dismissButton.isVisible = deferredAuthSplitTest.currentGroup.isDeferredAuth

        launchSignUpButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_UP)
            screenManager.showRegistration(this@LaunchActivity, courseFromExtra)
        }

        signInWithEmail.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN)
            screenManager.showLogin(this@LaunchActivity, null, null, false, courseFromExtra)
        }

        initGoogleApiClient(true, GoogleApiClient.OnConnectionFailedListener {
            showNetworkError()
        })

        val recyclerState = savedInstanceState?.getSerializable(KEY_SOCIAL_ADAPTER_STATE)
        if (recyclerState is SocialAuthAdapter.State) {
            initSocialRecycler(recyclerState)
        } else {
            initSocialRecycler()
        }

        selectedSocialType = savedInstanceState?.getSerializable(KEY_SELECTED_SOCIAL_TYPE) as? SocialManager.SocialType

        val signInString = getString(R.string.sign_in)
        val signInWithSocial = getString(R.string.sign_in_with_social_suffix)

        val spannableSignIn = SpannableString(signInString + signInWithSocial)
        val typefaceSpan = TypefaceSpanCompat(ResourcesCompat.getFont(this, R.font.roboto_medium))

        spannableSignIn.setSpan(typefaceSpan, 0, signInString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signInText.text = spannableSignIn

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                socialAuthPresenter
                    .authWithNativeCode(loginResult.accessToken.token, SocialManager.SocialType.facebook)
            }

            override fun onCancel() {}

            override fun onError(exception: FacebookException) {
                analytic.reportError(Analytic.Login.FACEBOOK_ERROR, exception)
                showNetworkError()
            }
        })

        if (checkPlayServices()) {
            googleApiClient?.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {
                    val wasLogout = intent?.getBooleanExtra(WAS_LOGOUT_KEY, false) ?: false
                    if (wasLogout) {
                        Auth.CredentialsApi.disableAutoSignIn(googleApiClient)
                    }

                    requestCredentials()
                }

                override fun onConnectionSuspended(cause: Int) {}
            })
        }

        onNewIntent(intent)
    }

    private fun injectComponent() {
        App.component()
            .authComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        socialAuthPresenter.attachView(this)
    }

    override fun onStop() {
        socialAuthPresenter.detachView(this)
        super.onStop()
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
                    root_view.snackbar(messageRes = R.string.google_services_late)
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
                selectedSocialType = type
                screenManager.loginWithSocial(this, type)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.data != null) {
            redirectFromSocial(intent)
        }
    }

    private fun redirectFromSocial(intent: Intent) {
        val code = intent.data?.getQueryParameter("code") ?: return
        val socialType = selectedSocialType ?: return
        socialAuthPresenter.authWithCode(code, socialType)
    }

    override fun onCredentialsRetrieved(credentials: Credentials) {
        screenManager.showLogin(this, credentials.login, credentials.password, true, courseFromExtra)
    }

    override fun onBackPressed() {
        val fromMainFeed = intent?.extras?.getBoolean(AppConstants.FROM_MAIN_FEED_FLAG) ?: false
        val index = intent?.extras?.getInt(MainFeedActivity.CURRENT_INDEX_KEY) ?: MainFeedActivity.defaultIndex

        when {
            fromMainFeed -> screenManager.showMainFeed(this, index)
            intent.hasExtra(AppConstants.KEY_COURSE_BUNDLE) -> super.onBackPressed()
            deferredAuthSplitTest.currentGroup.isDeferredAuth -> screenManager.showMainFeed(this,
                MainFeedActivity.CATALOG_INDEX
            )
            else -> super.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(result: VKAccessToken) {
                socialAuthPresenter
                    .authWithNativeCode(result.accessToken, SocialManager.SocialType.vk, result.email)
            }

            override fun onError(error: VKError?) {
                if (error?.errorCode == VKError.VK_REQUEST_HTTP_FAILED) {
                    showNetworkError()
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
                    showNetworkError()
                    return
                }

                socialAuthPresenter
                    .authWithNativeCode(authCode, SocialManager.SocialType.google)
            } else {
                // check statusCode here https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes
                val statusCode = result?.status?.statusCode?.toString() ?: "was null"
                analytic.reportEvent(Analytic.Login.GOOGLE_FAILED_STATUS, statusCode)
                showNetworkError()
            }
        }
    }

    override fun setState(state: SocialAuthView.State) {
        if (state is SocialAuthView.State.Loading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }

        when (state) {
            is SocialAuthView.State.Success ->
                screenManager.showMainFeedAfterLogin(this, courseFromExtra)
        }
    }

    override fun showAuthError(failType: LoginFailType) {
        root_view.snackbar(message = getMessageFor(failType))

        // logout from socials
        VKSdk.logout()
        if (googleApiClient?.isConnected == true) {
            Auth.GoogleSignInApi.signOut(googleApiClient)
        }
        //fb:
        LoginManager.getInstance().logOut()
    }

    override fun showNetworkError() {
        root_view.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun onSocialLoginWithExistingEmail(email: String) {
        screenManager.showLogin(this, email, null, false, courseFromExtra)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val adapter = socialListRecyclerView.adapter
        if (adapter is SocialAuthAdapter) {
            outState.putSerializable(KEY_SOCIAL_ADAPTER_STATE, adapter.state)
        }
        selectedSocialType?.let { outState.putSerializable(KEY_SELECTED_SOCIAL_TYPE, it) }
        super.onSaveInstanceState(outState)
    }
}
