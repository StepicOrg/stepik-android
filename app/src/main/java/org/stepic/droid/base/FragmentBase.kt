package org.stepic.droid.base

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.ShareHelper
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.ui.util.CloseIconHolder
import org.stepic.droid.util.resolvers.text.TextResolver
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

open class FragmentBase : Fragment() {

    private var viewHasBeenDestroyed = false
    private var unbinder: Unbinder? = null

    @Inject
    lateinit var textResolver: TextResolver

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    @Inject
    lateinit var databaseFacade: DatabaseFacade

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var mainHandler: MainHandler

    @Inject
    lateinit var systemDownloadManager: DownloadManager

    protected open fun injectComponent() {
        App.component().inject(this)
    }

    /**
     * optional method for releasing components
     * mirror of `injectComponent()`
     */
    protected open fun onReleaseComponent() {}

    protected fun hideSoftKeypad() {
        val activity = requireActivity()
        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //in Kotlin, for example, butterknife is not used
        unbinder?.unbind()
        viewHasBeenDestroyed = true
    }

    override fun onDestroy() {
        super.onDestroy()
        onReleaseComponent()
        App.refWatcher.watch(this)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val shouldNotBeenDestroyed = viewHasBeenDestroyed && enter
        viewHasBeenDestroyed = false

        //do not animate fragment on rotation via setCustomAnimations
        return if (shouldNotBeenDestroyed)
            object : Animation() {}
        else
            super.onCreateAnimation(transit, enter, nextAnim)
    }

    @DrawableRes
    protected fun getCloseIconDrawableRes(): Int =
            CloseIconHolder.getCloseIconDrawableRes()

    /**
     * Background of activity can be nullified for avoiding overdrawing.
     *
     * Background of fragment's root view should be initialized in fragment or xml
     *
     * Colors in theme and fragments without background is not used, because toolbars and other element (not
     * root) will be always overdrawn.
     *
     * Null is not used at Theme because of black background on pre-loading activity.
     */
    protected fun nullifyActivityBackground() {
        activity?.window?.decorView?.background = null
    }

    protected open fun getRootView(): ViewGroup? = null
}