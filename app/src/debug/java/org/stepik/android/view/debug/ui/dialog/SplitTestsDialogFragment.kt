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
import org.stepic.droid.databinding.DialogSplitTestsBinding
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.SplitTestData
import org.stepik.android.presentation.debug.SplitTestsFeature
import org.stepik.android.presentation.debug.SplitTestsViewModel
import org.stepik.android.view.debug.ui.adapter.delegate.SplitTestDataAdapterDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class SplitTestsDialogFragment : DialogFragment(R.layout.dialog_split_tests), ReduxView<SplitTestsFeature.State, SplitTestsFeature.Action.ViewAction> {
    companion object {
        const val TAG = "SplitTestsDialogFragment"

        fun newInstance(): DialogFragment =
            SplitTestsDialogFragment()
    }

    @Inject
    lateinit var splitTests: Set<@JvmSuppressWildcards SplitTest<*>>


    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val splitTestsViewModel: SplitTestsViewModel by reduxViewModel(this) { viewModelFactory }

    private val splitTestGroupsAdapter: DefaultDelegateAdapter<SplitTestData> = DefaultDelegateAdapter()

    private val splitTestsBinding: DialogSplitTestsBinding by viewBinding(DialogSplitTestsBinding::bind)

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
        splitTestsBinding.appBarLayoutBinding.viewCenteredToolbarBinding.centeredToolbarTitle.setText(R.string.debug_ab_group_subtitle)
        splitTestsViewModel.onNewMessage(SplitTestsFeature.Message.InitMessage(splitTests))
        splitTestGroupsAdapter += SplitTestDataAdapterDelegate{ splitTestName, splitTestGroupName, groups ->
            val chosenPosition = groups.indexOf(splitTestGroupName)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.debug_choose_ab_test)
                .setSingleChoiceItems(groups.toTypedArray(), chosenPosition) { dialog, which ->
                    val updatedSplitTestData = SplitTestData(
                        splitTestName = splitTestName,
                        splitTestValue = groups[which],
                        splitTestGroups = groups
                    )
                    splitTestsViewModel.onNewMessage(SplitTestsFeature.Message.ChosenGroup(updatedSplitTestData))
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.debug_alert_dialog_cancel) { _, _ -> }
                .show()
        }

        with(splitTestsBinding.splitTestsRecycler) {
            adapter = splitTestGroupsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
        }
    }

    private fun injectComponent() {
        App.component().splitTestsComponentBuilder().build().inject(this)
    }

    override fun onAction(action: SplitTestsFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: SplitTestsFeature.State) {
        if (state is SplitTestsFeature.State.Content) {
            splitTestGroupsAdapter.items = state.splitTestDataList
        }
    }
}