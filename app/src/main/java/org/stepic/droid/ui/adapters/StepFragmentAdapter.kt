package org.stepic.droid.ui.adapters

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.stepik.android.model.structure.Lesson
import org.stepik.android.model.structure.Section
import org.stepik.android.model.structure.Step
import org.stepik.android.model.structure.Unit
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.resolvers.StepTypeResolver

class StepFragmentAdapter(fm: FragmentManager, val stepList: List<Step?>, val stepTypeResolver: StepTypeResolver) : FragmentStatePagerAdapter(fm) {

    private var lesson: Lesson? = null
    private var unit: Unit? = null
    private var section: Section? = null


    @JvmOverloads
    fun setDataIfNotNull(outLesson: Lesson? = null, outUnit: Unit? = null, outSection: Section? = null) {

        if (lesson == null && outLesson != null) {
            lesson = outLesson
        }

        if (unit == null && outUnit != null) {
            unit = outUnit
        }

        if (section == null && outSection != null) {
            section = outSection
        }
    }

    override fun getItem(position: Int): Fragment {
        val step = stepList[position]
        val fragment = stepTypeResolver.getFragment(step)
        val args = Bundle()
        args.putParcelable(AppConstants.KEY_STEP_BUNDLE, step)
        args.putParcelable(AppConstants.KEY_LESSON_BUNDLE, lesson)
        args.putParcelable(AppConstants.KEY_UNIT_BUNDLE, unit)
        args.putParcelable(AppConstants.KEY_SECTION_BUNDLE, section)
        fragment.arguments = args
        return fragment
    }

    override fun getCount(): Int {
        return stepList.size
    }

    fun getTabDrawable(position: Int): Drawable? {
        if (position >= stepList.size) return null
        val step = stepList[position]
        return stepTypeResolver.getDrawableForType(step?.block?.name, step?.isCustomPassed ?: false, step?.actions?.doReview != null)
    }
}
