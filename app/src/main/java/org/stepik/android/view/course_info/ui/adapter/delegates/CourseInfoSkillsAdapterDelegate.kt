package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSkillCourseInfoBinding
import org.stepic.droid.databinding.ViewCourseInfoSkillsBinding
import org.stepik.android.view.course_info.model.CourseInfoItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseInfoSkillsAdapterDelegate : AdapterDelegate<CourseInfoItem, DelegateViewHolder<CourseInfoItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseInfoItem> =
        ViewHolder(createView(parent, R.layout.view_course_info_skills))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.Skills

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root) {
        private val viewBinding: ViewCourseInfoSkillsBinding by viewBinding { ViewCourseInfoSkillsBinding.bind(root) }
        private val skillsAdapter: DefaultDelegateAdapter<String> = DefaultDelegateAdapter()

        init {
            skillsAdapter += adapterDelegate(
                layoutResId = R.layout.item_skill_course_info
            ) {
                val viewBinding: ItemSkillCourseInfoBinding = ItemSkillCourseInfoBinding.bind(itemView)
                onBind { data ->
                    viewBinding.skillText.text = data
                }
            }

            with(viewBinding.skillsRecycler) {
                adapter = skillsAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.Skills
            skillsAdapter.items = data.acquiredSkills
        }
    }
}