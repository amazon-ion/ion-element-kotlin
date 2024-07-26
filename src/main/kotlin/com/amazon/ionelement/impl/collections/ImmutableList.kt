// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package com.amazon.ionelement.impl.collections

import com.amazon.ionelement.impl.collections.ImmutableList.Companion.EMPTY

/**
 * A [List] that (unlike the standard library lists) cannot be modified from Java.
 *
 * We cannot use `List.of(...)` because those were introduced in JDK 9, and we still
 * support JDK 8.
 */
// TODO: Mark as sealed once we're on a higher version of Kotlin.
internal interface ImmutableList<out T> : List<T> {
    companion object {
        val EMPTY: ImmutableList<Nothing> = ListBackedImmutableList(emptyList())
    }
}

/**
 * Wraps any [List] as an [ImmutableList].
 */
private class ListBackedImmutableList<out T>(private val list: List<T>) : ImmutableList<T>, List<T> by list {
    override fun toString(): String = list.toString()
    override fun equals(other: Any?): Boolean = list == other
    override fun hashCode(): Int = list.hashCode()
}

/**
 * Wraps an Array as an [ImmutableList].
 */
private class ArrayBackedImmutableList<out T>(private val array: Array<T>) : ImmutableList<T>, AbstractList<T>() {
    override val size: Int get() = array.size
    override fun get(index: Int): T = array[index]

    override fun toString(): String = array.contentDeepToString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is List<*>) return false
        if (other.size != array.size) return false
        for (i in array.indices) {
            if (array[i] != other[i]) return false
        }
        return true
    }

    override fun hashCode(): Int = array.contentDeepHashCode()
}

/**
 * Creates a [ImmutableList] for [this].
 * This function creates a defensive copy of [this] unless [this] is already a [ImmutableList].
 */
internal fun <E> Iterable<E>.toImmutableList(): ImmutableList<E> {
    if (this is ImmutableList<E>) return this
    val isEmpty = if (this is Collection<*>) {
        this.isEmpty()
    } else {
        !this.iterator().hasNext()
    }
    return if (isEmpty) EMPTY else ListBackedImmutableList(this.toList())
}

/**
 * Creates a [ImmutableList] for [this].
 * This function creates a defensive copy of [this] unless [this] is already a [ImmutableList].
 */
internal fun <E> List<E>.toImmutableList(): ImmutableList<E> {
    if (this is ImmutableList<E>) return this
    if (isEmpty()) return EMPTY
    return ListBackedImmutableList(this.toList())
}

/**
 * Creates a [ImmutableList] for [this] without making a defensive copy.
 * Only call this method if you are sure that [this] cannot leak anywhere it could be mutated.
 */
internal fun <E> List<E>.toImmutableListUnsafe(): ImmutableList<E> {
    if (this is ImmutableList<E>) return this
    if (isEmpty()) return EMPTY
    return ListBackedImmutableList(this)
}

/**
 * Creates a [ImmutableList] for [this] without making a defensive copy.
 * Only call this method if you are sure that [this] cannot leak anywhere it could be mutated.
 */
internal fun <E> Array<E>.toImmutableListUnsafe(): ImmutableList<E> {
    if (isEmpty()) return EMPTY
    return ArrayBackedImmutableList(this)
}
