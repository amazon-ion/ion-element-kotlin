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
import com.amazon.ionelement.impl.LongIntElementImpl
import com.amazon.ionelement.impl.StructFieldImpl
import com.amazon.ionelement.impl.ListElementImpl
import com.amazon.ionelement.impl.NullElementImpl
import com.amazon.ionelement.impl.SexpElementImpl
import com.amazon.ionelement.impl.StringElementImpl
import com.amazon.ionelement.impl.StructElementImpl
import com.amazon.ionelement.impl.SymbolElementImpl
import com.amazon.ionelement.impl.TimestampElementImpl
import java.math.BigInteger

// TODO:  add "metas: MetaContainer = emptyMetaContainer()" to all IonElement constructor functions.
// Currently, this is only present on [ionSexpOf] overloads.
// https://github.com/amzn/ion-element-kotlin/issues/6

/** Creates an [IonElement] that represents an Ion `null.null` or a typed `null`. */
@JvmOverloads
fun ionNull(elementType: ElementType = ElementType.NULL): IonElement = ALL_NULLS.getValue(elementType)

/** Creates a [StringElement] that represents an Ion `symbol`. */
fun ionString(s: String): StringElement = StringElementImpl(s)

/** Creates a [SymbolElement] that represents an Ion `symbol`. */
fun ionSymbol(s: String): SymbolElement = SymbolElementImpl(s)

/** Creates a [TimestampElement] that represents an Ion `timestamp`. */
fun ionTimestamp(s: String): TimestampElement = TimestampElementImpl(Timestamp.valueOf(s))

/** Creates a [TimestampElement] that represents an Ion `timestamp`. */
fun ionTimestamp(timestamp: Timestamp): TimestampElement = TimestampElementImpl(timestamp)

/** Creates an [IntElement] that represents an Ion `int`. */
fun ionInt(l: Long): IntElement = LongIntElementImpl(l)

/** Creates an [IntElement] that represents an Ion `BitInteger`. */
fun ionInt(bigInt: BigInteger): IntElement = BigIntIntElementImpl(bigInt)

/** Creates a [BoolElement] that represents an Ion `bool`. */
fun ionBool(b: Boolean): BoolElement = BoolElementImpl(b)

/** Creates a [FloatElement] that represents an Ion `float`. */
fun ionFloat(d: Double): FloatElement = FloatElementImpl(d)

/** Creates a [DecimalElement] that represents an Ion `decimall`. */
fun ionDecimal(bigDecimal: Decimal): DecimalElement = DecimalElementImpl(bigDecimal)

/**
 * Creates a [BlobElement] that represents an Ion `blob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionBlob(bytes: ByteArray): BlobElement = BlobElementImpl(bytes.clone())

/** Returns the empty [BlobElement] singleton. */
fun emptyBlob(): BlobElement = EMPTY_BLOB

/**
 * Creates a [ClobElement] that represents an Ion `clob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionClob(bytes: ByteArray): ClobElement = ClobElementImpl(bytes.clone())

/** Returns the empty [ClobElement] singleton. */
fun emptyClob(): ClobElement = EMPTY_CLOB

/** Creates a [ListElement] that represents an Ion `list`. */
fun ionListOf(iterable: Iterable<IonElement>): ListElement =
    when {
        iterable.none() -> EMPTY_LIST
        else -> ListElementImpl(iterable.map { it.asAnyElement() })
    }

/** Creates a [ListElement] that represents an Ion `list`. */
fun ionListOf(vararg elements: IonElement): ListElement =
    ionListOf(elements.asIterable())

/** Returns a [ListElement] representing an empty Ion `list`. */
fun emptyIonList(): ListElement = EMPTY_LIST

/** Creates an [SexpElement] that represents an Ion `sexp`. */
fun ionSexpOf(iterable: Iterable<IonElement>, metas: MetaContainer = emptyMetaContainer()): SexpElement =
    when {
        iterable.none() -> EMPTY_SEXP
        else -> SexpElementImpl(iterable.map { it.asAnyElement() }, metas = metas)
    }

/** Creates a [SexpElement] that represents an Ion `sexp`. */
fun ionSexpOf(vararg elements: IonElement, metas: MetaContainer = emptyMetaContainer()): SexpElement =
    ionSexpOf(elements.asIterable(), metas)

/** Returns a [SexpElement] representing an empty Ion `sexp`. */
fun emptyIonSexp(): SexpElement = EMPTY_SEXP

/** Returns a [StructElement] representing an empty Ion `struct`. */
fun emptyIonStruct(): StructElement = EMPTY_STRUCT

/** Creates a [StructField] . */
fun field(key: String, value: IonElement): StructField =
    StructFieldImpl(key, value.asAnyElement())

/** Creates a [StructElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(fields: Iterable<StructField>): StructElement =
    when {
        fields.none() -> EMPTY_STRUCT
        else -> StructElementImpl(fields.toList())
    }

/** Creates a [StructElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: StructField): StructElement =
    ionStructOf(fields.asIterable())

/** Creates an [AnyElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: Pair<String, IonElement>): StructElement =
    ionStructOf(fields.map { field(it.first, it.second.asAnyElement()) })

// Memoized empty instances of our container types.
private val EMPTY_LIST = ListElementImpl(emptyList())
private val EMPTY_SEXP = SexpElementImpl(emptyList())
private val EMPTY_STRUCT = StructElementImpl(emptyList())
private val EMPTY_BLOB = BlobElementImpl(ByteArray(0))
private val EMPTY_CLOB = ClobElementImpl(ByteArray(0))

// Memoized instances of all of our null values.
private val ALL_NULLS = ElementType.values().map { it to NullElementImpl(it) as IonElement }.toMap()
