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
// TODO: Make make implementation names consistent.  https://github.com/amzn/ion-element-kotlin/issues/24
import com.amazon.ionelement.impl.BigIntIonElement
import com.amazon.ionelement.impl.BlobIonElement
import com.amazon.ionelement.impl.BoolIonElement
import com.amazon.ionelement.impl.ClobIonElement
import com.amazon.ionelement.impl.DecimalIonElement
import com.amazon.ionelement.impl.FloatIonElement
import com.amazon.ionelement.impl.IntIonElement
import com.amazon.ionelement.impl.StructFieldImpl
import com.amazon.ionelement.impl.ListIonElementBase
import com.amazon.ionelement.impl.NullIonElement
import com.amazon.ionelement.impl.SexpIonElementArray
import com.amazon.ionelement.impl.StringIonElement
import com.amazon.ionelement.impl.StructIonElementImpl
import com.amazon.ionelement.impl.SymbolIonElement
import com.amazon.ionelement.impl.TimestampIonElement
import java.math.BigInteger

// TODO:  add "metas: MetaContainer = emptyMetaContainer()" to all IonElement constructor functions.
// Currently, this is only present on [ionSexpOf] overloads.
// https://github.com/amzn/ion-element-kotlin/issues/6

/** Creates an [IonElement] that represents an Ion `null.null` or a typed `null`. */
@JvmOverloads
fun ionNull(elementType: ElementType = ElementType.NULL): IonElement = ALL_NULLS.getValue(elementType)

/** Creates a [StringElement] that represents an Ion `symbol`. */
fun ionString(s: String): StringElement = StringIonElement(s)

/** Creates a [SymbolElement] that represents an Ion `symbol`. */
fun ionSymbol(s: String): SymbolElement = SymbolIonElement(s)

/** Creates a [TimestampElement] that represents an Ion `timestamp`. */
fun ionTimestamp(s: String): TimestampElement = TimestampIonElement(Timestamp.valueOf(s))

/** Creates a [TimestampElement] that represents an Ion `timestamp`. */
fun ionTimestamp(timestamp: Timestamp): TimestampElement = TimestampIonElement(timestamp)

/** Creates an [IntElement] that represents an Ion `int`. */
fun ionInt(l: Long): IntElement = IntIonElement(l)

/** Creates an [IntElement] that represents an Ion `BitInteger`. */
fun ionInt(bigInt: BigInteger): IntElement = BigIntIonElement(bigInt)

/** Creates a [BoolElement] that represents an Ion `bool`. */
fun ionBool(b: Boolean): BoolElement = BoolIonElement(b)

/** Creates a [FloatElement] that represents an Ion `float`. */
fun ionFloat(d: Double): FloatElement = FloatIonElement(d)

/** Creates a [DecimalElement] that represents an Ion `decimall`. */
fun ionDecimal(bigDecimal: Decimal): DecimalElement = DecimalIonElement(bigDecimal)

/**
 * Creates a [BlobElement] that represents an Ion `blob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionBlob(bytes: ByteArray): BlobElement = BlobIonElement(bytes.clone())

/** Returns the empty [BlobElement] singleton. */
fun emptyBlob(): BlobElement = EMPTY_BLOB

/**
 * Creates a [ClobElement] that represents an Ion `clob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionClob(bytes: ByteArray): ClobElement = ClobIonElement(bytes.clone())

/** Returns the empty [ClobElement] singleton. */
fun emptyClob(): ClobElement = EMPTY_CLOB

/** Creates a [ListElement] that represents an Ion `list`. */
fun ionListOf(iterable: Iterable<IonElement>): ListElement =
    when {
        iterable.none() -> EMPTY_LIST
        else -> ListIonElementBase(iterable.map { it.asAnyElement() })
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
        else -> SexpIonElementArray(iterable.map { it.asAnyElement() }, metas = metas)
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
        else -> StructIonElementImpl(fields.toList())
    }

/** Creates a [StructElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: StructField): StructElement =
    ionStructOf(fields.asIterable())

/** Creates an [AnyElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: Pair<String, IonElement>): StructElement =
    ionStructOf(fields.map { field(it.first, it.second.asAnyElement()) })

// Memoized empty instances of our container types.
private val EMPTY_LIST = ListIonElementBase(emptyList())
private val EMPTY_SEXP = SexpIonElementArray(emptyList())
private val EMPTY_STRUCT = StructIonElementImpl(emptyList())
private val EMPTY_BLOB = BlobIonElement(ByteArray(0))
private val EMPTY_CLOB = ClobIonElement(ByteArray(0))

// Memoized instances of all of our null values.
private val ALL_NULLS = ElementType.values().map { it to NullIonElement(it) as IonElement }.toMap()
