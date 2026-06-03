package org.stepik.android.view.profile_links.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.databinding.FragmentProfileLinksBinding
import org.stepik.android.model.SocialProfile
import org.stepik.android.presentation.profile_links.ProfileLinksPresenter
import org.stepik.android.presentation.profile_links.ProfileLinksView
import org.stepik.android.view.profile_links.ui.delegate.ProfileLinksAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
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

    private val profileLinksPresenter: ProfileLinksPresenter by viewModels { viewModelFactory }

    private var profileLinksAdapter: DefaultDelegateAdapter<SocialProfile> = DefaultDelegateAdapter()

    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileLinksView.State>

    private val binding: FragmentProfileLinksBinding by viewBinding(FragmentProfileLinksBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

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
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ProfileLinksView.State.Idle>()
        viewStateDelegate.addState<ProfileLinksView.State.Loading>(view, binding.profileExternalLinksLoading)
        viewStateDelegate.addState<ProfileLinksView.State.Error>(view, binding.profileExternalLinksLoadingError.root)
        viewStateDelegate.addState<ProfileLinksView.State.ProfileLinksLoaded>(view, binding.profileExternalLinksRecycler)
        viewStateDelegate.addState<ProfileLinksView.State.Empty>()

        binding.profileExternalLinksLoadingError.tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }

        with(binding.profileExternalLinksRecycler) {
            adapter = profileLinksAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
        setDataToPresenter()
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        profileLinksPresenter.showSocialProfiles(forceUpdate)
    }

    override fun setState(state: ProfileLinksView.State) {
        viewStateDelegate.switchState(state)
        if (state is ProfileLinksView.State.ProfileLinksLoaded) {
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
