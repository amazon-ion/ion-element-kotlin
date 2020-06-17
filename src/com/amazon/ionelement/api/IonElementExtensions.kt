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
fun IonElement.withAnnotations(vararg additionalAnnotations: String): AnyElement =
    when {
        additionalAnnotations.isEmpty() -> this.asAnyElement()
        else -> copy(annotations = this.annotations + additionalAnnotations)
    }


/** Returns a shallow copy of the current node with the specified additional annotations. */
fun IonElement.withAnnotations(additionalAnnotations: Iterable<String>): AnyElement =
    withAnnotations(*additionalAnnotations.toList().toTypedArray())

/** Returns a shallow copy of the current node with all annotations removed. */
fun IonElement.withoutAnnotations(): AnyElement =
    when {
        this.annotations.isNotEmpty() -> copy(annotations = emptyList())
        else -> this.asAnyElement()
    }

/**
 * Returns a shallow copy of the current node with the specified additional metadata, overwriting any metas
 * that already exist with the same keys.
 */
fun IonElement.withMetas(additionalMetas: MetaContainer): AnyElement =
    when {
        additionalMetas.isEmpty() -> this.asAnyElement()
        else -> copy(metas = metaContainerOf(metas.toList().union(additionalMetas.toList()).toList()))
    }


/**
 * Returns a shallow copy of the current node with the specified additional meta, overwriting any meta
 * that previously existed with the same key.
 *
 * When adding multiple metas, consider [withMetas] instead.
 */
fun IonElement.withMeta(key: String, value: Any): AnyElement = withMetas(metaContainerOf(key to value))

/** Returns a shallow copy of the current node without any metadata. */
fun IonElement.withoutMetas(): AnyElement =
    when {
        metas.isEmpty() -> this.asAnyElement()
        else -> copy(metas = emptyMetaContainer(), annotations = annotations)
    }

/**
 * Returns the string representation of the symbol in the first element of this container.
 *
 * If the first element is not a symbol or this container has no elements, throws [IonElectrolyteException],
 */
val SeqElement.tag get() = this.head.symbolValue

/**
 * Returns the first element of this container.
 *
 * If this container has no elements, throws [IonElectrolyteException].
 */
val SeqElement.head: AnyElement
    get() =
        when (this.size) {
            0 -> ionError(this, "Cannot get head of empty container")
            else -> this.values.first()
        }

/**
 * Returns a sub-list containing all elements of this container except the first.
 *
 * If this container has no elements, throws [IonElectrolyteException].
 *
 * TODO: [drop] is potentially a O(n - 1) operation and therefore not particularly performant.
 */
val SeqElement.tail: List<AnyElement> get()  =
    when (this.size) {
        0 -> ionError(this, "Cannot get tail of empty container")
        else -> this.values.drop(1)//subList(1, this.size)
    }

/** Returns the first element. */
val Iterable<AnyElement>.head get() =
    this.first()

/** Returns a copy of the list with the first element removed. */
val List<AnyElement>.tail get() = this.subList(1, this.size)
