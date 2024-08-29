/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

@file:JvmName("ElementLoader")
package com.amazon.ionelement.api

import com.amazon.ion.IonReader
import com.amazon.ionelement.impl.IonElementLoaderImpl

/**
 * Provides several functions for loading [IonElement] instances.
 *
 * All functions wrap any [com.amazon.ion.IonException] in an instance of [IonElementLoaderException], including the
 * current [IonLocation] if one is available.  Note that depending on the state of the [IonReader], a location
 * may not be available.
 */
public interface IonElementLoader {
    /**
     * Reads a single element from the specified Ion text data.
     *
     * Throws an [IonElementLoaderException] if there are multiple top level elements.
     */
    public fun loadSingleElement(ionText: String): AnyElement

    /**
     * Reads the next element from the specified [IonReader].
     *
     * Expects [ionReader] to be positioned *before* the element to be read.
     *
     * If there are additional elements to be read after reading the next element,
     * throws an [IonElementLoaderException].
     */
    public fun loadSingleElement(ionReader: IonReader): AnyElement

    /**
     * Reads all elements remaining to be read from the [IonReader].
     *
     * Expects [ionReader] to be positioned *before* the first element to be read.
     *
     * Avoid this function when reading large amounts of Ion because a large amount of memory will be consumed.
     * Instead, prefer [IonElementLoaderException].
     */
    public fun loadAllElements(ionReader: IonReader): Iterable<AnyElement>

    /**
     * Reads all of the elements in the specified Ion text data.
     *
     * Avoid this function when reading large amounts of Ion because a large amount of memory will be consumed.
     * Instead, prefer [processAll] or [loadCurrentElement].
     */
    public fun loadAllElements(ionText: String): Iterable<AnyElement>

    /**
     * Reads the current element from the specified [IonReader].  Does not close the [IonReader].
     *
     * Expects [ionReader] to be positioned *on* the element to be read--does not call [IonReader.next].
     *
     * This method can be utilized to fetch and process the elements one by one and can help avoid high memory
     * consumption when processing large amounts of Ion data.
     */
    public fun loadCurrentElement(ionReader: IonReader): AnyElement
}

/**
 * Specifies options for [IonElementLoader].
 *
 * Java consumers must use [IonElementLoaderOptions.builder] to create an instance of [IonElementLoaderOptions].
 * ```java
 * IonElementLoaderOptions loaderOpts = IonElementLoaderOptions.builder()
 *     .withIncludeLocationMetadata(true)
 *     .build();
 * ```
 * When calling from Kotlin, [IonElementLoaderOptions.invoke] is also available.
 * ```kotlin
 * val loaderOpts = IonElementLoaderOptions {
 *     includeLocationMetadata = true
 * }
 * ```
 */
