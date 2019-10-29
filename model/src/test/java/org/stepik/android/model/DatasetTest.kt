package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.attempts.Dataset
import org.stepik.android.model.attempts.Pair
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class DatasetTest {
    companion object {
        fun createTestDataset(): Dataset =
            Dataset(
                options = listOf("a", "b"),
                someStringValueFromServer = "some val",
                pairs = listOf(Pair("a", "b"), Pair("c", "d")),
                rows = listOf("a", "b"),
                columns = listOf("a", "b"),
                description = "description",

                isMultipleChoice = true,
                isHtmlEnabled = true,
                isCheckbox = true
            )
    }

    @Test
    fun datasetIsParcelable() {
        createTestDataset()
            .assertThatObjectParcelable<Dataset>()
    }
}