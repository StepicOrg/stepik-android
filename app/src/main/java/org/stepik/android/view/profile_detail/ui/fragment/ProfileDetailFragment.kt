package org.stepik.android.view.profile_detail.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_profile_detail.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.util.collapse
import org.stepic.droid.ui.util.expand
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.profile_detail.ProfileDetailPresenter
import org.stepik.android.presentation.profile_detail.ProfileDetailView
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class ProfileDetailFragment : Fragment(R.layout.fragment_profile_detail), ProfileDetailView {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileDetailFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var userId by argument<Long>()

    private lateinit var profileDetailPresenter: ProfileDetailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileDetailPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileDetailPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        TextViewCompat.setLineHeight(profileDetails.textView, resources.getDimensionPixelOffset(R.dimen.comment_item_text_line))

        profileDetailsTitleArrow.changeState()
        profileDetailsTitle.setOnClickListener {
            profileDetailsTitleArrow.changeState()
            val isExpanded = profileDetailsTitleArrow.isExpanded()
            if (isExpanded) {
                profileDetails.expand()
            } else {
                profileDetails.collapse()
            }
        }

        view.isVisible = false
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        profileDetailPresenter.attachView(this)
    }

    override fun onStop() {
        profileDetailPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(profileData: ProfileData?) {
        val details = profileData?.user?.details

        if (details.isNullOrBlank()) {
            view?.isVisible = false
        } else {
            profileDetails.setText(details)
            view?.isVisible = true
        }
    }
}