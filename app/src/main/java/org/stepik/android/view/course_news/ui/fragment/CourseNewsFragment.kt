package org.stepik.android.view.course_news.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.databinding.FragmentCourseNewsBinding
import org.stepik.android.domain.course_news.model.CourseNewsListItem
import org.stepik.android.presentation.course_news.CourseNewsFeature
import org.stepik.android.presentation.course_news.CourseNewsViewModel
import org.stepik.android.view.course_news.ui.adapter.delegate.CourseNewsAdapterDelegate
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import ru.nobird.app.core.model.PaginationDirection
import ru.nobird.app.presentation.redux.container.ReduxView
import javax.inject.Inject

class CourseNewsFragment : Fragment(R.layout.fragment_course_news),
    ReduxView<CourseNewsFeature.State, CourseNewsFeature.Action.ViewAction> {

    companion object {
        fun newInstance(courseId: Long): Fragment =
            CourseNewsFragment().apply {
                this.courseId = courseId
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val courseNewsViewModel: CourseNewsViewModel by reduxViewModel(this) { viewModelFactory }
    private val courseNewsAdapter: DefaultDelegateAdapter<CourseNewsListItem> = DefaultDelegateAdapter()
    private val viewStateDelegate: ViewStateDelegate<CourseNewsFeature.State> = ViewStateDelegate()

    private var courseId: Long by argument()

    private val courseNewsBinding: FragmentCourseNewsBinding by viewBinding(FragmentCourseNewsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent(courseId)
        courseNewsAdapter += adapterDelegate<CourseNewsListItem, CourseNewsListItem.Placeholder>(layoutResId = R.layout.item_course_news_placeholder)
        courseNewsAdapter += CourseNewsAdapterDelegate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(courseNewsBinding.courseNewsRecycler) {
            adapter = courseNewsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical_course_search)?.let(::setDrawable)
            })
            setOnPaginationListener { paginationDirection ->
                if (paginationDirection == PaginationDirection.NEXT) {
                    courseNewsViewModel.onNewMessage(CourseNewsFeature.Message.FetchNextPage)
                }
            }
        }

        viewStateDelegate.addState<CourseNewsFeature.State.Idle>()
        viewStateDelegate.addState<CourseNewsFeature.State.LoadingAnnouncements>(courseNewsBinding.courseNewsRecycler)
        viewStateDelegate.addState<CourseNewsFeature.State.NotEnrolled>(courseNewsBinding.courseNewsEmpty.root)
        viewStateDelegate.addState<CourseNewsFeature.State.Error>(courseNewsBinding.courseNewsError.root)
        viewStateDelegate.addState<CourseNewsFeature.State.Empty>(courseNewsBinding.courseNewsEmpty.root)
        viewStateDelegate.addState<CourseNewsFeature.State.Content>(courseNewsBinding.courseNewsRecycler)
    }

    override fun onResume() {
        super.onResume()
        courseNewsViewModel.onNewMessage(CourseNewsFeature.Message.OnScreenOpenedMessage)
    }

    private fun injectComponent(courseId: Long) {
        App.componentManager()
            .courseComponent(courseId)
            .inject(this)
    }

    private fun releaseComponent(courseId: Long) {
        App.componentManager()
            .releaseCourseComponent(courseId)
    }

    override fun onAction(action: CourseNewsFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: CourseNewsFeature.State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is CourseNewsFeature.State.Empty ->
                courseNewsBinding.courseNewsEmpty.placeholderMessage.text = getString(R.string.empty_try_later)
            is CourseNewsFeature.State.NotEnrolled ->
                courseNewsBinding.courseNewsEmpty.placeholderMessage.text = getString(R.string.course_news_not_enrolled_message)
            is CourseNewsFeature.State.LoadingAnnouncements -> {
                courseNewsAdapter.items = listOf(
                    CourseNewsListItem.Placeholder,
                    CourseNewsListItem.Placeholder,
                    CourseNewsListItem.Placeholder
                )
            }
            is CourseNewsFeature.State.Content -> {
                if (state.isLoadingNextPage) {
                    courseNewsAdapter.items = state.courseNewsListItems + CourseNewsListItem.Placeholder
                } else {
                    courseNewsAdapter.items = state.courseNewsListItems
                }
            }
        }
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}