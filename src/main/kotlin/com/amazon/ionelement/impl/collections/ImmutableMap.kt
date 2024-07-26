// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package com.amazon.ionelement.impl.collections

import com.amazon.ionelement.api.*
import java.util.AbstractMap.SimpleImmutableEntry

/**
 * A [Map] that (unlike the standard library maps) cannot be modified from Java.
 *
 * We cannot use `Map.of(...)` because those were introduced in JDK 9, and we still
 * support JDK 8.
 */
// TODO: Mark as sealed once we're on a higher version of Kotlin.
internal interface ImmutableMap<K, out V> : Map<K, V> {
    companion object {
        val EMPTY: ImmutableMap<Any?, Nothing> = MapBackedImmutableMap(emptyMap())
    }
}

/**
 * Wraps any [Map] as an [ImmutableMap].
 */
private class MapBackedImmutableMap<K, out V>(private val map: Map<K, V>) : ImmutableMap<K, V>, Map<K, V> by map {
    override fun toString(): String = map.toString()
    override fun equals(other: Any?): Boolean = map == other
    override fun hashCode(): Int = map.hashCode()
}

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
private class IonLocationBackedImmutableMap(private val value: IonLocation) : ImmutableMap<String, IonLocation> {
    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun get(key: String): IonLocation? = if (key == ION_LOCATION_META_TAG) value else null
    override fun containsValue(value: IonLocation): Boolean = value == this.value
    override fun containsKey(key: String): Boolean = key == ION_LOCATION_META_TAG

    override val keys: Set<String> get() = KEY_SET
    // We could memoize these values, but that would increase the memory footprint of this class.
    override val values: Collection<IonLocation> get() = setOf(value)
    override val entries: Set<Map.Entry<String, IonLocation>> get() = setOf(SimpleImmutableEntry(ION_LOCATION_META_TAG, value))

    override fun toString(): String = "{$ION_LOCATION_META_TAG=$value}"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Map<*, *>) return false
        if (other.size != 1) return false
        return other[ION_LOCATION_META_TAG] == value
    }
    override fun hashCode(): Int = ION_LOCATION_META_TAG.hashCode() xor value.hashCode()

    companion object {
        private val KEY_SET = setOf(ION_LOCATION_META_TAG)
    }
}

/**
 * Creates a [ImmutableMap] for [this] without making a defensive copy.
 * Only call this method if you are sure that [this] cannot leak anywhere it could be mutated.
 */
internal fun <K, V> Map<K, V>.toImmutableMapUnsafe(): ImmutableMap<K, V> {
    if (this is ImmutableMap) return this
    // Empty ImmutableMap can be safely cast to any `<K, V>` because it is empty.
    @Suppress("UNCHECKED_CAST")
    if (isEmpty()) return (ImmutableMap.EMPTY as ImmutableMap<K, V>)
    return MapBackedImmutableMap(this)
}

/**
 * Creates a [ImmutableMap] for [this].
 * This function creates a defensive copy of [this] unless [this] is already a [ImmutableMap].
 */
internal fun <K, V> Map<K, V>.toImmutableMap(): ImmutableMap<K, V> {
    if (this is ImmutableMap) return this
    // Empty ImmutableMap can be safely cast to any `<K, V>` because it is empty.
    @Suppress("UNCHECKED_CAST")
    if (isEmpty()) return (ImmutableMap.EMPTY as ImmutableMap<K, V>)
    return MapBackedImmutableMap(toMap())
}

/**
 * Creates an [ImmutableMetaContainer] ([ImmutableMap]) that holds [this] [IonLocation] instance.
 */
internal fun IonLocation.toMetaContainer(): ImmutableMap<String, Any> {
    return IonLocationBackedImmutableMap(this)
}
