package org.stepic.droid.ui.fragments

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_about_app.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.social.SocialMedia
import org.stepic.droid.ui.adapters.SocialLinksAdapter
import org.stepic.droid.util.ColorUtil

class AboutAppFragment : FragmentBase() {

    companion object {
        fun newInstance(): Fragment =
            AboutAppFragment()
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
        initSocialRecycler()
    }

    override fun onDestroyView() {
        privacyPolicyView.setOnClickListener(null)
        termsOfServiceView.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun initSocialRecycler() {
        socialListRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        socialListRecyclerView.adapter = SocialLinksAdapter(onClick = ::handleSocialClick)
    }

    private fun handleSocialClick(social: SocialMedia) {
        analytic.reportEventWithName(Analytic.Interaction.CLICK_SOCIAL_NETWORK, social.name)
        screenManager.openSocialMediaLink(requireContext(), social)
    }
}
