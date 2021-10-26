package org.stepik.android.domain.debug.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.analytic.experiments.SplitTest
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.SplitTestData
import javax.inject.Inject

class SplitTestsInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    companion object {
        private const val SPLIT_TEST_PREFIX = "split_test_"
    }
    fun getSplitTestDataList(splitTests: Set<SplitTest<*>>): Single<List<SplitTestData>> =
        Single.fromCallable {
            splitTests.map { splitTest ->
                SplitTestData(
                    splitTestName = splitTest.name,
                    splitTestValue = sharedPreferenceHelper.getSplitTestGroup(SPLIT_TEST_PREFIX + splitTest.name) ?: "",
                    splitTestGroups = splitTest.groups.map { it.name }
                )
            }
        }

    fun updateSplitTestData(splitTestData: SplitTestData): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.saveSplitTestGroup(SPLIT_TEST_PREFIX + splitTestData.splitTestName, splitTestData.splitTestValue)
        }
}