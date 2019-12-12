package org.stepik.android.view.profile_courses.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.nobird.android.view.base.ui.extension.argument
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_profile_courses.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.profile_courses.ProfileCoursesPresenter
import org.stepik.android.presentation.profile_courses.ProfileCoursesView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class ProfileCoursesFragment : Fragment(), ProfileCoursesView {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileCoursesFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var userId by argument<Long>()

    private lateinit var profileCoursesPresenter: ProfileCoursesPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileCoursesView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileCoursesPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileCoursesPresenter::class.java)
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile_courses, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ProfileCoursesView.State.Idle>()
        viewStateDelegate.addState<ProfileCoursesView.State.SilentLoading>()
        viewStateDelegate.addState<ProfileCoursesView.State.Empty>()
        viewStateDelegate.addState<ProfileCoursesView.State.Error>(view, profileCoursesLoadingError)
        viewStateDelegate.addState<ProfileCoursesView.State.Content>(view, profileCoursesRecycler)

        setDataToPresenter()
        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        profileCoursesPresenter.fetchUserActivities(forceUpdate)
    }

    override fun onStart() {
        super.onStart()

        profileCoursesPresenter.attachView(this)
    }

    override fun onStop() {
        profileCoursesPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: ProfileCoursesView.State) {
        viewStateDelegate.switchState(state)

        when (state) {
            is ProfileCoursesView.State.Content -> {
                profileCoursesCount.text = resources.getQuantityString(R.plurals.course_count, state.courses.size, state.courses.size)
            }
        }
    }
}