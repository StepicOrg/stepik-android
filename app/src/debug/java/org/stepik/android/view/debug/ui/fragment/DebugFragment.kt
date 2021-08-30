package org.stepik.android.view.debug.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.util.copyTextToClipboard
import org.stepik.android.presentation.debug.DebugFeature
import org.stepik.android.presentation.debug.DebugViewModel
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject
import android.content.Intent
import android.content.Context
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.databinding.FragmentDebugBinding

class DebugFragment : Fragment(R.layout.fragment_debug), ReduxView<DebugFeature.State, DebugFeature.Action.ViewAction> {
    companion object {
        fun newInstance(): Fragment =
            DebugFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val debugViewModel: DebugViewModel by reduxViewModel(this) { viewModelFactory }

    private val viewStateDelegate = ViewStateDelegate<DebugFeature.State>()

    private val debugBinding: FragmentDebugBinding by viewBinding(FragmentDebugBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        debugBinding.appBarLayout.viewCenteredToolbar.centeredToolbarTitle.setText(R.string.debug_toolbar_title)
        initViewStateDelegate()
        debugViewModel.onNewMessage(DebugFeature.Message.InitMessage())

        debugBinding.debugFcmTokenValue.setOnLongClickListener {
            val textToCopy = (it as AppCompatTextView).text.toString()
            requireContext().copyTextToClipboard(
                textToCopy = textToCopy,
                toastMessage = getString(R.string.copied_to_clipboard_toast)
            )
            true
        }

        debugBinding.debugEndpointRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            val position = group.indexOfChild(checkedRadioButton)
            debugViewModel.onNewMessage(DebugFeature.Message.RadioButtonSelectionMessage(position))
        }

        debugBinding.debugApplySettingsAction.setOnClickListener {
            debugViewModel.onNewMessage(DebugFeature.Message.ApplySettingsMessage)
        }

        debugBinding.debugLoadingError.tryAgain.setOnClickListener {
            debugViewModel.onNewMessage(DebugFeature.Message.InitMessage(forceUpdate = true))
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
        viewStateDelegate.addState<DebugFeature.State.Loading>(debugBinding.debugProgressBar.loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<DebugFeature.State.Error>(debugBinding.debugLoadingError.errorNoConnection)
        viewStateDelegate.addState<DebugFeature.State.Content>(debugBinding.debugContent)
    }

    override fun onAction(action: DebugFeature.Action.ViewAction) {
        if (action is DebugFeature.Action.ViewAction.RestartApplication) {
            Toast.makeText(requireContext(), R.string.debug_restarting_message, Toast.LENGTH_SHORT).show()
            view?.postDelayed({ triggerApplicationRestart(requireContext()) }, 1500)
        }
    }

    override fun render(state: DebugFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is DebugFeature.State.Content) {
            debugBinding.debugFcmTokenValue.text = state.fcmToken
            setRadioButtonSelection(state.endpointConfigSelection)
            debugBinding.debugApplySettingsAction.isVisible = state.currentEndpointConfig.ordinal != state.endpointConfigSelection
        }
    }

    private fun triggerApplicationRestart(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    private fun setRadioButtonSelection(itemPosition: Int) {
        val targetRadioButton = debugBinding.debugEndpointRadioGroup[itemPosition] as RadioButton
        targetRadioButton.isChecked = true
    }
}