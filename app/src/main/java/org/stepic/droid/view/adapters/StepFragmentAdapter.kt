package org.stepic.droid.view.adapters

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.stepic.droid.base.MainApplication
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Step
import org.stepic.droid.model.Unit
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.resolvers.IStepResolver

import javax.inject.Inject

class StepFragmentAdapter(fm: FragmentManager, val mStepList: List<Step?>, val mLesson: Lesson?, val mUnit: Unit?) : FragmentStatePagerAdapter(fm) {
    @Inject
    lateinit var mResolver: IStepResolver

    init {
        MainApplication.component().inject(this)
    }

    override fun getItem(position: Int): Fragment {
        val step = mStepList[position]
        val fragment = mResolver.getFragment(step)
        val args = Bundle()
        args.putSerializable(AppConstants.KEY_STEP_BUNDLE, step)
        args.putSerializable(AppConstants.KEY_LESSON_BUNDLE, mLesson)
        args.putSerializable(AppConstants.KEY_UNIT_BUNDLE, mUnit)
        fragment.arguments = args
        return fragment
    }

    override fun getCount(): Int {
        return mStepList.size
    }

    fun getTabDrawable(position: Int): Drawable? {
        val step = mStepList[position]
        return mResolver.getDrawableForType(step?.block?.name, step?.is_custom_passed?:false)
    }
}
