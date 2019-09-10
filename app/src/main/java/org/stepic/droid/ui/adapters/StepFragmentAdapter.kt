package org.stepic.droid.ui.adapters

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.lesson.model.StepItem
import org.stepik.android.view.fragment_pager.ActiveFragmentPagerAdapter
import org.stepik.android.view.step.ui.fragment.StepFragment

class StepFragmentAdapter(
    fm: FragmentManager,
    private val stepTypeResolver: StepTypeResolver
) : FragmentStatePagerAdapter(fm),
    ActiveFragmentPagerAdapter {
    
    var items: List<StepItem> = emptyList()
        set(value) {
            val oldIds = field.map { it.stepWrapper.step.id }
            val newIds = value.map { it.stepWrapper.step.id }
            
            field = value
            if (oldIds != newIds) {
                notifyDataSetChanged()
            }
        }

    lateinit var lessonData: LessonData

    private val _activeFragments = mutableMapOf<Int, Fragment>()
    override val activeFragments: Map<Int, Fragment>
        get() = _activeFragments

    override fun getItem(position: Int): Fragment =
        StepFragment.newInstance(items[position].stepWrapper, lessonData)

    override fun getCount(): Int =
        items.size

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

    @DrawableRes
    fun getTabDrawable(position: Int): Int {
        val step = items.getOrNull(position)?.stepWrapper?.step
        return stepTypeResolver.getDrawableForType(step?.block?.name, step?.actions?.doReview != null)
    }

    @ColorRes
    fun getTabTint(position: Int): Int {
        val stepItem = items.getOrNull(position)
        val isStepPassed = stepItem?.assignmentProgress?.isPassed
            ?: stepItem?.stepProgress?.isPassed
            ?: false

        return stepTypeResolver.getDrawableTintForStep(isStepPassed)
    }
}
