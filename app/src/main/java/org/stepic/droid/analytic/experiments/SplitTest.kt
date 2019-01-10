package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper

abstract class SplitTest<G : SplitTest.Group>(
    private val analytics: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,

    val name: String,
    private val groups: Array<G>
) {
    companion object {
        private const val SPLIT_TEST_PREFIX = "split_test_"
    }

    val currentGroup: G = getLocalGroup() ?: fetchGroup()

    private fun getLocalGroup(): G? =
        sharedPreferenceHelper
            .getSplitTestGroup(SPLIT_TEST_PREFIX + name)
            ?.let { groupName -> groups.find { it.name == groupName } }

    private fun fetchGroup(): G =
        getRandomGroup().also { group ->
            sharedPreferenceHelper.saveSplitTestGroup(SPLIT_TEST_PREFIX + name, group.name)
            analytics.setUserProperty(SPLIT_TEST_PREFIX + name, group.name)
        }

    private fun getRandomGroup(): G {
        val totalDistribution = groups.sumBy(Group::distribution)

        val seed = (0 until totalDistribution).random()
        var cumulativeSum = 0

        return groups.first {
            cumulativeSum += it.distribution
            cumulativeSum > seed
        }
    }

    interface Group {
        val name: String
        val distribution: Int
            get() = 1
    }
}