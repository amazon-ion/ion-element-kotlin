/*
 * Copyright (c) 2020. Amazon.com, Inc. or its affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


@file: JvmName("Ion")
package com.amazon.ionelement.api

import com.amazon.ionelement.impl.BigIntIonElement
import com.amazon.ionelement.impl.BlobIonElement
import com.amazon.ionelement.impl.BoolIonElement
import com.amazon.ionelement.impl.ClobIonElement
import com.amazon.ionelement.impl.DecimalIonElement
import com.amazon.ionelement.impl.FloatIonElement
import com.amazon.ionelement.impl.IntIonElement
import com.amazon.ionelement.impl.IonStructFieldImpl
import com.amazon.ionelement.impl.ListIonElementArray
import com.amazon.ionelement.impl.NullIonElement
import com.amazon.ionelement.impl.SexpIonElementArray
import com.amazon.ionelement.impl.StringIonElement
import com.amazon.ionelement.impl.StructIonElementImpl
import com.amazon.ionelement.impl.SymbolIonElement
import com.amazon.ionelement.impl.TimestampIonElement
import com.amazon.ion.Decimal
import com.amazon.ion.IonType
import com.amazon.ion.Timestamp
import java.math.BigInteger

// TODO:  add "metas: MetaContainer = emptyMetaContainer()" to all IonElement constructor functions.
// Currently, this is only present on [ionSexpOf] overloads.

/** Creates an [IonElement] that represents an Ion `null.null` or a typed `null`.*/
@JvmOverloads
fun ionNull(ionType: IonType = IonType.NULL): IonElement = ALL_NULLS.getValue(ionType)

/** Creates an [IonElement] that represents an Ion `symbol`.*/
fun ionString(s: String?): IonElement =
    s?.let { StringIonElement(it) } ?: ionNull(IonType.STRING)

/** Creates an [IonElement] that represents an Ion `symbol`.*/
fun ionSymbol(s: String?): IonElement =
    s?.let { SymbolIonElement(it) } ?: ionNull(IonType.SYMBOL)

/** Creates an IonElement that represents an Ion `timestamp`.*/
fun ionTimestamp(s: String?): IonElement =
    s?.let { TimestampIonElement(Timestamp.valueOf(s)) } ?: ionNull(IonType.TIMESTAMP)

/** Creates an [IonElement] that represents an Ion `timestamp`.*/
fun ionTimestamp(timestamp: Timestamp?): IonElement =
    timestamp?.let { TimestampIonElement(timestamp) } ?: ionNull(IonType.TIMESTAMP)

/** Creates an [IonElement] that represents an Ion `int`.*/
fun ionInt(l: Long?): IonElement =
    l?.let { IntIonElement(it) } ?: ionNull(IonType.INT)

/** Creates an [IonElement] that represents an Ion `BitInteger`.*/
fun ionInt(bigInt: BigInteger?): IonElement =
    bigInt?.let { BigIntIonElement(it) } ?: ionNull(IonType.INT)

/** Creates an [IonElement] that represents an Ion `bool`.*/
fun ionBool(b: Boolean?): IonElement =
    b?.let { BoolIonElement(it) } ?: ionNull(IonType.BOOL)

/** Creates an [IonElement] that represents an Ion `float`.*/
fun ionFloat(d: Double?): IonElement =
    d?.let { FloatIonElement(it) } ?: ionNull(IonType.FLOAT)

/** Creates an [IonElement] that represents an Ion `decimall`.*/
fun ionDecimal(bigDecimal: Decimal?): IonElement =
    bigDecimal?.let { DecimalIonElement(bigDecimal) } ?: ionNull(IonType.DECIMAL)

/**
 * Creates an [IonElement] that represents an Ion `blob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionBlob(bytes: ByteArray?): IonElement =
    bytes?.let { BlobIonElement(bytes.clone()) } ?: ionNull(IonType.BLOB)

fun emptyBlob(): IonElement = EMPTY_BLOB

/**
 * Creates an [IonElement] that represents an Ion `clob`.
 *
 * Note that the [ByteArray] is cloned so immutability can be enforced.
 */
fun ionClob(bytes: ByteArray?): IonElement =
    bytes?.let { ClobIonElement(bytes.clone()) } ?: ionNull(IonType.CLOB)

fun emptyClob(): IonElement = EMPTY_CLOB

/** Creates an [IonElement] that represents an Ion `list`.*/
fun ionListOf(iterable: Iterable<IonElement>): IonElementContainer =
    when {
        iterable.none() -> EMPTY_LIST
        else -> ListIonElementArray(iterable.toList())
    }

/** Creates an [IonElement] that represents an Ion `list`.*/
fun ionListOf(vararg elements: IonElement): IonElementContainer =
    ionListOf(elements.asIterable())

/** Creates an [IonElement] that represents an Ion `sexp`.*/
fun ionSexpOf(iterable: Iterable<IonElement>, metas: MetaContainer = emptyMetaContainer()): IonElementContainer =
    when {
        iterable.none() -> EMPTY_SEXP
        else -> SexpIonElementArray(iterable.toList(), metas = metas)
    }

/** Creates an [IonElement] that represents an Ion `sexp`.*/
fun ionSexpOf(vararg elements: IonElement, metas: MetaContainer = emptyMetaContainer()): IonElementContainer =
    ionSexpOf(elements.asIterable(), metas)

/** Returns an [IonElement] representing an empty Ion `list` .*/
fun emptyIonList(): IonElementContainer = EMPTY_LIST

/** Returns an [IonElement] representing an empty Ion `sexp` .*/
fun emptyIonSexp(): IonElementContainer = EMPTY_SEXP

/** Returns an [IonElement] representing an empty Ion `struct` .*/
fun emptyIonStruct(): IonElement = EMPTY_STRUCT

/** Creates an [IonStructField] .*/
fun field(key: String, value: IonElement): IonStructField =
    IonStructFieldImpl(key, value)

/** Creates an [IonElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(fields: Iterable<IonStructField>): IonElement =
    when {
        fields.none() -> EMPTY_STRUCT
        else -> StructIonElementImpl(fields.toList())
    }

/** Creates an [IonElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: IonStructField): IonElement =
    ionStructOf(fields.asIterable())

/** Creates an [IonElement] that represents an Ion `struct` with the specified fields. */
fun ionStructOf(vararg fields: Pair<String, IonElement>): IonElement =
    ionStructOf(fields.map { field(it.first, it.second) })

// Memoized empty instances of our container types.
private val EMPTY_LIST: IonElementContainer = ListIonElementArray(emptyList())
private val EMPTY_SEXP: IonElementContainer = SexpIonElementArray(emptyList())
private val EMPTY_STRUCT = StructIonElementImpl(emptyList())
private val EMPTY_BLOB = BlobIonElement(ByteArray(0))
private val EMPTY_CLOB = ClobIonElement(ByteArray(0))

// Memoized instances of all of our null values.
private val ALL_NULLS = IonType.values().map { it to NullIonElement(it) }.toMap()
