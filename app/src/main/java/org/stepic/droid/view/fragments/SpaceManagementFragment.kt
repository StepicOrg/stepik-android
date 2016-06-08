package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase

class SpaceManagementFragment : FragmentBase() {
    companion object {
        fun newInstance(): Fragment {
            val fragment = SpaceManagementFragment()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_space_management, container, false)
        return v
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.let{

        }
    }
}