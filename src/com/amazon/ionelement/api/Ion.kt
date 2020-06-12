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
import com.amazon.ionelement.impl.BigIntIonElement
import com.amazon.ionelement.impl.BlobIonElement
import com.amazon.ionelement.impl.BoolIonElement
import com.amazon.ionelement.impl.ClobIonElement
import com.amazon.ionelement.impl.DecimalIonElement
import com.amazon.ionelement.impl.FloatIonElement
import com.amazon.ionelement.impl.IntIonElement
import com.amazon.ionelement.impl.IonStructFieldImpl
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

/*
TODO:  explain in greater detail that constructor functions in this file all return the narrowed interface
corresponding to data type while the container constructors all accept instances of [Element].  Every
instance of [Element] that's passed in to become a container child is is cast to [IonElement].

This allows these functions to be combined to create nested Ion data.

    ionInt(1) // returns [IntElement]

    val aList = ionListOf(ionInt(1)) // accepts [Element] and returns [ListElement]

Note that: aList.values is list of [IonElement].

This is one reason every [Element] implementation must also be an [IonElement].
*/

//

/** Creates a [NullElement] that represents an Ion `null.null` or a typed `null`.*/
@JvmOverloads
fun ionNull(elementType: ElementType = ElementType.NULL): NullElement = ALL_NULLS.getValue(elementType)

/** Creates a [StringElement] that represents an Ion `symbol`.*/
fun ionString(s: String): StringElement = StringIonElement(s)

/** Creates a [SymbolElement] that represents an Ion `symbol`.*/
fun ionSymbol(s: String): SymbolElement = SymbolIonElement(s)

/** Creates a [TimestampElement] that represents an Ion `timestamp`.*/
fun ionTimestamp(s: String): TimestampElement = TimestampIonElement(Timestamp.valueOf(s))

/** Creates a [TimestampElement] that represents an Ion `timestamp`.*/
fun ionTimestamp(timestamp: Timestamp): TimestampElement = TimestampIonElement(timestamp)

/** Creates an [IntElement] that represents an Ion `int`.*/
fun ionInt(l: Long): IntElement = IntIonElement(l)

/** Creates an [IntElement] that represents an Ion `BitInteger`.*/
fun ionInt(bigInt: BigInteger): IntElement = BigIntIonElement(bigInt)

/** Creates a [BooleanElement] that represents an Ion `bool`.*/
fun ionBool(b: Boolean): BooleanElement = BoolIonElement(b)

/** Creates a [FloatElement] that represents an Ion `float`.*/
fun ionFloat(d: Double): FloatElement = FloatIonElement(d)

/** Creates a [DecimalElement] that represents an Ion `decimall`.*/
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

/** Creates a [ListElement] that represents an Ion `list`.*/
fun ionListOf(iterable: Iterable<Element>): ListElement =
    when {
        iterable.none() -> EMPTY_LIST
        else -> ListIonElementBase(iterable.map { it as IonElement })
    }

/** Creates a [ListElement] that represents an Ion `list`.*/
fun ionListOf(vararg elements: Element): ListElement =
    ionListOf(elements.asIterable())

/** Creates a [SexpElement] that represents an Ion `sexp`.*/
fun ionSexpOf(iterable: Iterable<Element>, metas: MetaContainer = emptyMetaContainer()): SexpElement =
    when {
        iterable.none() -> EMPTY_SEXP
        else -> SexpIonElementArray(iterable.map { it as IonElement }, metas = metas)
    }

/** Creates a [SexpElement] that represents an Ion `sexp`.*/
fun ionSexpOf(vararg elements: Element, metas: MetaContainer = emptyMetaContainer()): SexpElement =
    ionSexpOf(elements.asIterable(), metas)

/** Returns a [ListElement] representing an empty Ion `list` .*/
fun emptyIonList(): ListElement = EMPTY_LIST

/** Returns a [SexpElement] representing an empty Ion `sexp` .*/
fun emptyIonSexp(): SexpElement = EMPTY_SEXP

/** Returns a [StructElement] representing an empty Ion `struct` .*/
fun emptyIonStruct(): StructElement = EMPTY_STRUCT

/** Creates an [IonStructField] .*/
fun field(key: String, value: Element): IonStructField =
    IonStructFieldImpl(key, value as IonElement)

/** Creates a [StructElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(fields: Iterable<IonStructField>): StructElement =
    when {
        fields.none() -> EMPTY_STRUCT
        else -> StructIonElementImpl(fields.toList())
    }

/** Creates a [StructElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: IonStructField): StructElement =
    ionStructOf(fields.asIterable())

/** Creates an [IonElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: Pair<String, Element>): StructElement =
    ionStructOf(fields.map { field(it.first, it.second as IonElement) })

// Memoized empty instances of our container types.
private val EMPTY_LIST = ListIonElementBase(emptyList())
private val EMPTY_SEXP = SexpIonElementArray(emptyList())
private val EMPTY_STRUCT = StructIonElementImpl(emptyList())
private val EMPTY_BLOB = BlobIonElement(ByteArray(0))
private val EMPTY_CLOB = ClobIonElement(ByteArray(0))

// Memoized instances of all of our null values.
private val ALL_NULLS = ElementType.values().map { it to NullIonElement(it) }.toMap()
