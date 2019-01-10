package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_about_app.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.util.ColorUtil

class AboutAppFragment : FragmentBase() {

    companion object {
        fun newInstance() = AboutAppFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_about_app, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity()

        activity.window.decorView.setBackgroundColor(ColorUtil.getColorArgb(R.color.old_cover, activity))
        super.onViewCreated(view, savedInstanceState)

        privacyPolicyView.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_PRIVACY_POLICY)
            screenManager.openPrivacyPolicyWeb(activity)
        }

        termsOfServiceView.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_TERMS_OF_SERVICE)
            screenManager.openTermsOfServiceWeb(activity)
        }
    }

    override fun onDestroyView() {
        privacyPolicyView.setOnClickListener(null)
        termsOfServiceView.setOnClickListener(null)
        super.onDestroyView()
    }
}
