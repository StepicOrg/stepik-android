package org.stepik.android.view.profile_links.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_profile_links.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepik.android.model.SocialProfile
import org.stepik.android.presentation.profile_links.ProfileLinksPresenter
import org.stepik.android.presentation.profile_links.ProfileLinksView
import org.stepik.android.view.profile_links.ui.delegate.ProfileLinksAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import timber.log.Timber
import javax.inject.Inject

class ProfileLinksFragment : Fragment(), ProfileLinksView {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileLinksFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var userId by argument<Long>()

    private lateinit var profileLinksPresenter: ProfileLinksPresenter

    private var profileLinksAdapter: DefaultDelegateAdapter<SocialProfile> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileLinksPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileLinksPresenter::class.java)

        profileLinksAdapter += ProfileLinksAdapterDelegate(
            onItemClick = { screenManager.openSocialMediaLink(requireContext(), it) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile_links, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.isVisible = false
        with(profileExternalLinksRecycler) {
            adapter = profileLinksAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        setDataToPresenter()
    }

    private fun setDataToPresenter() {
        profileLinksPresenter.showSocialProfiles()
    }

    override fun setState(state: ProfileLinksView.State) {
        if (state is ProfileLinksView.State.ProfileLinksLoaded) {
            Timber.d("Profiles: ${state.profileLinks}")
            view?.isVisible = true
            profileLinksAdapter.items = state.profileLinks
        }
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        profileLinksPresenter.attachView(this)
    }

    override fun onStop() {
        profileLinksPresenter.detachView(this)
        super.onStop()
    }
}