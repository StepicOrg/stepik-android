package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.activities.contracts.BottomNavigationViewRoot
import org.stepic.droid.ui.util.initCenteredToolbar

class HomeFragment : FragmentBase() {
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)
        applyBottomMarginForRootView()
        initCenteredToolbar(R.string.home_title)
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
