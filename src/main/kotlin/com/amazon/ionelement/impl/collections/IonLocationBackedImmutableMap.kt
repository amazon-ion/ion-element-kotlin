// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package com.amazon.ionelement.impl.collections

import com.amazon.ionelement.api.*
import java.util.AbstractMap.SimpleImmutableEntry
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.adapters.ImmutableSetAdapter

/**
 * Specialized implementation of [Map] that always has a size of 1 and contains only the key [ION_LOCATION_META_TAG].
 *
 * This exists so that we can populate location metadata with as little overhead as possible.
 * On 64-bit Hotspot JVM, this has an object size of only 16 bytes compared to [java.util.Collections.singletonMap]
 * which creates a map with an object size of 40 bytes.
 *
 * We assume that by far the most common use case for this class is calling `get(ION_LOCATION_META_TAG)`
 * rather than general `Map` operations.
 */
internal class IonLocationBackedImmutableMap(private val value: IonLocation) : ImmutableMap<String, IonLocation> {
    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun get(key: String): IonLocation? = if (key == ION_LOCATION_META_TAG) value else null
    override fun containsValue(value: IonLocation): Boolean = value == this.value
    override fun containsKey(key: String): Boolean = key == ION_LOCATION_META_TAG

    override val keys: ImmutableSet<String> get() = KEY_SET
    // We could memoize these values, but that would increase the memory footprint of this class.
    override val values: ImmutableCollection<IonLocation> get() = ImmutableSetAdapter(setOf(value))
    override val entries: ImmutableSet<Map.Entry<String, IonLocation>> get() = ImmutableSetAdapter(setOf(SimpleImmutableEntry(ION_LOCATION_META_TAG, value)))

    override fun toString(): String = "{$ION_LOCATION_META_TAG=$value}"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Map<*, *>) return false
        if (other.size != 1) return false
        return other[ION_LOCATION_META_TAG] == value
    }
    override fun hashCode(): Int = ION_LOCATION_META_TAG.hashCode() xor value.hashCode()

    companion object {
        private val KEY_SET = ImmutableSetAdapter(setOf(ION_LOCATION_META_TAG))
    }
}
