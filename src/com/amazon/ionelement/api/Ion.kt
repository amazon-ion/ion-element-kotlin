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


@file: JvmName("Ion")
package com.amazon.ionelement.api

import com.amazon.ion.Decimal
import com.amazon.ion.Timestamp
import com.amazon.ionelement.impl.BigIntIntElementImpl
import com.amazon.ionelement.impl.BlobElementImpl
import com.amazon.ionelement.impl.BoolElementImpl
import com.amazon.ionelement.impl.ClobElementImpl
import com.amazon.ionelement.impl.DecimalElementImpl
import com.amazon.ionelement.impl.FloatElementImpl
import com.amazon.ionelement.impl.ListElementImpl
import com.amazon.ionelement.impl.LongIntElementImpl
import com.amazon.ionelement.impl.NullElementImpl
import com.amazon.ionelement.impl.SexpElementImpl
import com.amazon.ionelement.impl.StringElementImpl
import com.amazon.ionelement.impl.StructElementImpl
import com.amazon.ionelement.impl.StructFieldImpl
import com.amazon.ionelement.impl.SymbolElementImpl
import com.amazon.ionelement.impl.TimestampElementImpl
import java.math.BigInteger

/** Returns a memoized instance of [IonElement] that represents an Ion `null.null` or a typed `null`. */
fun ionNull(
    elementType: ElementType = ElementType.NULL
): IonElement =
    ALL_NULLS.getValue(elementType)

/**
 * Creates an [IonElement] that represents an Ion `null.null` or a typed `null` with the specified metas
 * and annotations. */
fun ionNull(
    elementType: ElementType = ElementType.NULL,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): IonElement =
    NullElementImpl(elementType, annotations, metas)

/** Creates a [StringElement] that represents an Ion `symbol`. */
fun ionString(
    s: String,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): StringElement = StringElementImpl(s, annotations, metas)

/** Creates a [SymbolElement] that represents an Ion `symbol`. */
fun ionSymbol(
    s: String,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): SymbolElement = SymbolElementImpl(s, annotations, metas)

/** Creates a [TimestampElement] that represents an Ion `timestamp`. */
fun ionTimestamp(
    s: String,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): TimestampElement = TimestampElementImpl(Timestamp.valueOf(s), annotations, metas)

/** Creates a [TimestampElement] that represents an Ion `timestamp`. */
fun ionTimestamp(
    timestamp: Timestamp,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): TimestampElement = TimestampElementImpl(timestamp, annotations, metas)

/** Creates an [IntElement] that represents an Ion `int`. */
fun ionInt(
    l: Long,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): IntElement =
    LongIntElementImpl(l, annotations, metas)

/** Creates an [IntElement] that represents an Ion `BitInteger`. */
fun ionInt(
    bigInt: BigInteger,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): IntElement = BigIntIntElementImpl(bigInt, annotations, metas)

/** Creates a [BoolElement] that represents an Ion `bool`. */
fun ionBool(
    b: Boolean,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): BoolElement = BoolElementImpl(b, annotations, metas)

/** Creates a [FloatElement] that represents an Ion `float`. */
fun ionFloat(
    d: Double,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): FloatElement = FloatElementImpl(d, annotations, metas)

/** Creates a [DecimalElement] that represents an Ion `decimall`. */
fun ionDecimal(
    bigDecimal: Decimal,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): DecimalElement = DecimalElementImpl(bigDecimal, annotations, metas)

/**
 * Creates a [BlobElement] that represents an Ion `blob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionBlob(
    bytes: ByteArray,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): BlobElement = BlobElementImpl(bytes.clone(), annotations, metas)

/** Returns the empty [BlobElement] singleton. */
fun emptyBlob(): BlobElement = EMPTY_BLOB

/**
 * Creates a [ClobElement] that represents an Ion `clob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionClob(
    bytes: ByteArray,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): ClobElement = ClobElementImpl(bytes.clone(), annotations, metas)

/** Returns the empty [ClobElement] singleton. */
fun emptyClob(): ClobElement = EMPTY_CLOB

/** Creates a [ListElement] that represents an Ion `list`. */
fun ionListOf(
    iterable: Iterable<IonElement>,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): ListElement =
    ListElementImpl(iterable.map { it.asAnyElement() }, annotations, metas)

/** Creates a [ListElement] that represents an Ion `list`. */
fun ionListOf(
    vararg elements: IonElement,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): ListElement =
    ionListOf(elements.asIterable(), annotations, metas)

/** Returns a [ListElement] representing an empty Ion `list`. */
fun emptyIonList(): ListElement = EMPTY_LIST

/** Creates an [SexpElement] that represents an Ion `sexp`. */
fun ionSexpOf(
    iterable: Iterable<IonElement>,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): SexpElement =
    SexpElementImpl(iterable.map { it.asAnyElement() }, annotations, metas)

/** Creates a [SexpElement] that represents an Ion `sexp`. */
fun ionSexpOf(
    vararg elements: IonElement,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): SexpElement =
    ionSexpOf(elements.asIterable(), annotations, metas)

/** Returns a [SexpElement] representing an empty Ion `sexp`. */
fun emptyIonSexp(): SexpElement = EMPTY_SEXP

/** Returns a [StructElement] representing an empty Ion `struct`. */
fun emptyIonStruct(): StructElement = EMPTY_STRUCT

/** Creates a [StructField] . */
fun field(key: String, value: IonElement): StructField =
    StructFieldImpl(key, value.asAnyElement())

/** Creates a [StructElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(
    fields: Iterable<StructField>,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): StructElement =
    StructElementImpl(fields.toList(), annotations, metas)

/** Creates a [StructElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(
    vararg fields: StructField,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): StructElement =
    ionStructOf(fields.asIterable(), annotations, metas)

/** Creates an [AnyElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(
    vararg fields: Pair<String, IonElement>,
    annotations: List<String> = emptyList(),
    metas: MetaContainer = emptyMetaContainer()
): StructElement =
    ionStructOf(fields.map { field(it.first, it.second.asAnyElement()) }, annotations, metas)

// Memoized empty instances of our container types.
private val EMPTY_LIST = ListElementImpl(emptyList(), emptyList(), emptyMetaContainer())
private val EMPTY_SEXP = SexpElementImpl(emptyList(), emptyList(), emptyMetaContainer())
private val EMPTY_STRUCT = StructElementImpl(emptyList(), emptyList(), emptyMetaContainer())
private val EMPTY_BLOB = BlobElementImpl(ByteArray(0), emptyList(), emptyMetaContainer())
private val EMPTY_CLOB = ClobElementImpl(ByteArray(0), emptyList(), emptyMetaContainer())

// Memoized instances of all of our null values.
private val ALL_NULLS = ElementType.values().map {
    it to NullElementImpl(it, emptyList(), emptyMetaContainer()) as IonElement
}.toMap()
