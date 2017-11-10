package org.stepic.droid.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.model.CoursesCarouselInfoConstants
import org.stepic.droid.ui.util.initCenteredToolbar

class HomeFragment : FragmentBase() {
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private const val fastContinueTag = "fastContinueTag"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_home, container, false)

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.home_title)

        if (savedInstanceState == null) {
            childFragmentManager
                    .beginTransaction() //false positive Lint: ... should completed with commit()
                    .add(R.id.homeFastContinueContainer, FastContinueFragment.newInstance(), fastContinueTag)
                    .commitNow()


            myCoursesView.setCourseCarouselInfo(CoursesCarouselInfoConstants.myCourses)
            popularCoursesView.setCourseCarouselInfo(CoursesCarouselInfoConstants.popular)
        }


    }
}
