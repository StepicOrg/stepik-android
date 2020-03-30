package org.stepik.android.view.course_list.delegate

import androidx.annotation.StringRes
import org.stepic.droid.R
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepik.android.presentation.course_list.CourseListView

class CourseListPlaceholderDelegate(
    private val placeholderTextView: PlaceholderTextView,
    @StringRes
    private val emptyMessageRes: Int,
    private val emptyListener: () -> Unit,
    private val errorListener: () -> Unit
) {
    fun setState(state: CourseListView.State) {
        when (state) {
            CourseListView.State.Empty -> {
                placeholderTextView.setPlaceholderText(emptyMessageRes)
                placeholderTextView.setOnClickListener { emptyListener() }
            }
            CourseListView.State.NetworkError -> {
                placeholderTextView.setPlaceholderText(R.string.internet_problem)
                placeholderTextView.setOnClickListener { errorListener() }
            }
        }
    }
}