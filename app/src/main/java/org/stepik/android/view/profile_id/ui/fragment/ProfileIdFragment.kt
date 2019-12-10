package org.stepik.android.view.profile_id.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.view_profile_user_id.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.util.copyTextToClipboard
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.profile_id.ProfileIdPresenter
import org.stepik.android.presentation.profile_id.ProfileIdView
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class ProfileIdFragment : Fragment(), ProfileIdView {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileIdFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var userId by argument<Long>()

    private lateinit var profileIdPresenter: ProfileIdPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileIdPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileIdPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.view_profile_user_id, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.isVisible = false
        profileUserId.setOnLongClickListener {
            val textToCopy = (it as AppCompatTextView).text.toString()
            requireContext().copyTextToClipboard(textToCopy = textToCopy, toastMessage = getString(R.string.copied_to_clipboard_toast))
            true
        }
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        profileIdPresenter.attachView(this)
    }

    override fun onStop() {
        profileIdPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(profileData: ProfileData?) {
        val userId = profileData?.user?.id

        if (profileData?.isCurrentUser == true && userId != null) {
            view?.isVisible = true
            profileUserId.text = getString(R.string.profile_user_id, userId)
        } else {
            view?.isVisible = false
        }
    }
}