package org.stepik.android.view.user_reviews.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.presentation.user_reviews.UserReviewsViewModel
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class UserReviewsFragment : Fragment(R.layout.fragment_user_reviews), ReduxView<UserReviewsFeature.State, UserReviewsFeature.Action.ViewAction> {

    companion object {
        fun newInstance(): Fragment =
            UserReviewsFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val userReviewsViewModel: UserReviewsViewModel by reduxViewModel(this) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    private fun injectComponent() {
        App.component()
            .userReviewsComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onAction(action: UserReviewsFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: UserReviewsFeature.State) {
        // no op
    }
}