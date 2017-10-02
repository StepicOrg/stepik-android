package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.model.CoursesCarouselColorType
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.util.initCenteredToolbar

class HomeFragment : FragmentBase() {
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private const val myCoursesTag = "my_courses"
        private const val popularCoursesTag = "popular_courses"
        private const val fastContinueTag = "fastContinueTag"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.home_title)

        if (savedInstanceState == null) {
            val fragmentTransaction = childFragmentManager.beginTransaction()

            val fastContinueFragment = FastContinueFragment.newInstance()
            fragmentTransaction.add(R.id.homeFragmentsContainer, fastContinueFragment, fastContinueTag)

            val myCoursesFragment = CoursesCarouselFragment.newInstance(CoursesCarouselInfo(
                    CoursesCarouselColorType.Light,
                    getString(R.string.my_courses_title),
                    Table.enrolled,
                    null))
            fragmentTransaction.add(R.id.homeFragmentsContainer, myCoursesFragment, myCoursesTag)

            val popularCoursesFragment = CoursesCarouselFragment.newInstance(CoursesCarouselInfo(
                    CoursesCarouselColorType.Dark,
                    getString(R.string.popular_courses_title),
                    Table.featured,
                    null
            ))
            fragmentTransaction.add(R.id.homeFragmentsContainer, popularCoursesFragment, popularCoursesTag)
            fragmentTransaction.commit()
        }
    }
}
