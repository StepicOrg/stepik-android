package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_onboarding_page.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.model.OnboardingType
import org.stepic.droid.ui.activities.contracts.OnNextClickedListener
import org.stepic.droid.util.argument

class OnboardingFragment : FragmentBase() {
    companion object {
        fun newInstance(onboardingType: OnboardingType): OnboardingFragment =
            OnboardingFragment()
                .apply {
                    this.onboardingType = onboardingType
                }
    }

    private var onboardingType: OnboardingType by argument()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_onboarding_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initScreen(onboardingType)
    }

    private fun initScreen(type: OnboardingType) {
        onboardingPageTitle.setText(type.title)
        onboardingPageSubtitle.setText(type.subtitle)
        onboardingPageAction.setText(type.getActionText())

        onboardingPageAction.setOnClickListener {
            (context as OnNextClickedListener).onNextClicked()
        }
        initAnimation(type)
    }

    private fun initAnimation(type: OnboardingType) {
        onboardingAnimationView.visibility = View.VISIBLE
        onboardingAnimationView.pauseAnimation()
        onboardingAnimationView.setAnimation(type.assetPathToAnimation)
    }

    fun startAnimation() {
        onboardingAnimationView.setAnimation(onboardingType.assetPathToAnimation)
        onboardingAnimationView.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        onboardingAnimationView.pauseAnimation()
    }
}
