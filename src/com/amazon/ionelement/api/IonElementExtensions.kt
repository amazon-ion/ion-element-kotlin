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

package com.amazon.ionelement.api

/** Returns a shallow copy of the current node with the specified additional annotations. */
inline fun <reified T: IonElement> T.withAnnotations(vararg additionalAnnotations: String): T =
    when {
        additionalAnnotations.isEmpty() -> this
        else -> copy(annotations = this.annotations + additionalAnnotations) as T
    }


/** Returns a shallow copy of the current node with the specified additional annotations. */
inline fun <reified T: IonElement> T.withAnnotations(additionalAnnotations: Iterable<String>): T =
    withAnnotations(*additionalAnnotations.toList().toTypedArray())

/** Returns a shallow copy of the current node with all annotations removed. */
inline fun <reified T: IonElement> T.withoutAnnotations(): T =
    when {
        this.annotations.isNotEmpty() -> copy(annotations = emptyList()) as T
        else -> this
    }

/**
 * Returns a shallow copy of the current node with the specified additional metadata, overwriting any metas
 * that already exist with the same keys.
 */
inline fun <reified T: IonElement> T.withMetas(additionalMetas: MetaContainer): T =
    when {
        additionalMetas.isEmpty() -> this
        else -> copy(metas = metaContainerOf(metas.toList().union(additionalMetas.toList()).toList())) as T
    }

/**
 * Returns a shallow copy of the current node with the specified additional meta, overwriting any meta
 * that previously existed with the same key.
 *
 * When adding multiple metas, consider [withMetas] instead.
 */
inline fun <reified T: IonElement> T.withMeta(key: String, value: Any): T =
    withMetas(metaContainerOf(key to value))

/** Returns a shallow copy of the current node without any metadata. */
inline fun <reified T: IonElement> T.withoutMetas(): T =
    when {
        metas.isEmpty() -> this
        else -> copy(metas = emptyMetaContainer(), annotations = annotations) as T
    }

/**
 * Returns the string representation of the symbol in the first element of this container.
 *
 * If the first element is not a symbol or this container has no elements, throws [IonElementException],
 */
val SeqElement.tag get() = this.head.symbolValue

/**
 * Returns the first element of this container.
 *
 * If this container has no elements, throws [IonElementException].
 */
val SeqElement.head: AnyElement
    get() =
        when (this.size) {
            0 -> constraintError(this, "Cannot get head of empty container")
            else -> this.values.first()
        }

/**
 * Returns a sub-list containing all elements of this container except the first.
 *
 * If this container has no elements, throws [IonElementException].
 */
val SeqElement.tail: List<AnyElement> get()  =
    when (this.size) {
        0 -> constraintError(this, "Cannot get tail of empty container")
        else -> this.values.subList(1, this.size)
    }

/** Returns the first element. */
val List<AnyElement>.head get() =
    this.first()

/** Returns a copy of the list with the first element removed. */
val List<AnyElement>.tail get() = this.subList(1, this.size)
