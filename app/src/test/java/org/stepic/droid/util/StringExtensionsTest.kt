package org.stepic.droid.util

import org.junit.Test
import org.junit.Assert.assertEquals


class StringExtensionsTest {

    @Test
    fun countWhileTest() {
        val testString = "a \n    x    \n b"
        assertEquals(
                testString.indexOf('x') - testString.indexOf('a') - 1, // whitespace symbols between 'a' and 'x'
                testString.countWhile(testString.indexOf('x') - 1, reversed = true, predicate = Character::isWhitespace))

        assertEquals(
                testString.indexOf('b') - testString.indexOf('x') - 1, // whitespace symbols between 'x' and 'b'
                testString.countWhile(testString.indexOf('x') + 1, predicate = Character::isWhitespace))
    }

    @Test
    fun substringOrNullTest() {
        val testString = "1232321"

        assertEquals(null, testString.substringOrNull(-1, 4))
        assertEquals(null, testString.substringOrNull(5, 4))
        assertEquals(null, testString.substringOrNull(0, testString.length + 1))
        assertEquals(testString, testString.substringOrNull(0, testString.length))
    }


    @Test
    fun takeLastFromIndexWhileTest() {
        val testString = "a \n    xxx    \n b"
        
        assertEquals("xxx", testString.takeLastFromIndexWhile(testString.lastIndexOf('x') + 1, { it == 'x' }))
        assertEquals(" \n    ", testString.takeLastFromIndexWhile(testString.indexOf('x'), Character::isWhitespace))

        assertEquals(testString, testString.takeLastFromIndexWhile(testString.length, { true }))
        assertEquals(null, testString.takeLastFromIndexWhile(testString.length + 1, { true }))
        assertEquals(null, testString.takeLastFromIndexWhile(-1, { true }))
    }
}