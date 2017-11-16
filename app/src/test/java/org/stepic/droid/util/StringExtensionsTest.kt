package org.stepic.droid.util

import org.junit.Test
import org.junit.Assert.assertEquals


class StringExtensionsTest {

    @Test
    fun countWhileTestDefaultFunctionality() {
        val testString = "a \n    x    \n b"
        assertEquals(
                testString.indexOf('x') - testString.indexOf('a') - 1, // whitespace symbols between 'a' and 'x'
                testString.countWhile(testString.indexOf('x') - 1, reversed = true, predicate = Character::isWhitespace))

        assertEquals(
                testString.indexOf('b') - testString.indexOf('x') - 1, // whitespace symbols between 'x' and 'b'
                testString.countWhile(testString.indexOf('x') + 1, predicate = Character::isWhitespace))
    }

    @Test
    fun countWhileTestWrongBounds() {
        val testString = "a \n    x    \n b"
        assertEquals("countWhile should return 0 if started from position out of bounds",
                0, testString.countWhile(-1, reversed = true, predicate = Character::isWhitespace))

        assertEquals("countWhile should return 0 if started from position out of bounds",
                0, testString.countWhile(testString.length, predicate = Character::isWhitespace))
    }

    @Test
    fun substringOrNullTestWrongBounds() {
        val testString = "1232321"
        assertEquals("substringOrNull should return null when start >= end", null, testString.substringOrNull(5, 4))
    }

    @Test
    fun substringOrNullTestOutOfBounds() {
        val testString = "1232321"
        assertEquals("substringOrNull should return null when start < 0", null, testString.substringOrNull(-1, 4))
        assertEquals("substringOrNull should return null when end > length", null, testString.substringOrNull(0, testString.length + 1))
    }

    @Test
    fun substringOrNullTestCorrectBounds() {
        val testString = "1232321"

        assertEquals("substringOrNull result should be equal result of substring on correct bounds",
                testString.substring(0, testString.length), testString.substringOrNull(0, testString.length))
        assertEquals("substringOrNull result should be equal result of substring on correct bounds",
                testString.substring(3, 4), testString.substringOrNull(3, 4))
    }


    @Test
    fun takeLastFromIndexWhileTestDefaultFunctionality() {
        val testString = "a \n    xxx    \n b"
        
        assertEquals("xxx", testString.takeLastFromIndexWhile(testString.lastIndexOf('x') + 1, { it == 'x' }))
        assertEquals(" \n    ", testString.takeLastFromIndexWhile(testString.indexOf('x'), Character::isWhitespace))

        assertEquals("takeLastFromIndexWhile should return whole string if started from end with (const true) predicate",
                testString, testString.takeLastFromIndexWhile(testString.length, { true }))
    }

    @Test
    fun takeLastFromIndexWhileTestWrongBounds() {
        val testString = "a \n    xxx    \n b"
        assertEquals("takeLastFromIndexWhile should return null on wrong bounds",
                null, testString.takeLastFromIndexWhile(testString.length + 1, { true }))
        assertEquals("takeLastFromIndexWhile should return null on wrong bounds",
                null, testString.takeLastFromIndexWhile(-1, { true }))
    }
}