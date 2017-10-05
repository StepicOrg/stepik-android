package org.stepic.droid.util

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TextUtilTest {

    @Before
    fun beforeEachTest() {
    }

    @Test
    fun textWithoutSpaces_textLength() {
        val text = "TextWithoutSpaces"
        Assert.assertEquals(text.length, TextUtil.getIndexOfFirstSpace(text))
    }

    @Test
    fun textWithOneSpace_spaceIndex() {
        val text = "pr ivet"
        Assert.assertEquals(2, TextUtil.getIndexOfFirstSpace(text))
    }


    @Test
    fun textWithMultipleSpaces_spaceIndex() {
        val text = "pr ivet medved"
        Assert.assertEquals(2, TextUtil.getIndexOfFirstSpace(text))
    }


    @Test
    fun leadSpace_zero() {
        val text = " pr ivet"
        Assert.assertEquals(0, TextUtil.getIndexOfFirstSpace(text))
    }

}
