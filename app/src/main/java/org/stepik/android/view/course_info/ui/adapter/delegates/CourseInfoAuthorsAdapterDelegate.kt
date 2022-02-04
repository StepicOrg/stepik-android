package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemAuthorCourseInfoBinding
import org.stepic.droid.databinding.ViewCourseInfoAuthorsBinding
import org.stepik.android.model.user.User
import org.stepik.android.view.course_info.model.CourseInfoItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseInfoAuthorsAdapterDelegate(
    private val onAuthorClicked: (User) -> Unit
) : AdapterDelegate<CourseInfoItem, DelegateViewHolder<CourseInfoItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseInfoItem> =
        ViewHolder(createView(parent, R.layout.view_course_info_authors))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.AuthorsBlock

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root) {
        private val viewBinding: ViewCourseInfoAuthorsBinding by viewBinding { ViewCourseInfoAuthorsBinding.bind(root) }
        private val authorsAdapter = DefaultDelegateAdapter<User?>()

        init {
            authorsAdapter += adapterDelegate(
                layoutResId = R.layout.item_author_skeleton_course_info,
                isForViewType = { _, data -> data == null }
            )

            authorsAdapter += adapterDelegate(
                layoutResId = R.layout.item_author_course_info,
                isForViewType = { _, data -> data != null }
            ) {
                val viewBinding: ItemAuthorCourseInfoBinding =  ItemAuthorCourseInfoBinding.bind(this.itemView)

                onBind { user ->
                    if (user != null) {
                        viewBinding.root.setOnClickListener { onAuthorClicked(user) }

                        Glide.with(context)
                            .load(user.avatar)
                            .placeholder(ContextCompat.getDrawable(context, R.drawable.general_placeholder))
                            .into(viewBinding.authorIcon)

                        viewBinding.authorName.text = user.fullName
                    }
                }
            }

            with(viewBinding.root) {
                adapter = authorsAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                isNestedScrollingEnabled = false
            }
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.AuthorsBlock
            authorsAdapter.items = data.authors
        }
    }
}