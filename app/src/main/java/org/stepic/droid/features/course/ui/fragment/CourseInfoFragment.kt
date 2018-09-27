package org.stepic.droid.features.course.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.error_course_not_found.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.changeVisibility

class CourseInfoFragment : Fragment() {
    companion object {
        fun newInstance(): Fragment =
                CourseInfoFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_course_info, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        course_not_found.changeVisibility(true)
        error.changeVisibility(false)
    }
}