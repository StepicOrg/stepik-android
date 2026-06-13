package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.databinding.FragmentOnboardingPageBinding
import org.stepic.droid.model.OnboardingType
import org.stepic.droid.ui.activities.contracts.OnNextClickedListener
import ru.nobird.android.view.base.ui.extension.argument

class OnboardingFragment : FragmentBase() {
    private val onboardingPageBinding: FragmentOnboardingPageBinding by viewBinding(FragmentOnboardingPageBinding::bind)

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
        onboardingPageBinding.onboardingPageTitle.setText(type.title)
        onboardingPageBinding.onboardingPageSubtitle.setText(type.subtitle)
        onboardingPageBinding.onboardingPageAction.setText(type.getActionText())

        onboardingPageBinding.onboardingPageAction.setOnClickListener {
            (context as OnNextClickedListener).onNextClicked()
        }
        initAnimation(type)
    }

    private fun initAnimation(type: OnboardingType) {
        onboardingPageBinding.onboardingAnimationView.visibility = View.VISIBLE
        onboardingPageBinding.onboardingAnimationView.pauseAnimation()
        onboardingPageBinding.onboardingAnimationView.setAnimation(type.assetPathToAnimation)
    }

    fun startAnimation() {
        onboardingPageBinding.onboardingAnimationView.setAnimation(onboardingType.assetPathToAnimation)
        onboardingPageBinding.onboardingAnimationView.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        onboardingPageBinding.onboardingAnimationView.pauseAnimation()
    }
}
