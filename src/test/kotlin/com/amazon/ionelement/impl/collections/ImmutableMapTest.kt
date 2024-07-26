// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package com.amazon.ionelement.impl.collections

import com.amazon.ionelement.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ImmutableMapTest {

    @Nested
    inner class MapBackedImmutableMapTest {
        @Test
        fun `equals() should be the same as a standard library map`() {
            val map = mapOf(
                1 to "a",
                2 to "b",
                3 to "c",
            )

            val immutableMapUnsafe = map.toImmutableMapUnsafe()
            assertEquals(map, immutableMapUnsafe)
            assertEquals(immutableMapUnsafe, map)
            assertEquals(immutableMapUnsafe, immutableMapUnsafe)

            val immutableMap = map.toImmutableMap()
            assertEquals(map, immutableMap)
            assertEquals(immutableMap, map)
            assertEquals(immutableMap, immutableMap)

            assertNotEquals(immutableMap, mapOf(1 to "a"))
            assertNotEquals<Any>(immutableMap, "foo")
        }

        @Test
        fun `hashCode() should be the same as a standard library map`() {
            val map = mapOf(
                "a" to 1,
                "b" to 2,
                "c" to 3,
            )

            val immutableMapUnsafe = map.toImmutableMapUnsafe()
            assertEquals(map.hashCode(), immutableMapUnsafe.hashCode())

            val immutableMap = map.toImmutableMap()
            assertEquals(map.hashCode(), immutableMap.hashCode())
        }

        @Test
        fun `toString() should be the same as a standard library map`() {
            val map = mapOf(
                "a" to 1,
                "b" to 2,
                "c" to 3,
            )

            val immutableMapUnsafe = map.toImmutableMapUnsafe()
            assertEquals(map.toString(), immutableMapUnsafe.toString())

            val immutableMap = map.toImmutableMap()
            assertEquals(map.toString(), immutableMap.toString())
        }

        @Test
        fun `toImmutableMap() should return the singleton EMPTY when this is empty`() {
            val immutableMap = mapOf<Int, String>().toImmutableMap()
            Assertions.assertSame(ImmutableMap.EMPTY, immutableMap)
        }

        @Test
        fun `toImmutableMapUnsafe() should return the singleton EMPTY when this is empty`() {
            val immutableMap = mapOf<Int, String>().toImmutableMapUnsafe()
            Assertions.assertSame(ImmutableMap.EMPTY, immutableMap)
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
    }

    @Nested
    inner class IonLocationBackedImmutableMapTest {
        private val location = IonBinaryLocation(1)
        private val locationMap: Map<String, Any> = location.toMetaContainer()
        private val generalMap = mapOf(ION_LOCATION_META_TAG to location)

        @Test
        fun `equals() should be the same as a standard library map`() {
            assertEquals(generalMap, locationMap)
            assertEquals(locationMap, generalMap)
            assertEquals(locationMap, locationMap)
            assertNotEquals(locationMap, mapOf(ION_LOCATION_META_TAG to IonBinaryLocation(1), "foo-bar" to IonBinaryLocation(99)))
            assertNotEquals<Any>(locationMap, setOf(location))
            assertNotEquals(locationMap, mapOf(ION_LOCATION_META_TAG to IonBinaryLocation(99)))
            assertNotEquals(locationMap, mapOf("foo-bar" to location))
        }

        @Test
        fun `hashCode() should be the same as a standard library map`() = assertEquals(generalMap.hashCode(), locationMap.hashCode())

        @Test
        fun `toString() should be the same as a standard library map`() = assertEquals(generalMap.toString(), locationMap.toString())

        @Test
        fun `keys should be the same as an equivalent standard library map`() = assertEquals(generalMap.keys, locationMap.keys)

        @Test
        fun `values should be the same as an equivalent standard library map`() = assertEquals(generalMap.values, locationMap.values)

        @Test
        fun `entries should the same as an equivalent standard library map`() = assertEquals(generalMap.entries, locationMap.entries)

        @Test
        fun `size should be 1`() = assertEquals(1, locationMap.size)

        @Test
        fun `isEmpty() should return false`() = assertFalse(locationMap.isEmpty())

        @Test
        fun `get(ION_LOCATION_META_TAG) should return the expected value`() = assertEquals(location, locationMap[ION_LOCATION_META_TAG])

        @Test
        fun `get() should return null for any other key`() = assertNull(locationMap["foo-bar"])

        @Test
        fun `containsKey(ION_LOCATION_META_TAG) should return true`() = assertTrue(locationMap.containsKey(ION_LOCATION_META_TAG))

        @Test
        fun `containsKey() should return false for any other key`() = assertFalse(locationMap.containsKey("foo-bar"))

        @Test
        fun `containsValue() should return true for the expected IonLocation`() = assertTrue(locationMap.containsValue(location))

        @Test
        fun `containsValue() should return false for any other value`() = assertFalse(locationMap.containsValue(ION_LOCATION_META_TAG))
    }
}
