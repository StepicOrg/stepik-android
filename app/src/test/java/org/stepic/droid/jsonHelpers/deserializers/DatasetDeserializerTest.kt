package org.stepic.droid.jsonHelpers.deserializers

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import org.stepic.droid.testUtils.TestingGsonProvider
import org.stepik.android.model.learning.attempts.DatasetWrapper


class DatasetDeserializerTest {

    private val gson: Gson = TestingGsonProvider.gson

    @Test
    fun datasetNull() {
        val json = """null"""

        val datasetWrapper = gson.fromJson(json, DatasetWrapper::class.java)

        assertNull(datasetWrapper) //gson do not invoke DatasetDeserializer if DatasetWrapper is null
    }

    @Test
    fun datasetString() {
        val str = "hello man"
        val json = """"$str""""

        val datasetWrapper = gson.fromJson(json, DatasetWrapper::class.java)

        assertEquals(str, datasetWrapper.dataset?.someStringValueFromServer)
    }

    @Test
    fun datasetEmptyString_datasetWithEmptyString() {
        val str = ""
        val json = """"$str""""

        val datasetWrapper = gson.fromJson(json, DatasetWrapper::class.java)

        assertEquals(str, datasetWrapper.dataset?.someStringValueFromServer)
    }

    @Test
    fun datasetObject() {
        val json = """{"is_multiple_choice": false, "options": ["1", "2", "4", "3"]}"""
        val datasetWrapper = gson.fromJson(json, DatasetWrapper::class.java)

        assertFalse(datasetWrapper.dataset?.isMultipleChoice == true)
        assertArrayEquals(arrayOf("1", "2", "4", "3"), datasetWrapper.dataset?.options?.toTypedArray())
    }


}