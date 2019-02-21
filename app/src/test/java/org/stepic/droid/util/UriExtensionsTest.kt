package org.stepic.droid.util

import android.net.Uri
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UriExtensionsTest {

    @Test
    fun getQueryParametersTest() {
        val key = "promo"
        val value = "5rub"
        val uri = Uri.parse("https://stepik.org/course/40005/pay?$key=$value")

        val params = uri.getQueryParameters()

        Assert.assertEquals(mapOf(key to value), params)
    }

    @Test
    fun getAllQueryParametersTest() {
        val key = "promo"
        val value1 = "5rub"
        val value2 = "15rub"
        val uri = Uri.parse("https://stepik.org/course/40005/pay?$key=$value1&$key=$value2")

        val params = uri.getAllQueryParameters()

        Assert.assertEquals(mapOf(key to listOf(value1, value2)), params)
    }

    @Test
    fun appendQueryParametersTest() {
        val params = mapOf("promo" to "5rub")

        val uri = Uri.parse("https://stepik.org/course/40005/pay")

        val actualParams = uri
            .buildUpon()
            .appendQueryParameters(params)
            .build()
            .getQueryParameters()

        Assert.assertEquals(params, actualParams)
    }

    @Test
    fun appendAllQueryParametersTest() {
        val params = mapOf("promo" to listOf("5rub", "10rub"))

        val uri = Uri.parse("https://stepik.org/course/40005/pay")

        val actualParams = uri
            .buildUpon()
            .appendQueryParameters(params)
            .build()
            .getAllQueryParameters()

        Assert.assertEquals(params, actualParams)
    }

}