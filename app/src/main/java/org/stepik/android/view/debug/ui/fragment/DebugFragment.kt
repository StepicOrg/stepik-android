package org.stepik.android.view.debug.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_debug.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.util.copyTextToClipboard
import org.stepik.android.presentation.debug.DebugFeature
import org.stepik.android.presentation.debug.DebugViewModel
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class DebugFragment : Fragment(R.layout.fragment_debug),
    ReduxView<DebugFeature.State, DebugFeature.Action.ViewAction> {
    companion object {
        const val TAG = "DebugFragment"

        fun newInstance(): Fragment =
            DebugFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val debugViewModel: DebugViewModel by reduxViewModel(this) { viewModelFactory }

    private val viewStateDelegate = ViewStateDelegate<DebugFeature.State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        centeredToolbarTitle.setText(R.string.debug_toolbar_title)
        initViewStateDelegate()
        debugViewModel.onNewMessage(DebugFeature.Message.InitMessage())

        debugFcmTokenValue.setOnLongClickListener {
            val textToCopy = (it as AppCompatTextView).text.toString()
            requireContext().copyTextToClipboard(
                textToCopy = textToCopy,
                toastMessage = getString(R.string.copied_to_clipboard_toast)
            )
            true
        }
    }

    private fun injectComponent() {
        App.component()
            .debugComponentBuilder()
            .build()
            .inject(this)
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<DebugFeature.State.Idle>()
        viewStateDelegate.addState<DebugFeature.State.Loading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<DebugFeature.State.Error>()
        viewStateDelegate.addState<DebugFeature.State.Content>(debugContent)
    }

    override fun onAction(action: DebugFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: DebugFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is DebugFeature.State.Content) {
            debugFcmTokenValue.text = state.fcmToken
        }
    }
}