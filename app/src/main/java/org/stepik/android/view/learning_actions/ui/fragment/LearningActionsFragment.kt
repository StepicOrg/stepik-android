package org.stepik.android.view.learning_actions.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_learning_actions.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepik.android.presentation.learning_actions.LearningActionsFeature
import org.stepik.android.presentation.learning_actions.LearningActionsViewModel
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.presentation.wishlist.WishlistFeature
import org.stepik.android.view.learning_actions.model.LearningActionsItem
import org.stepik.android.view.learning_actions.ui.adapter.delegate.UserReviewsActionAdapterDelegate
import org.stepik.android.view.learning_actions.ui.adapter.delegate.WishlistActionAdapterDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class LearningActionsFragment :
    Fragment(),
    ReduxView<LearningActionsFeature.State, LearningActionsFeature.Action.ViewAction> {

    companion object {
        const val TAG = "LearningActionsFragment"

        fun newInstance(): Fragment =
            LearningActionsFragment()
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val learningActionsViewModel: LearningActionsViewModel by reduxViewModel(this) { viewModelFactory }

    private val learningActionsItemAdapter: DefaultDelegateAdapter<LearningActionsItem> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        learningActionsViewModel.onNewMessage(LearningActionsFeature.Message.WishlistMessage(WishlistFeature.Message.InitMessage(forceUpdate = false)))
        learningActionsViewModel.onNewMessage(LearningActionsFeature.Message.UserReviewsMessage(UserReviewsFeature.Message.InitMessage(forceUpdate = false)))
    }

    private fun injectComponent() {
        App.componentManager()
            .learningActionsComponent()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_learning_actions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        learningActionsItemAdapter += WishlistActionAdapterDelegate { screenManager.showWishlist(requireContext()) }
        learningActionsItemAdapter += UserReviewsActionAdapterDelegate { screenManager.showUserReviews(requireContext()) }
        with(learningActionsRecycler) {
            adapter = learningActionsItemAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = null
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    override fun onAction(action: LearningActionsFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: LearningActionsFeature.State) {

        learningActionsItemAdapter.items =
            listOf(
                LearningActionsItem.Wishlist(state.wishlistState),
                LearningActionsItem.UserReviews(state.userReviewsState)
            )
    }
}