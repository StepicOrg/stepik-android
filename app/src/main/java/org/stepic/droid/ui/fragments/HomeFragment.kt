package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.adapters.CourseSimpleAdapter
import org.stepic.droid.ui.decorators.LeftSpacesDecoration
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.decorators.VerticalSpacesForFirstRowDecoration
import org.stepic.droid.ui.util.StartSnapHelper
import org.stepic.droid.ui.util.initCenteredToolbar

class HomeFragment : FragmentBase() {
    companion object {
        fun newInstance(): HomeFragment {
            val args = Bundle()
            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }

        //FIXME: 04.09.17 if adapter.count < ROW_COUNT -> recycler creates extra padding
        private const val ROW_COUNT = 2
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nullifyActivityBackground()
        initCenteredToolbar(R.string.home_title)
        initMyCoursesRecycler()
    }

    private fun initMyCoursesRecycler() {
        myCoursesOnHome.layoutManager = GridLayoutManager(context, ROW_COUNT, GridLayoutManager.HORIZONTAL, false)
        myCoursesOnHome.adapter = CourseSimpleAdapter(context)
        val spacePx = resources.getDimensionPixelSize(R.dimen.course_list_between_items_padding)
        myCoursesOnHome.addItemDecoration(VerticalSpacesForFirstRowDecoration(spacePx, ROW_COUNT))
        myCoursesOnHome.addItemDecoration(LeftSpacesDecoration(spacePx))
        myCoursesOnHome.addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.home_right_recycler_padding_without_extra), ROW_COUNT))
        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(myCoursesOnHome)
    }

}
