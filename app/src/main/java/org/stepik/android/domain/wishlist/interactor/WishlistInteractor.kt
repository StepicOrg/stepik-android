package org.stepik.android.domain.wishlist.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.interactor.CourseListUserInteractor
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.view.injection.course_list.WishlistOperationBus
import javax.inject.Inject

class WishlistInteractor
@Inject
constructor(
    private val wishlistRepository: WishlistRepository,
    private val courseListUserInteractor: CourseListUserInteractor,

    @WishlistOperationBus
    private val wishlistOperationPublisher: PublishSubject<WishlistOperationData>
) {
    fun getWishlistSyncedWithUserCourses(dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<WishlistEntity> =
        zip(
            courseListUserInteractor.getAllUserCourses(userCourseQuery = UserCourseQuery(page = 1, isArchived = false), sourceType = dataSourceType),
            getWishlist(dataSourceType = dataSourceType)
        ) { userCourses, wishlistEntity ->
            val enrolledCourseIds = userCourses.map(UserCourse::course).toSet()
            val wishlistEntityCourses = wishlistEntity.courses.toSet()
            val updatedWishlistCourses = (wishlistEntityCourses - enrolledCourseIds).toList()
            val updatedWishlistEntity = wishlistEntity.copy(courses = updatedWishlistCourses)
            updatedWishlistEntity
        }
        .flatMap { wishlistRepository.updateWishlistRecord(it) }

    fun getWishlist(dataSourceType: DataSourceType = DataSourceType.CACHE): Single<WishlistEntity> =
        wishlistRepository
            .getWishlistRecord(dataSourceType)

    fun updateWishlistWithOperation(wishlistEntity: WishlistEntity, wishlistOperationData: WishlistOperationData): Single<WishlistEntity> =
        wishlistRepository
            .updateWishlistRecord(wishlistEntity)
            .doOnSuccess { wishlistOperationPublisher.onNext(wishlistOperationData) }
}