package org.stepic.droid.ui.adapters

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepik.android.view.base.ActivePagerFragmentInterface

class StepFragmentAdapter(fm: FragmentManager, val stepList: List<StepPersistentWrapper?>, val stepTypeResolver: StepTypeResolver) : FragmentStatePagerAdapter(fm),
    ActivePagerFragmentInterface {

    private var lesson: Lesson? = null
    private var unit: Unit? = null
    private var section: Section? = null

    private val _activeFragments = mutableMapOf<Int, Fragment>()
    override val activeFragments: Map<Int, Fragment>
        get() = _activeFragments

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
        val stepWrapper = stepList[position]
        val fragment = stepTypeResolver.getFragment(stepWrapper?.step)
        val args = Bundle()
        args.putParcelable(AppConstants.KEY_STEP_BUNDLE, stepWrapper)
        args.putParcelable(AppConstants.KEY_LESSON_BUNDLE, lesson)
        args.putParcelable(AppConstants.KEY_UNIT_BUNDLE, unit)
        args.putParcelable(AppConstants.KEY_SECTION_BUNDLE, section)
        fragment.arguments = args
        return fragment
    }

    override fun getCount(): Int {
        return stepList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        super
            .instantiateItem(container, position)
            .also {
                (it as? Fragment)?.let { fragment ->  _activeFragments[position] = fragment }
            }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        _activeFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun getTabDrawable(position: Int): Drawable? {
        if (position >= stepList.size) return null
        val step = stepList[position]?.step
        return stepTypeResolver.getDrawableForType(step?.block?.name, step?.isCustomPassed ?: false, step?.actions?.doReview != null)
    }
}
