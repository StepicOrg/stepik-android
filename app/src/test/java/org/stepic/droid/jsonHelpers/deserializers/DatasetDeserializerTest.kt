package org.stepic.droid.jsonHelpers.deserializers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.stepic.droid.model.DatasetWrapper


class DatasetDeserializerTest {


    companion object {
        private lateinit var gson: Gson

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            gson = GsonBuilder()
                    .registerTypeAdapter(DatasetWrapper::class.java, DatasetDeserializer())
                    .create()
        }
    }


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

        assertEquals(str, datasetWrapper.dataset.someStringValueFromServer)
    }

    @Test
    fun datasetEmptyString_datasetWithEmptyString() {
        val str = ""
        val json = """"$str""""

        val datasetWrapper = gson.fromJson(json, DatasetWrapper::class.java)

        assertEquals(str, datasetWrapper.dataset.someStringValueFromServer)
    }

    @Test
    fun datasetObject() {
        val json = """{"is_multiple_choice": false, "options": ["1", "2", "4", "3"]}"""
        val datasetWrapper = gson.fromJson(json, DatasetWrapper::class.java)

        assertFalse(datasetWrapper.dataset.is_multiple_choice)
        assertArrayEquals(arrayOf("1", "2", "4", "3"), datasetWrapper.dataset.options.toTypedArray())
    }


}