// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
@file:JvmName("Equivalence")
package com.amazon.ionelement.api

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.ElementType.*

/**
 * Checks if two [IonElement]s are equal.
 *
 * This function is normative for equality of [IonElement]s.
 * All [IonElement] implementations must override [Any.equals] in a way that is equivalent to calling this function.
 */
public fun areElementsEqual(left: IonElement, right: IonElement): Boolean {
    return left.asAnyElement().isEquivalentTo(right.asAnyElement())
}

/**
 * Checks if two [StructField]s are equal.
 *
 * This function is normative for equality of [StructField]s.
 * All [StructField] implementations must override [Any.equals] in a way that is equivalent to calling this function.
 */
public fun areFieldsEqual(left: StructField, right: StructField): Boolean = left.name == right.name && left.value == right.value

/**
 * Checks if this [AnyElement] is equal to some other object.
 */
internal fun AnyElement.isEquivalentTo(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AnyElement) return false
    return this.isEquivalentTo(other)
}

/**
 * Internal only function that is equivalent to [areElementsEqual]
 */
private fun AnyElement.isEquivalentTo(other: AnyElement): Boolean {
    if (this === other) return true
    val thisType = this.type
    if (thisType != other.type) return false
    if (annotations != other.annotations) return false
    // Metas intentionally not included here.

    return if (isNull) {
        // Already verified that they are the same type
        other.isNull
    } else if (other.isNull) {
        false
    } else
    // Matching an enum rather than a type allows the Kotlin compiler
    // to use a table switch instead of a chain of if/else comparisons.
        when (thisType) {
            BOOL -> booleanValue == other.booleanValue
            INT -> when {
                integerSize != other.integerSize -> false
                integerSize == IntElementSize.LONG -> longValue == other.longValue
                else -> bigIntegerValue == other.bigIntegerValue
            }
            // compareTo() distinguishes between 0.0 and -0.0 while `==` operator does not.
            FLOAT -> doubleValue.compareTo(other.doubleValue) == 0
            // `==` considers `0d0` and `-0d0` to be equivalent.  `Decimal.equals` does not.
            DECIMAL -> Decimal.equals(decimalValue, other.decimalValue)
            TIMESTAMP -> timestampValue == other.timestampValue
            STRING -> stringValue == other.stringValue
            SYMBOL -> symbolValue == other.symbolValue
            BLOB -> blobValue == other.blobValue
            CLOB -> clobValue == other.clobValue
            LIST -> listValues == other.listValues
            SEXP -> sexpValues == other.sexpValues
            STRUCT -> {
                val thisFields = this.structFields
                val otherFields = other.structFields
                when {
                    thisFields === otherFields -> true
                    thisFields.size != otherFields.size -> false
                    // We've tried the inexpensive checks, now do a deep comparison
                    else -> thisFields.groupingBy { it }.eachCount() == otherFields.groupingBy { it }.eachCount()
                }
            }
            NULL -> TODO("Unreachable")
        }
}

/**
 * Calculates the hash code of an [IonElement].
 *
 * Implementations of [IonElement] MAY NOT calculate their own hash codes, but MAY cache the result of this function.
 */
public fun hashElement(ionElement: IonElement): Int {
    val element = ionElement.asAnyElement()
    val typeAndValueHashCode = if (element.isNull) {
        element.type.hashCode()
    } else {
        // Matching an enum rather than a type allows the Kotlin compiler
        // to use a tableswitch instead of a chain of if/else comparisons.
        val valueHashCode = when (element.type) {
            BOOL -> element.booleanValue.hashCode()
            INT -> when (element.integerSize) {
                IntElementSize.LONG -> element.longValue.hashCode()
                IntElementSize.BIG_INTEGER -> element.bigIntegerValue.hashCode()
            }
            // Adding compareTo(0.0) causes 0e0 to have a different hash code than -0e0
            FLOAT -> element.doubleValue.compareTo(0.0).hashCode() * 31 + element.doubleValue.hashCode()
            DECIMAL -> element.decimalValue.isNegativeZero.hashCode() * 31 + element.decimalValue.hashCode()
            TIMESTAMP -> element.timestampValue.hashCode()
            STRING -> element.textValue.hashCode()
            SYMBOL -> element.textValue.hashCode()
            BLOB -> element.bytesValue.hashCode()
            CLOB -> element.bytesValue.hashCode()
            LIST -> element.listValues.hashCode()
            SEXP -> element.sexpValues.hashCode()
            STRUCT -> element.structFields.map { it.hashCode() }.sorted().hashCode()
            NULL -> TODO("Unreachable")
        }
        element.type.hashCode() * 31 + valueHashCode
    }
    return typeAndValueHashCode * 31 + element.annotations.hashCode()
}

/**
 * Calculates the hash code of a [StructField].
 */
public fun hashField(structField: StructField): Int {
    return structField.name.hashCode() * 31 + structField.value.hashCode()
}
