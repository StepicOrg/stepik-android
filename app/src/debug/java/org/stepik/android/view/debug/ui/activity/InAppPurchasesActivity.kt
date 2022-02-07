package org.stepik.android.view.debug.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.android.billingclient.api.Purchase
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.databinding.ActivityInAppPurchasesBinding
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.presentation.debug.InAppPurchasesFeature
import org.stepik.android.presentation.debug.InAppPurchasesViewModel
import org.stepik.android.view.debug.ui.adapter.delegate.InAppPurchaseAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.presentation.redux.container.ReduxView
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class InAppPurchasesActivity : AppCompatActivity(), ReduxView<InAppPurchasesFeature.State, InAppPurchasesFeature.Action.ViewAction> {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, InAppPurchasesActivity::class.java)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private val inAppPurchasesViewModel: InAppPurchasesViewModel by reduxViewModel(this) { viewModelFactory }

    private val inAppPurchasesAdapter: DefaultDelegateAdapter<Purchase> = DefaultDelegateAdapter()

    private val inAppPurchasesBinding: ActivityInAppPurchasesBinding by viewBinding(ActivityInAppPurchasesBinding::bind)

    private val viewStateDelegate = ViewStateDelegate<InAppPurchasesFeature.State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_purchases)
        injectComponent()
        initViewStateDelegate()

        setSupportActionBar(inAppPurchasesBinding.appBarLayoutBinding.viewCenteredToolbarBinding.centeredToolbar)

        val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
        inAppPurchasesBinding.appBarLayoutBinding.viewCenteredToolbarBinding.centeredToolbarTitle.setText(R.string.debug_purchases_subtitle)

        inAppPurchasesAdapter += InAppPurchaseAdapterDelegate {
            inAppPurchasesViewModel.onNewMessage(InAppPurchasesFeature.Message.PurchaseClickedMessage(it))
        }
        with(inAppPurchasesBinding.inAppPurchasesRecycler) {
            adapter = inAppPurchasesAdapter
            layoutManager = LinearLayoutManager(this@InAppPurchasesActivity)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
        }

        inAppPurchasesBinding.inAppPurchasesConsumeAllAction.setOnClickListener {
            inAppPurchasesViewModel.onNewMessage(InAppPurchasesFeature.Message.ConsumeAllMessage)
        }

        inAppPurchasesViewModel.onNewMessage(InAppPurchasesFeature.Message.InitMessage())
        inAppPurchasesBinding.inAppPurchaseError.tryAgain.setOnClickListener {
            inAppPurchasesViewModel.onNewMessage(InAppPurchasesFeature.Message.InitMessage(forceUpdate = true))
        }
    }

    private fun injectComponent() {
        App.component()
            .inAppPurchasesComponentBuilder()
            .build()
            .inject(this)
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<InAppPurchasesFeature.State.Idle>()
        viewStateDelegate.addState<InAppPurchasesFeature.State.Loading>(inAppPurchasesBinding.inAppPurchaseProgressBar.loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<InAppPurchasesFeature.State.Empty>(inAppPurchasesBinding.inAppPurchaseEmpty)
        viewStateDelegate.addState<InAppPurchasesFeature.State.Error>(inAppPurchasesBinding.inAppPurchaseError.error)
        viewStateDelegate.addState<InAppPurchasesFeature.State.Content>(inAppPurchasesBinding.inAppPurchasesContentContainer)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAction(action: InAppPurchasesFeature.Action.ViewAction) {
        when (action) {
            is InAppPurchasesFeature.Action.ViewAction.ShowLoading ->
                ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)

            is InAppPurchasesFeature.Action.ViewAction.ShowConsumeSuccess -> {
                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
            }

            is InAppPurchasesFeature.Action.ViewAction.ShowConsumeFailure -> {
                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
            }
        }
    }

    override fun render(state: InAppPurchasesFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is InAppPurchasesFeature.State.Content) {
            inAppPurchasesAdapter.items = state.purchases
        }
    }
}