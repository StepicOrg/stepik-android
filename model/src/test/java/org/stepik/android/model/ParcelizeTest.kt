package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.code.UserCodeRun
import org.stepik.android.model.user.Profile
import org.stepik.android.model.util.assertThatObjectParcelable
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class ParcelizeTest {
    companion object {
        fun createTestActions(): Actions =
            Actions(
                vote = true,
                edit = true,
                delete = false,
                pin = false,
                testSection = "",
                doReview = "",
                editInstructions = ""
            )

        fun createTestSection(): Section =
            Section(
                id = 123123,
                course = 123123,
                units = listOf(13, 31, 23, 123),
                position = 1,
                progress = "",
                slug = "",
                beginDate = Date(),
                endDate = Date(),
                softDeadline = Date(),
                hardDeadline = Date(),
                createDate = Date(),
                updateDate = Date(),
                gradingPolicy = "",
                isActive = true,
                actions = createTestActions(),
                isExam = false,
                discountingPolicy = DiscountingPolicyType.FirstOne,
                isRequirementSatisfied = true,
                requiredSection = 123,
                requiredPercent = 70
            )

        fun createTestTableChoiceAnswer(): TableChoiceAnswer =
            TableChoiceAnswer(
                nameRow = "Row",
                columns = listOf(
                    Cell("Cell1", true),
                    Cell("Cell2", false),
                    Cell("Cell3", true),
                    Cell("Cell4", false)
                )
            )

        fun createTestUserCodeRun(): UserCodeRun =
            UserCodeRun(
                id = 123,
                user = 12312321,
                step = 34501,
                language = "kotlin",
                code = "",
                status = UserCodeRun.Status.EVALUATION,
                stdin = "",
                stdout = "",
                stderr = "",
                timeLimitExceeded = false,
                memoryLimitExceeded = false,
                createDate = Date()
            )

        fun createTestProfile(): Profile =
            Profile(
                id = 12312312,
                firstName = "Test",
                lastName = "Testov",
                fullName = "Test Testov",
                shortBio = "",
                details = "",
                avatar = "",
                isPrivate = false,
                isGuest = false,
                emailAddresses = listOf(123213, 131333)
            )
    }

    @Test
    fun actionsIsParcelable() {
        createTestActions()
            .assertThatObjectParcelable<Actions>()
    }

    @Test
    fun sectionIsParcelable() {
        createTestSection()
            .assertThatObjectParcelable<Section>()
    }

    @Test
    fun tableChoiceAnswerIsParcelable() {
        createTestTableChoiceAnswer()
            .assertThatObjectParcelable<TableChoiceAnswer>()
    }

    @Test
    fun userCodeRunIsParcelable() {
        createTestUserCodeRun()
            .assertThatObjectParcelable<UserCodeRun>()
    }

    @Test
    fun profileIsParcelable() {
        createTestProfile()
            .assertThatObjectParcelable<Profile>()
    }
}