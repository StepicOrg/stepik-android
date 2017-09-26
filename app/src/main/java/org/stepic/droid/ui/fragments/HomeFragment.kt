package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.model.CoursesCarouselColorType
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.activities.contracts.BottomNavigationViewRoot
import org.stepic.droid.ui.util.initCenteredToolbar

class HomeFragment : FragmentBase() {
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private const val myCoursesTag = "my_courses"
        private const val popularCoursesTag = "popular_courses"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)
        applyBottomMarginForRootView()
        initCenteredToolbar(R.string.home_title)

        if (savedInstanceState == null) {
            val fragmentTransaction = fragmentManager.beginTransaction()
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

    override fun onResume() {
        super.onResume()
        (activity as? BottomNavigationViewRoot)?.disableAnyBehaviour()
    }

    override fun onPause() {
        super.onPause()
        (activity as? BottomNavigationViewRoot)?.resetDefaultBehaviour()
    }

    override fun getRootView(): ViewGroup = homeRootView

}
