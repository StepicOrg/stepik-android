package org.stepic.droid.util


import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class KotlinUtilTest {

    private var delimiter: Char = ' '

    @Before
    fun beforeEachTest() {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
        delimiter = numberFormat.decimalFormatSymbols.decimalSeparator
    }

    @Test
    fun testFormatPi2Digit() {
        val pi = 3.14159265358979323
        Assert.assertEquals("3" + delimiter + "14", KotlinUtil.getNiceFormatOfDouble(pi))
    }

    @Test
    fun testFormatPiLeadZeros() {
        val pi = 3.00159265358979323
        Assert.assertEquals("3", KotlinUtil.getNiceFormatOfDouble(pi))
    }

    @Test
    fun testFormatPiLeadZerosRound() {
        val pi = 3.00959265358979323
        Assert.assertEquals("3" + delimiter + "01", KotlinUtil.getNiceFormatOfDouble(pi))
    }

    @Test
    fun testFormatBigNumber() {
        // up to 15 decimals should support
        val big = 123456789123456.1111
        Assert.assertEquals("123456789123456" + delimiter + "11", KotlinUtil.getNiceFormatOfDouble(big))
    }

}
