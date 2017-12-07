package org.stepic.droid.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_onboarding_page.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.model.OnboardingType
import org.stepic.droid.ui.activities.contracts.OnNextClickedListener
import timber.log.Timber

class OnboardingFragment : FragmentBase() {
    companion object {
        private const val ONBOARDING_TYPE_KEY = "onboarding_type_key"

        fun newInstance(onboardingType: OnboardingType): OnboardingFragment {
            val args = Bundle().apply { putParcelable(ONBOARDING_TYPE_KEY, onboardingType) }
            return OnboardingFragment().apply { arguments = args }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_onboarding_page, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = arguments.getParcelable<OnboardingType>(ONBOARDING_TYPE_KEY)
        initScreen(type)
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
        onboardingAnimationView.speed = 1f

    }

    fun startAnimation() {
        if (isResumed) {
            val type = arguments.getParcelable<OnboardingType>(ONBOARDING_TYPE_KEY)
            Timber.d("startAnimation ${getString(type.title)}")
            onboardingAnimationView.pauseAnimation()
            onboardingAnimationView.setAnimation(type.assetPathToAnimation)
            onboardingAnimationView.setMaxFrame(40)
            onboardingAnimationView.playAnimation()
            onboardingAnimationView.addAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener() {
                Timber.d("frame  ${onboardingAnimationView.frame}")
            })
        }
    }

    override fun onPause() {
        super.onPause()
        onboardingAnimationView.pauseAnimation()
    }

}
