package org.stepik.android.domain.course.interactor

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import okhttp3.ResponseBody
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.then
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.solutions.interactor.SolutionsInteractor
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.CourseScope
import retrofit2.HttpException
import retrofit2.Response
import ru.nobird.android.domain.rx.first
import java.net.HttpURLConnection
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val solutionsInteractor: SolutionsInteractor,
    private val coursePublishSubject: BehaviorSubject<Course>,
    private val courseStatsInteractor: CourseStatsInteractor,
    private val defaultPromoCodeMapper: DefaultPromoCodeMapper,
    private val wishlistRepository: WishlistRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) {
    companion object {
//        private const val COURSE_TIER_PREFIX = "course_tier_"
        private val UNAUTHORIZED_EXCEPTION_STUB =
            HttpException(Response.error<Nothing>(HttpURLConnection.HTTP_UNAUTHORIZED, ResponseBody.create(null, "")))
        private const val PURCHASE_FLOW_IAP = "iap"
        private const val PURCHASE_FLOW_WEB = "web"
    }

    fun getCourseHeaderData(courseId: Long, promo: String? = null, canUseCache: Boolean = true): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(courseId, if (canUseCache) DataSourceType.CACHE else DataSourceType.REMOTE, canUseCache)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap { obtainCourseHeaderData(it, promo) }

    /**
     * Trying to fetch DB data in first place as course object passed with intent could be obsolete
     */
    fun getCourseHeaderData(course: Course, canUseCache: Boolean = true): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(course.id, if (canUseCache) DataSourceType.CACHE else DataSourceType.REMOTE, canUseCache)
            .onErrorReturnItem(course)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap(::obtainCourseHeaderData)

    private fun obtainCourseHeaderData(course: Course, promo: String? = null): Maybe<CourseHeaderData> =
        zip(
            if (firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID].asString() == PURCHASE_FLOW_IAP || RemoteConfig.PURCHASE_FLOW_ANDROID_TESTING_FLAG) {
                courseStatsInteractor.getCourseStatsMobileTiersSingle(course)
            } else  {
                courseStatsInteractor.getCourseStats(listOf(course)).first()
            },
            solutionsInteractor.fetchAttemptCacheItems(course.id, localOnly = true),
            if (promo == null) Single.just(DeeplinkPromoCode.EMPTY to PromoCodeSku.EMPTY) else courseStatsInteractor.checkDeeplinkPromoCodeValidityMobileTiers(course.id, promo),
            (requireAuthorization() then wishlistRepository.getWishlistRecord(DataSourceType.CACHE)).onErrorReturnItem(WishlistEntity.EMPTY)
        ) { courseStats, localSubmissions, (promoCode, promoCodeSku), wishlistEntity ->
            CourseHeaderData(
                courseId = course.id,
                course = course,
                title = course.title ?: "",
                cover = course.cover ?: "",

                stats = courseStats,
                localSubmissionsCount = localSubmissions.count { it is SolutionItem.SubmissionItem },
                deeplinkPromoCode = promoCode,
                deeplinkPromoCodeSku = promoCodeSku,
                defaultPromoCode = defaultPromoCodeMapper.mapToDefaultPromoCode(course),
                isWishlistUpdating = false,
                wishlistEntity = wishlistEntity
            )
        }
            .toMaybe()

    private fun requireAuthorization(): Completable =
        Completable.create { emitter ->
            if (sharedPreferenceHelper.authResponseFromStore != null) {
                emitter.onComplete()
            } else {
                emitter.onError(UNAUTHORIZED_EXCEPTION_STUB)
            }
        }
}