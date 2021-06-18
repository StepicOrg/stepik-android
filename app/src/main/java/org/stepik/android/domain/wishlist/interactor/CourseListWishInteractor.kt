package org.stepik.android.domain.wishlist.interactor

import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.wishlist.model.WishlistWrapper
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import ru.nobird.android.core.model.PagedList
import javax.inject.Inject

class CourseListWishInteractor
@Inject
constructor(
    private val courseListInteractor: CourseListInteractor,
    private val wishlistRepository: WishlistRepository
) {
    fun getWishlistStorageRecord(dataSourceType: DataSourceType): Single<StorageRecord<WishlistWrapper>> =
        wishlistRepository.getWishlistRecord(dataSourceType)

    fun getCourseListItems(
        courseIds: List<Long>,
        courseViewSource: CourseViewSource,
        sourceTypeComposition: SourceTypeComposition = SourceTypeComposition.REMOTE
    ): Single<PagedList<CourseListItem.Data>> =
        courseListInteractor.getCourseListItems(courseIds, courseViewSource, sourceTypeComposition)
}