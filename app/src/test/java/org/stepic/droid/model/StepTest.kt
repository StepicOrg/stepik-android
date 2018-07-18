package org.stepic.droid.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.TestingGsonProvider
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepic.droid.testUtils.generators.FakeStepGenerator
import org.stepik.android.model.structure.Step

@RunWith(RobolectricTestRunner::class)
class StepTest {

    private val gson = TestingGsonProvider.gson

    @Test
    fun preparingStatusJson() {
        val json = """{"id":236461,
            |"lesson":58532,
            |"position":8,
            |"status":"preparing",
            |"block":{"name":"code","text":"You can change the problem statement right here and specify the settings below. <br><br> Write a program that finds the sum of two numbers.","video":null,"animation":null,"options":{},"subtitle_files":[]},"actions":{"submit":"#","edit_instructions":"#"},"progress":"77-236461","subscriptions":["31-77-236461","30-77-236461"],"instruction":null,"session":null,"instruction_type":null,"viewed_by":0,"passed_by":0,"correct_ratio":null,"worth":null,"is_solutions_unlocked":false,"solutions_unlocked_attempts":3,"has_submissions_restrictions":false,"max_submissions_count":3,"variation":1,"variations_count":1,"create_date":"2017-10-31T08:41:17Z","update_date":"2017-10-31T08:41:17Z","discussions_count":0,"discussion_proxy":"77-236461-1","discussion_threads":["77-236461-1","77-236461-2"]}""".trimMargin()
        val step = gson.fromJson(json, Step::class.java)

        assertEquals(Step.Status.PREPARING, step.status)
    }

    @Test
    fun readyStatusJson() {
        val json = """{"id":236461,
            |"lesson":58532,
            |"position":8,
            |"status":"ready",
            |"block":{"name":"code","text":"You can change the problem statement right here and specify the settings below. <br><br> Write a program that finds the sum of two numbers.","video":null,"animation":null,"options":{},"subtitle_files":[]},"actions":{"submit":"#","edit_instructions":"#"},"progress":"77-236461","subscriptions":["31-77-236461","30-77-236461"],"instruction":null,"session":null,"instruction_type":null,"viewed_by":0,"passed_by":0,"correct_ratio":null,"worth":null,"is_solutions_unlocked":false,"solutions_unlocked_attempts":3,"has_submissions_restrictions":false,"max_submissions_count":3,"variation":1,"variations_count":1,"create_date":"2017-10-31T08:41:17Z","update_date":"2017-10-31T08:41:17Z","discussions_count":0,"discussion_proxy":"77-236461-1","discussion_threads":["77-236461-1","77-236461-2"]}""".trimMargin()

        val step = gson.fromJson(json, Step::class.java)

        assertEquals(Step.Status.READY, step.status)
    }

    @Test
    fun unexpectedStatusNull() {
        val json = """{"id":236461,
            |"lesson":58532,
            |"position":8,
            |"status":"unexpected_wtf-status",
            |"block":{"name":"code","text":"You can change the problem statement right here and specify the settings below. <br><br> Write a program that finds the sum of two numbers.","video":null,"animation":null,"options":{},"subtitle_files":[]},"actions":{"submit":"#","edit_instructions":"#"},"progress":"77-236461","subscriptions":["31-77-236461","30-77-236461"],"instruction":null,"session":null,"instruction_type":null,"viewed_by":0,"passed_by":0,"correct_ratio":null,"worth":null,"is_solutions_unlocked":false,"solutions_unlocked_attempts":3,"has_submissions_restrictions":false,"max_submissions_count":3,"variation":1,"variations_count":1,"create_date":"2017-10-31T08:41:17Z","update_date":"2017-10-31T08:41:17Z","discussions_count":0,"discussion_proxy":"77-236461-1","discussion_threads":["77-236461-1","77-236461-2"]}""".trimMargin()

        val step = gson.fromJson(json, Step::class.java)

        assertNull(step.status)
    }

    @Test
    fun stepIsParcelable() {
        val step = FakeStepGenerator.generate()
        step.assertThatObjectParcelable<Step>()
    }


}
