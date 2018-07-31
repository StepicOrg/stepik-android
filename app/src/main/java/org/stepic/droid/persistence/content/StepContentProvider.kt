package org.stepic.droid.persistence.content

interface StepContentProvider {
    fun getDownloadableContentFromStep(): List<String>
}

//    override fun resolvePath(originalPath: String): Maybe<String> =
//            persistentItemDao.getItem(mapOf(DBStructurePersistentItem.Columns.ORIGINAL_PATH to originalPath)).filter {
//                it.status == PersistentItem.Status.COMPLETED
//            }.flatMap {
//                if (File(it.localPath).exists()) {
//                    Maybe.just(it.localPath)
//                } else {
//                    persistentItemDao.remove(DBStructurePersistentItem.Columns.LOCAL_PATH, it.localPath)
//                    Maybe.empty()
//                }
//            }