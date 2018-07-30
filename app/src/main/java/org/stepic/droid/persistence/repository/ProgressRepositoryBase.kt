//package org.stepic.droid.persistence.repository
//
//import io.reactivex.Observable
//import org.stepic.droid.persistence.model.PersistentItem
//
//abstract class ProgressRepositoryBase: ProgressRepository {
//    protected abstract val
//
//    /**
//     * Column from DBStructurePersistentItem.Columns
//     */
//    private fun getItemProgress(itemId: Long, persistentObservable: Observable<List<PersistentItem>>) =
//            getItemUpdateObservable(itemId, itemType) // listen for updates
//                    .flatMap { persistentObservable } // fetch from DB
//                    .flatMap { items ->               // fetch progresses from system Download Manager
//                        Observable.just(items) zip downloadItemDao.get(*items.map { it.downloadId }.toLongArray())
//                    }.map { (persistentItems, downloadItems) -> // count progresses
//                        countItemProgress(itemId, persistentItems, downloadItems)
//                    }.distinctUntilChanged() // exclude repetitive events
//
//}