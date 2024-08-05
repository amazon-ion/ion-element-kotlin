package com.amazon.ionelement.impl.collections

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ExtensionsTest {

    @Test
    fun `toImmutableMap() should return the singleton EMPTY when this is empty`() {
        val immutableMap = mapOf<Int, String>().toImmutableMap()
        Assertions.assertSame(EMPTY_IMMUTABLE_MAP, immutableMap)
    }

    @Test
    fun `toImmutableMapUnsafe() should return the singleton EMPTY when this is empty`() {
        val immutableMap = mapOf<Int, String>().toImmutableMapUnsafe()
        Assertions.assertSame(EMPTY_IMMUTABLE_MAP, immutableMap)
    }

    @Test
    fun `toImmutableMap() should return this when this is already a ImmutableMap`() {
        val map = mapOf(
            "a" to 1,
            "b" to 2,
            "c" to 3,
        ).toImmutableMap()
        Assertions.assertSame(map, map.toImmutableMap())
    }

    @Test
    fun `toImmutableMapUnsafe() should return this when this is already a ImmutableMap`() {
        val map = mapOf(
            "a" to 1,
            "b" to 2,
            "c" to 3,
        ).toImmutableMap()
        Assertions.assertSame(map, map.toImmutableMapUnsafe())
    }

    @Test
    fun `toImmutableMap() should create a defensive copy`() {
        val map = mapOf(
            "a" to 1,
            "b" to 2,
            "c" to 3,
        )
        val mutableCopy = map.toMutableMap()
        val immutableMap = mutableCopy.toImmutableMap()

        // Add a value to `mutableCopy` and then make sure that ImmutableMap is still [1, 2, 3]
        mutableCopy["d"] = 4
        assertEquals(map, immutableMap)
        Assertions.assertNotEquals(mutableCopy, immutableMap)
    }

    @Test
    fun `toImmutableMapUnsafe() should not create a defensive copy`() {
        val map = mapOf(
            "a" to 1,
            "b" to 2,
            "c" to 3,
        )
        val mutableCopy = map.toMutableMap()
        val immutableMap = mutableCopy.toImmutableMapUnsafe()

        // Add a value to `mutableCopy` and then make sure that ImmutableMap now has "d" -> 4
        mutableCopy["d"] = 4
        Assertions.assertNotEquals(map, immutableMap)
        assertEquals(mutableCopy, immutableMap)
    }

    @Test
    fun `List toImmutableList() should return the singleton EMPTY when this is empty`() {
        assertSame(EMPTY_IMMUTABLE_LIST, listOf<Int>().toImmutableList())
    }

    @Test
    fun `Iterable toImmutableList() should return the singleton EMPTY when this is empty`() {
        assertSame(EMPTY_IMMUTABLE_LIST, sequenceOf<Int>().asIterable().toImmutableList())
    }

    @Test
    fun `List toImmutableListUnsafe() should return the singleton EMPTY when this is empty`() {
        val immutableList = listOf<Int>().toImmutableListUnsafe()
        assertSame(EMPTY_IMMUTABLE_LIST, immutableList)
    }

    @Test
    fun `List toImmutableList() should return this when this is already a ImmutableList`() {
        val list = listOf(1, 2, 3).toImmutableList()
        assertSame(list, list.toImmutableList())
    }

    @Test
    fun `Iterable toImmutableList() should return this when this is already a ImmutableList`() {
        val list = listOf(1, 2, 3).toImmutableList()
        assertSame(list, (list as Iterable<Int>).toImmutableList())
    }

    @Test
    fun `List toImmutableListUnsafe() should return this when this is already a ImmutableList`() {
        val list = listOf(1, 2, 3).toImmutableList()
        assertSame(list, list.toImmutableListUnsafe())
    }

    @Test
    fun `List toImmutableList() should create a defensive copy`() {
        val list = listOf(1, 2, 3)
        val mutableCopy = list.toMutableList()
        val immutableList = mutableCopy.toImmutableList()

        // Add a value to `mutableCopy` and then make sure that ImmutableList is still [1, 2, 3]
        mutableCopy.add(4)
        assertEquals(list, immutableList)
        Assertions.assertNotEquals(mutableCopy, immutableList)
    }

    @Test
    fun `List toImmutableListUnsafe() should not create a defensive copy`() {
        val list = listOf(1, 2, 3)
        val mutableCopy = list.toMutableList()
        val immutableList = mutableCopy.toImmutableListUnsafe()

        // Add a value to `mutableCopy` and then make sure that ImmutableList is now [1, 2, 3, 4]
        mutableCopy.add(4)
        Assertions.assertNotEquals(list, immutableList)
        assertEquals(mutableCopy, immutableList)
    }

    @Test
    fun `Array toImmutableListUnsafe() should return the singleton EMPTY when this is empty`() {
        val immutableList = arrayOf<Int>().toImmutableListUnsafe()
        assertSame(EMPTY_IMMUTABLE_LIST, immutableList)
    }

    @Test
    fun `Array toImmutableListUnsafe() should not create a defensive copy`() {
        val array = arrayOf(1, 2, 3)
        val list = listOf(*array)
        val immutableList = array.toImmutableListUnsafe()

        // Change a value in `array` and then make sure that ImmutableList is now [4, 2, 3]
        array[0] = 4
        Assertions.assertNotEquals(list, immutableList)
        assertEquals(array.asList(), immutableList)
    }
}
