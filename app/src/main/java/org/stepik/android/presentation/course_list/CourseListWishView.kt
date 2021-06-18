package org.stepik.android.presentation.course_list

import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistWrapper
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListWishView : CourseContinueView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Data(
            val wishlistRecord: StorageRecord<WishlistWrapper>,
            val courseListViewState: CourseListView.State,
            val sourceType: DataSourceType? = null
        ) : State()
        object NetworkError : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}