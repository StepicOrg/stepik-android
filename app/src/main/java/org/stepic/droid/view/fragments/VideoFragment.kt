package org.stepic.droid.view.fragments

import android.os.Bundle
import org.stepic.droid.base.FragmentBase

class VideoFragment : FragmentBase() {
    companion object {
        fun newInstance(): VideoFragment {
            val args = Bundle()
            val fragment = VideoFragment()
            fragment.arguments = args
            return fragment
        }
    }
}