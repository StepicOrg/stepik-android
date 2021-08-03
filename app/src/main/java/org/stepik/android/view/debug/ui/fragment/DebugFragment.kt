package org.stepik.android.view.debug.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.debug.DebugFeature
import org.stepik.android.presentation.debug.DebugViewModel
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import timber.log.Timber
import javax.inject.Inject

class DebugFragment : Fragment(R.layout.fragment_debug), ReduxView<DebugFeature.State, DebugFeature.Action.ViewAction> {
    companion object {
        const val TAG = "DebugFragment"

        fun newInstance(): Fragment =
            DebugFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val debugViewModel: DebugViewModel by reduxViewModel(this) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        debugViewModel.onNewMessage(DebugFeature.Message.InitMessage())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        centeredToolbarTitle.setText(R.string.debug_toolbar_title)
    }

    private fun injectComponent() {
        App.component()
            .debugComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onAction(action: DebugFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: DebugFeature.State) {
        Timber.d("State: $state")
    }
}