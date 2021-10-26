package org.stepik.android.view.debug.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.experiments.SplitTest
import org.stepic.droid.base.App
import org.stepic.droid.databinding.DialogSplitGroupsBinding
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.SplitGroupData
import org.stepik.android.presentation.debug.SplitGroupFeature
import org.stepik.android.presentation.debug.SplitGroupViewModel
import org.stepik.android.view.debug.ui.adapter.delegate.SplitGroupAdapterDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class SplitGroupsDialogFragment : DialogFragment(R.layout.dialog_split_groups), ReduxView<SplitGroupFeature.State, SplitGroupFeature.Action.ViewAction> {
    companion object {
        const val TAG = "SplitGroupsDialogFragment"

        fun newInstance(): DialogFragment =
            SplitGroupsDialogFragment()
    }

    @Inject
    lateinit var splitTestGroups: Set<@JvmSuppressWildcards SplitTest<*>>

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val splitGroupViewModel: SplitGroupViewModel by reduxViewModel(this) { viewModelFactory }

    private val splitTestGroupsAdapter: DefaultDelegateAdapter<SplitGroupData> = DefaultDelegateAdapter()

    private val splitGroupsBinding: DialogSplitGroupsBinding by viewBinding(DialogSplitGroupsBinding::bind)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        splitGroupsBinding.appBarLayoutBinding.viewCenteredToolbarBinding.centeredToolbarTitle.setText(R.string.debug_ab_group_subtitle)
        splitGroupViewModel.onNewMessage(SplitGroupFeature.Message.InitMessage(splitTestGroups))
        splitTestGroupsAdapter += SplitGroupAdapterDelegate{ splitTestName, splitTestGroupName, groups ->
            val chosenPosition = groups.indexOf(splitTestGroupName)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.debug_choose_ab_test)
                .setSingleChoiceItems(groups.toTypedArray(), chosenPosition) { dialog, which ->
                    val updatedSplitGroupData = SplitGroupData(
                        splitTestName = splitTestName,
                        splitTestValue = groups[which],
                        splitTestGroups = groups
                    )
                    splitGroupViewModel.onNewMessage(SplitGroupFeature.Message.ChosenGroup(updatedSplitGroupData))
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.debug_alert_dialog_cancel) { _, _ -> }
                .show()
        }

        with(splitGroupsBinding.splitGroupsRecycler) {
            adapter = splitTestGroupsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
        }
    }

    private fun injectComponent() {
        App.component().splitGroupsComponentBuilder().build().inject(this)
    }

    override fun onAction(action: SplitGroupFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: SplitGroupFeature.State) {
        if (state is SplitGroupFeature.State.Content) {
            splitTestGroupsAdapter.items = state.splitGroupDataList
        }
    }
}