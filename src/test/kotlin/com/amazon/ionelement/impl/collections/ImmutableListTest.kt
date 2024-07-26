// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package com.amazon.ionelement.impl.collections

import kotlin.test.assertNotEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ImmutableListTest {

    @Nested
    inner class ListBackedImmutableListTest {
        @Test
        fun `equals() should be the same as a standard library list`() {
            val list = listOf(1, 2, 3)

            val immutableListUnsafe = list.toImmutableListUnsafe()
            assertEquals(list, immutableListUnsafe)
            assertEquals(immutableListUnsafe, list)
            assertEquals(immutableListUnsafe, immutableListUnsafe)

            val immutableList = list.toImmutableList()
            assertEquals(list, immutableList)
            assertEquals(immutableList, list)
            assertEquals(immutableList, immutableList)

            assertNotEquals(immutableList, listOf(1))
            assertNotEquals(immutableList, listOf(2, 3, 4))
            assertNotEquals<Any>(immutableList, "foo")
        }

        @Test
        fun `hashCode() should return the same as a standard library list`() {
            val list = listOf(1, 2, 3)

            val immutableListUnsafe = list.toImmutableListUnsafe()
            assertEquals(list.hashCode(), immutableListUnsafe.hashCode())

            val immutableList = list.toImmutableList()
            assertEquals(list.hashCode(), immutableList.hashCode())
        }

        @Test
        fun `toString() should return the same as a standard library list`() {
            val list = listOf(1, 2, 3)

            val immutableListUnsafe = list.toImmutableListUnsafe()
            assertEquals(list.toString(), immutableListUnsafe.toString())

            val immutableList = list.toImmutableList()
            assertEquals(list.toString(), immutableList.toString())
        }

        @Test
        fun `toImmutableList() should return the singleton EMPTY when this is empty`() {
            assertSame(ImmutableList.EMPTY, listOf<Int>().toImmutableList())
            // Check again to cover the same case in the overload for Iterable
            assertSame(ImmutableList.EMPTY, sequenceOf<Int>().asIterable().toImmutableList())
        }

        @Test
        fun `toImmutableListUnsafe() should return the singleton EMPTY when this is empty`() {
            val immutableList = listOf<Int>().toImmutableListUnsafe()
            assertSame(ImmutableList.EMPTY, immutableList)
        }

        @Test
        fun `toImmutableList() should return this when this is already a ImmutableList`() {
            val list = listOf(1, 2, 3).toImmutableList()
            assertSame(list, list.toImmutableList())
            // Check again to cover the same case in the overload for Iterable
            assertSame(list, (list as Iterable<Int>).toImmutableList())
        }

        @Test
        fun `toImmutableListUnsafe() should return this when this is already a ImmutableList`() {
            val list = listOf(1, 2, 3).toImmutableList()
            assertSame(list, list.toImmutableListUnsafe())
        }

        @Test
        fun `toImmutableList() should create a defensive copy`() {
            val list = listOf(1, 2, 3)
            val mutableCopy = list.toMutableList()
            val immutableList = mutableCopy.toImmutableList()

            // Add a value to `mutableCopy` and then make sure that ImmutableList is still [1, 2, 3]
            mutableCopy.add(4)
            assertEquals(list, immutableList)
            Assertions.assertNotEquals(mutableCopy, immutableList)
        }

        @Test
        fun `toImmutableListUnsafe() should not create a defensive copy`() {
            val list = listOf(1, 2, 3)
            val mutableCopy = list.toMutableList()
            val immutableList = mutableCopy.toImmutableListUnsafe()

            // Add a value to `mutableCopy` and then make sure that ImmutableList is now [1, 2, 3, 4]
            mutableCopy.add(4)
            Assertions.assertNotEquals(list, immutableList)
            assertEquals(mutableCopy, immutableList)
        }
    }

    @Nested
    inner class ArrayBackedImmutableListTest {
        @Test
        fun `equals() should be the same as a standard library list`() {
            val array = arrayOf(1, 2, 3)
            val list = listOf(*array)

            val immutableListUnsafe = array.toImmutableListUnsafe()
            assertEquals(list, immutableListUnsafe)
            assertEquals(immutableListUnsafe, list)
            assertEquals(immutableListUnsafe, immutableListUnsafe)

            assertNotEquals(immutableListUnsafe, listOf(1))
            assertNotEquals(immutableListUnsafe, listOf(2, 3, 4))
            assertNotEquals<Any>(immutableListUnsafe, "foo")
        }

        @Test
        fun `hashCode() should return the same as a standard library list`() {
            val array = arrayOf(1, 2, 3)
            val list = listOf(*array)

            val immutableListUnsafe = array.toImmutableListUnsafe()
            assertEquals(list.hashCode(), immutableListUnsafe.hashCode())
        }

        @Test
        fun `toString() should return the same as a standard library list`() {
            val array = arrayOf(1, 2, 3)
            val list = listOf(*array)

            val immutableListUnsafe = array.toImmutableListUnsafe()
            assertEquals(list.toString(), immutableListUnsafe.toString())
        }

        @Test
        fun `toImmutableListUnsafe() should return the singleton EMPTY when this is empty`() {
            val immutableList = arrayOf<Int>().toImmutableListUnsafe()
            assertSame(ImmutableList.EMPTY, immutableList)
        }

        @Test
        fun `toImmutableListUnsafe() should not create a defensive copy`() {
            val array = arrayOf(1, 2, 3)
            val list = listOf(*array)
            val immutableList = array.toImmutableListUnsafe()

            // Change a value in `array` and then make sure that ImmutableList is now [4, 2, 3]
            array[0] = 4
            Assertions.assertNotEquals(list, immutableList)
            assertEquals(array.asList(), immutableList)
        }
    }
}
