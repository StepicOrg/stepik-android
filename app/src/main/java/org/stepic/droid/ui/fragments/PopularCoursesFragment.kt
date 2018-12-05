package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import org.jetbrains.annotations.NotNull
import org.stepic.droid.R
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepic.droid.ui.util.initCenteredToolbar

class PopularCoursesFragment : CoursesDatabaseFragmentBase() {
    companion object {
        fun newInstance(): PopularCoursesFragment = PopularCoursesFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initCenteredToolbar(R.string.popular_courses_title, false)
    }

    @NotNull
    override fun getCourseType(): DbStructureCourseList.Type =
        DbStructureCourseList.Type.ENROLLED
}
