package org.stepik.android.cache.user

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.user.structure.DbStructureUser
import org.stepik.android.data.user.source.UserCacheDataSource
import org.stepik.android.model.user.User
import javax.inject.Inject

class UserCacheDataSourceImpl
@Inject
constructor(
    private val userDao: IDao<User>
) : UserCacheDataSource {

    override fun getUsers(userIds: List<Long>): Single<List<User>> =
        Single.fromCallable {
            userDao.getAllInRange(DbStructureUser.Columns.ID, userIds.joinToString())
        }

    override fun saveUsers(users: List<User>): Completable =
        Completable.fromAction {
            userDao.insertOrReplaceAll(users)
        }
}