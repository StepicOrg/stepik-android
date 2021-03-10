package org.stepik.android.cache.code_preference.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.code_preference.model.CodePreference

@Dao
interface CodePreferenceDao {
    @Query("SELECT * FROM CodePreference WHERE languagesKey = :languagesKey")
    fun getCodePreferences(languagesKey: String): Single<CodePreference>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCodePreference(codePreference: CodePreference): Completable
}