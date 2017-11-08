package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import org.jetbrains.annotations.NotNull
import org.stepic.droid.R
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.util.initCenteredToolbar

open class PopularCoursesAloneFragment : CoursesDatabaseFragmentBase() {
    companion object {

        fun newInstance(): PopularCoursesAloneFragment = PopularCoursesAloneFragment()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initCenteredToolbar(R.string.popular_courses_title, false)
    }

    @NotNull
    override fun getCourseType(): Table = Table.featured

}