public class IonElementLoaderOptions internal constructor(
    val includeLocationMeta: Boolean,
    val useRecursiveLoad: Boolean,
) {
    /*
     * Intentionally not a KDoc comment.
     *
     * IonElementLoaderOptions used to be a data class, but data classes have some inherent backwards compatibility
     * issues. Namely, that the auto-generated `copy` function does not have Java overloads with lesser numbers of
     * parameters.
     *
     * The `copy()` and `component1()` functions and the single arg constructor in this class exist for backwards
     * compatibility. They should be removed in the next major version.
     */

    @Deprecated("Will be removed in the next major version. Replace with builder.")
    @JvmOverloads
    constructor(includeLocationMeta: Boolean = false) : this(includeLocationMeta, DEFAULT.useRecursiveLoad)

    @Deprecated("Will be removed in the next major version. Use toBuilder() to copy this to a new builder.")
    @JvmOverloads
    fun copy(includeLocationMeta: Boolean = this.includeLocationMeta): IonElementLoaderOptions {
        return IonElementLoaderOptions(includeLocationMeta, useRecursiveLoad)
    }

    @Deprecated("Will be removed in the next major version. Replace with getIncludeLocationMetadata().")
    public operator fun component1() = includeLocationMeta

    /**
     * Returns a new [Builder] that is prepopulated with the values in this [IonElementLoaderOptions].
     */
    fun toBuilder(): Builder = Builder(this)

    /**
     * Creates a new copy of this [IonElementLoaderOptions] with the given changes.
     */
    @JvmSynthetic
    fun copyWith(block: Builder.() -> Unit) = toBuilder().apply(block).build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IonElementLoaderOptions) return false
        return includeLocationMeta == other.includeLocationMeta &&
            useRecursiveLoad == other.useRecursiveLoad
    }

    override fun hashCode(): Int {
        // We can treat all the boolean options as flags in a bitfield to guarantee no hash collisions.
        return (if (includeLocationMeta) 1 else 0) +
            (if (useRecursiveLoad) 2 else 0)
    }

    override fun toString(): String {
        return "IonElementLoaderOptions(" +
            "includeLocationMeta=$includeLocationMeta," +
            "useRecursiveLoad=$useRecursiveLoad," +
            ")"
    }

    companion object {
        @JvmStatic
        private val DEFAULT = IonElementLoaderOptions(
            includeLocationMeta = false,
            useRecursiveLoad = true,
        )

        @JvmStatic
        fun builder() = Builder(DEFAULT)

        @JvmSynthetic
        operator fun invoke(block: Builder.() -> Unit): IonElementLoaderOptions {
            return builder().apply(block).build()
        }
    }

    /*
     * Implementation Note:
     * This builder includes `with*` methods for idiomatic usage from Java, and the properties have their setters marked
     * with `@JvmSynthetic` rather than private so that they are visible from Kotlin for a DSL-like experience.
     */
    class Builder internal constructor(startingValues: IonElementLoaderOptions) {
        /**
         * Set to `true` to cause `IonLocation` to be stored in the [IonElement.metas] collection of all elements loaded.
         *
         * This is `false` by default because it has a performance penalty.
         */
        var includeLocationMeta: Boolean = startingValues.includeLocationMeta
            @JvmSynthetic set

        /**
         * Set to `false` to cause the [IonElementLoader] to use an iterative loader. Otherwise, the loader will use a
         * recursive approach and fall back to the iterative loader if it steps into nested containers with a depth greater
         * than 100.
         *
         * This is `true` by default.
         *
         * This does not affect the behavior of the [IonElementLoader] other than its performance characteristics. If
         * performance is critical, users should benchmark both options for loading typical data and select the one with
         * the desired performance characteristics.
         */
        var useRecursiveLoad: Boolean = startingValues.useRecursiveLoad
            @JvmSynthetic set

        /**
         * Set to `true` to cause `IonLocation` to be stored in the [IonElement.metas] collection of all elements loaded.
         *
         * This is `false` by default because it has a performance penalty.
         */
        fun withIncludeLocationMeta(value: Boolean) = apply { includeLocationMeta = value }

        /**
         * Set to `false` to cause the [IonElementLoader] to use an iterative loader. Otherwise, the loader will use a
         * recursive approach and fall back to the iterative loader if it steps into nested containers with a depth greater
         * than 100.
         *
         * This is `true` by default.
         *
         * This does not affect the behavior of the [IonElementLoader] other than its performance characteristics. If
         * performance is critical, users should benchmark both options for loading typical data and select the one with
         * the desired performance characteristics.
         */
        fun withUseRecursiveLoad(value: Boolean) = apply { useRecursiveLoad = value }

        fun build() = IonElementLoaderOptions(includeLocationMeta, useRecursiveLoad)
    }
}

/** Creates an [IonElementLoader] implementation with the specified [options]. */
@JvmOverloads
public fun createIonElementLoader(options: IonElementLoaderOptions = IonElementLoaderOptions()): IonElementLoader =
    IonElementLoaderImpl(options)

/** Provides syntactically lighter way of invoking [IonElementLoader.loadSingleElement]. */
@JvmOverloads
public fun loadSingleElement(ionText: String, options: IonElementLoaderOptions = IonElementLoaderOptions()): AnyElement =
    createIonElementLoader(options).loadSingleElement(ionText)

/** Provides syntactically lighter method of invoking [IonElementLoader.loadSingleElement]. */
@JvmOverloads
public fun loadSingleElement(ionReader: IonReader, options: IonElementLoaderOptions = IonElementLoaderOptions()): AnyElement =
    createIonElementLoader(options).loadSingleElement(ionReader)

/** Provides syntactically lighter method of invoking [IonElementLoader.loadAllElements]. */
@JvmOverloads
public fun loadAllElements(ionText: String, options: IonElementLoaderOptions = IonElementLoaderOptions()): Iterable<AnyElement> =
    createIonElementLoader(options).loadAllElements(ionText)

/** Provides syntactically lighter method of invoking [IonElementLoader.loadAllElements]. */
@JvmOverloads
public fun loadAllElements(ionReader: IonReader, options: IonElementLoaderOptions = IonElementLoaderOptions()): Iterable<AnyElement> =
    createIonElementLoader(options).loadAllElements(ionReader)

/** Provides syntactically lighter method of invoking [IonElementLoader.loadAllElements]. */
@JvmOverloads
public fun loadCurrentElement(ionReader: IonReader, options: IonElementLoaderOptions = IonElementLoaderOptions()): AnyElement =
    createIonElementLoader(options).loadCurrentElement(ionReader)
