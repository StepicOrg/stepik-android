package org.stepik.android.data.base.repository.delegate

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import ru.nobird.android.core.model.Identifiable
import ru.nobird.android.domain.rx.doCompletableOnSuccess

class SingleRepositoryDelegate<ID, Item : Identifiable<ID>>(
    private val remoteSource: (ID) -> Single<Item>,
    private val cacheSource: (ID) -> Maybe<Item>,
    private val saveAction: (Item) -> Completable
) {
    fun get(id: ID, sourceType: DataSourceType, allowFallback: Boolean = false): Single<Item> {
        val remote = remoteSource(id)
            .doCompletableOnSuccess(saveAction)

        val cache = cacheSource(id)

        return when (sourceType) {
            DataSourceType.REMOTE ->
                if (allowFallback) {
                    remote.onErrorResumeNext(cache.toSingle())
                } else {
                    remote
                }

            DataSourceType.CACHE ->
                if (allowFallback) {
                    cache.switchIfEmpty(remote)
                } else {
                    cache.toSingle()
                }

            else ->
                throw IllegalArgumentException("Unsupported sourceType = $sourceType")
        }
    }
}