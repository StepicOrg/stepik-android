package org.stepic.droid.features.achievements.repository

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables.zip
import io.reactivex.rxkotlin.toObservable
import org.stepik.android.model.achievements.Achievement
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.web.achievements.AchievementsService
import org.stepik.android.model.achievements.AchievementProgress
import javax.inject.Inject
import kotlin.math.min

class AchievementsRepositoryImpl
@Inject
constructor(
        private val achievementsService: AchievementsService
): AchievementsRepository {
    private fun getDistinctAchievementKindsOrderedByObtainDate(userId: Long, count: Int = -1): Observable<String> = Observable.create { emitter ->
        val kinds = LinkedHashSet<String>()
        var hasNextPage = true
        var page = 1

        paginationLoop@ while (hasNextPage) {
            val response = achievementsService.getAchievementProgresses(user = userId, page = page, order = "-obtain_date").blockingGet()

            for (item in response.achievementsProgresses) {
                kinds.add(item.kind)
                if (kinds.size == count) {
                    break@paginationLoop
                }
            }

            hasNextPage = response.meta.hasNext
            page = response.meta.page + 1
        }

        hasNextPage = true
        page = 1

        paginationLoop@ while (hasNextPage && (count == -1 || kinds.size < count)) {
            val response = achievementsService.getAchievements(page = page).firstOrError().blockingGet()

            for (item in response.achievements) {
                kinds.add(item.kind)
                if (kinds.size == count) {
                    break@paginationLoop
                }
            }

            hasNextPage = response.meta.hasNext
            page = response.meta.page + 1
        }

        kinds.forEach(emitter::onNext) // in order to handle errors in correct way
        emitter.onComplete()
    }

    private fun getAllAchievementsByKind(kind: String): Observable<Achievement> =
            achievementsService.getAchievements(kind = kind, page = 1).concatMap {
                if (it.meta.hasNext) {
                    Observable.just(it).concatWith(achievementsService.getAchievements(kind = kind, page = it.meta.page + 1))
                } else {
                    Observable.just(it)
                }
            }.concatMap {
                it.achievements.toObservable()
            }

    private fun getAchievementWithProgressByKind(userId: Long, kind: String): Observable<AchievementFlatItem> =
            getAllAchievementsByKind(kind).flatMap {
                zip(
                        Observable.just(it),
                        achievementsService.getAchievementProgresses(user = userId, achievement = it.id)
                                .map { it.achievementsProgresses.firstOrNull() ?: AchievementProgress.EmptyStub }.toObservable()
                )
            }.toList().map {
                val sorted = it.sortedBy { (achievement, _) -> achievement.targetScore }
                val firstCompleted = sorted.indexOfLast { (_, progress) -> progress.obtainDate != null }
                val level = firstCompleted + 1

                AchievementFlatItem(
                        sorted.getOrNull(firstCompleted)?.first,
                        sorted[min(level, sorted.size - 1)].first,
                        sorted[min(level, sorted.size - 1)].second,
                        currentLevel = level,
                        maxLevel = sorted.size
                )
            }.toObservable()

    override fun getAchievements(userId: Long, count: Int): Single<List<AchievementFlatItem>> =
            getDistinctAchievementKindsOrderedByObtainDate(userId, count).flatMap { kind ->
                getAchievementWithProgressByKind(userId, kind)
            }.toList()

    override fun getAchievement(userId: Long, kind: String): Single<AchievementFlatItem> =
            getAchievementWithProgressByKind(userId, kind).firstOrError()
}