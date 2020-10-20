package org.stepik.android.domain.achievement.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class AchievementItemTest {
    companion object {
        fun createTestAchievementItem(): AchievementItem =
            AchievementItem(
                uploadcareUUID = "24774934-0fa9-11eb-adc1-0242ac120002",
                isLocked = false,
                kind = "",
                currentScore = 1,
                targetScore = 10,
                currentLevel = 2,
                maxLevel = 5
            )
    }

    @Test
    fun achievementItemIsSerializable() {
        createTestAchievementItem()
            .assertThatObjectParcelable<AchievementItem>()
    }
}