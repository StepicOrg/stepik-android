package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.attempts.DatasetWrapper
import org.stepik.android.model.util.assertThatObjectParcelable
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class AttemptTest {
    companion object {
        fun createTestAttempt(): Attempt =
            Attempt(
                id = 111,
                step = 2132,
                user = 231321,
                _dataset = DatasetWrapper(DatasetTest.createTestDataset()),
                datasetUrl = "some url",
                status = "status",
                time = Date(),
                timeLeft = "time left"
            )
    }

    @Test
    fun attemptIsParcelable() {
        createTestAttempt()
            .assertThatObjectParcelable<Attempt>()
    }
}