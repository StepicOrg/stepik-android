package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.BuildConfig
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.databinding.FragmentAboutAppBinding
import org.stepic.droid.social.SocialMedia
import org.stepic.droid.ui.adapters.SocialLinksAdapter

class AboutAppFragment : FragmentBase() {
    private val aboutAppBinding: FragmentAboutAppBinding by viewBinding(FragmentAboutAppBinding::bind)

    companion object {
        fun newInstance(): Fragment =
            AboutAppFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_about_app, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aboutAppBinding.privacyPolicyView.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_PRIVACY_POLICY)
            screenManager.openPrivacyPolicyWeb(activity)
        }

        aboutAppBinding.termsOfServiceView.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_TERMS_OF_SERVICE)
            screenManager.openTermsOfServiceWeb(activity)
        }

        aboutAppBinding.appVersionName.text = getString(R.string.settings_app_version, BuildConfig.VERSION_NAME)
        initSocialRecycler()
    }

    override fun onDestroyView() {
        aboutAppBinding.privacyPolicyView.setOnClickListener(null)
        aboutAppBinding.termsOfServiceView.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun initSocialRecycler() {
        aboutAppBinding.socialListRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        aboutAppBinding.socialListRecyclerView.adapter = SocialLinksAdapter(onClick = ::handleSocialClick)
    }

    private fun handleSocialClick(social: SocialMedia) {
        analytic.reportEventWithName(Analytic.Interaction.CLICK_SOCIAL_NETWORK, social.name)
        screenManager.openSocialMediaLink(requireContext(), social)
    }
}
