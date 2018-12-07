package org.stepik.android.data.progress

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.stepik.android.data.progress.repository.ProgressRepositoryImpl
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.model.Progress
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class ProgressRepositoryTest {
    @Mock
    private lateinit var progressCacheDataSource: ProgressCacheDataSource
    @Mock
    private lateinit var progressRemoteDataSource: ProgressRemoteDataSource

    @Test
    fun objectNotInCacheTest() {
        val progressRepositoryImpl = ProgressRepositoryImpl(progressRemoteDataSource, progressCacheDataSource)

        val progressId = "aaa"
        val progress = Progress()
        whenever(progressCacheDataSource.getProgress(progressId)) doReturn Maybe.empty()
        whenever(progressCacheDataSource.saveProgress(progress)) doReturn Completable.complete()

        whenever(progressRemoteDataSource.getProgress(progressId)) doReturn Single.just(progress)

        verifyNoMoreInteractions(progressCacheDataSource)
        verifyNoMoreInteractions(progressRemoteDataSource)

        progressRepositoryImpl
            .getProgress(progressId)
            .test()
            .assertComplete()
            .assertResult(progress)
            .assertNoErrors()
    }

    @Test
    fun objectInCacheTest() {
        val progressRepositoryImpl = ProgressRepositoryImpl(progressRemoteDataSource, progressCacheDataSource)

        val progressId = "aaa"
        val progress = Progress()
        whenever(progressCacheDataSource.getProgress(progressId)) doReturn Maybe.just(progress)
        whenever(progressRemoteDataSource.getProgress(progressId)) doReturn Single.error(IOException(""))

        verifyNoMoreInteractions(progressCacheDataSource)
        verifyNoMoreInteractions(progressRemoteDataSource)

        progressRepositoryImpl
            .getProgress(progressId)
            .test()
            .assertComplete()
            .assertResult(progress)
            .assertNoErrors()
    }

}