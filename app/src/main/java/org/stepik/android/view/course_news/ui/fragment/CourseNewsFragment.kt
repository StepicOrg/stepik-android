package org.stepik.android.view.course_news.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.course_news.CourseNewsFeature
import org.stepik.android.presentation.course_news.CourseNewsViewModel
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class CourseNewsFragment : Fragment(R.layout.fragment_course_news), ReduxView<CourseNewsFeature.State, CourseNewsFeature.Action.ViewAction> {

    companion object {
        fun newInstance(courseId: Long): Fragment =
            CourseNewsFragment().apply {
                this.courseId = courseId
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val courseNewsViewModel: CourseNewsViewModel by reduxViewModel(this) { viewModelFactory }

    private var courseId: Long by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent(courseId)
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
        TODO("Not yet implemented")
    }

    override fun render(state: CourseNewsFeature.State) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }
}