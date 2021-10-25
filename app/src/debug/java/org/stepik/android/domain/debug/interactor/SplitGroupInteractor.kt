package org.stepik.android.domain.debug.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.analytic.experiments.SplitTest
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.SplitGroupData
import javax.inject.Inject

class SplitGroupInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    companion object {
        private const val SPLIT_TEST_PREFIX = "split_test_"
    }
    fun getSplitGroupsList(splitGroups: Set<SplitTest<*>>): Single<List<SplitGroupData>> =
        Single.fromCallable {
            splitGroups.map { splitTest ->
                SplitGroupData(
                    splitTestName = splitTest.name,
                    splitTestValue = sharedPreferenceHelper.getSplitTestGroup(SPLIT_TEST_PREFIX + splitTest.name) ?: "",
                    splitTestGroups = splitTest.groups.map { it.name }
                )
            }
        }

    fun updateSplitGroupData(splitGroupData: SplitGroupData): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.saveSplitTestGroup(SPLIT_TEST_PREFIX + splitGroupData.splitTestName, splitGroupData.splitTestValue)
        }
}