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

package com.amazon.ionelement.api

import com.amazon.ion.Decimal
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonType
import com.amazon.ion.IonWriter
import com.amazon.ion.Timestamp
import com.amazon.ionelement.api.IonByteArray
import com.amazon.ionelement.api.IonElementContainer
import com.amazon.ionelement.api.StructIonElement
import java.math.BigInteger

/**
 * Represents an immutable Ion value, its type annotations and metadata.
 *
 * The table below shows which properties should be used to access the raw values for each of the given [IonType]s.
 *
 * | When the IonType is... | The Valid Accessors Are (others will throw [IonElectrolyteException])      |
 * |------------------------|----------------------------------------------------------------------------|
 * | [IonType.NULL]         | ...any function with the `OrNull` suffix.                                  |
 * | [IonType.BOOL]         | [booleanValue], [booleanValueOrNull]                                       |
 * | [IonType.INT]          | [longValue], [longValueOrNull], [bigIntegerValue], [bigIntegerValueOrNull] |
 * | [IonType.STRING]       | [textValue], [textValueOrNull], [stringValue], [stringValueOrNull]         |
 * | [IonType.SYMBOL]       | [textValue], [textValueOrNull], [symbolValue], [symbolValueOrNull]         |
 * | [IonType.DECIMAL]      | [decimalValue], [decimalValueOrNull]                                       |
 * | [IonType.TIMESTAMP]    | [timestampValue], [timestampValueOrNull]                                   |
 * | [IonType.CLOB]         | [bytesValue], [bytesValueOrNull], [clobValue], [clobValueOrNull]           |
 * | [IonType.BLOB]         | [bytesValue], [bytesValueOrNull], [blobValue], [blobValueOrNull]           |
 * | [IonType.LIST]         | [containerValue], [containerValueOrNull], [listValue], [listValueOrNull]   |
 * | [IonType.SEXP]         | [containerValue], [containerValueOrNull], [sexpValue], [sexpValueOrNull]   |
 * | [IonType.STRUCT]       | [structValue], [structValueOrNull]                                         |
 *
 * These accessors can be chained together in a way that allows data to be mapped to domain objects very easily.  The
 * main benefit of this approach is that data is automatically checked to ensure it's the proper data type and
 * nullability.
 *
 * Given the Ion data:
 *
 * ```
 * stock_item::{
 *      name: "Fantastic Widget",
 *      price: 12.34,
 *      countInStock: 2,
 *      orders: [
 *          { customerId: 123, state: WA },
 *          { customerId: 456, state: "HI" }
 *      ]
 * }
 * stock_item::{ // stock item has no name
 *      price: 23.45,
 *      countInStock: 20,
 *      orders: [
 *          { customerId: 234, state: "VA" },
 *          { customerId: 567, state: MI }
 *      ]
 * }
 * ```
 *
 * The following Kotlin code can be used to deserialize it to a Kotlin class:
 *
 * ```
 * val stockItems = ION.newReader("...").use { reader ->
 *     createIonElementLoader(includeLocations = true)
 *        .loadAllElements(reader)
 *        .map { stockItem: IonElement ->
 *            stockItem.structValue.run {
 *                 StockItem(
 *                     firstOrNull("name")?.textValue ?: "<unknown name>",
 *                     first("price").decimalValue,
 *                     first("countInStock").longValue,
 *                     first("orders").containerValue.map { order ->
 *                         order.structValue.run {
 *                             Order(
 *                                 first("customerId").longValue,
 *                                 first("state").textValue)
 *                         }
 *                    })
 *             }
 *         }
 * }.asSequence().toList()
 * ```
 *
 */
interface IonElement {

    /** The Ion data type of the current node.  */
    val type: IonType

    /** This element's Ion metadata. */
    val metas: MetaContainer

    /** This element's Ion type annotations. */
    val annotations: List<String>

    /** Returns true if the current value is `null.null` or any typed null. */
    val isNull: Boolean

    /** Returns a copy of the current node with the specified additional annotations. */
    fun withAnnotations(vararg additionalAnnotations: String): IonElement

    /** Returns a copy of the current node with the specified additional annotations. */
    fun withAnnotations(additionalAnnotations: Iterable<String>): IonElement =
        withAnnotations(*additionalAnnotations.toList().toTypedArray())

    /** Returns a copy of the current node without any annotations.  (Not recursive.) */
    fun withoutAnnotations(): IonElement

    /**
     * Returns a copy of the current node with the specified additional metadata, overwriting any metas
     * that exist with the same keys.
     */
    fun withMetas(additionalMetas: MetaContainer): IonElement

    /**
     * Returns a copy of the current node with the specified additional meta, overwriting any meta
     * that previously existed with the same key.
     *
     * When adding multiple metas, consider [withMetas] instead.
     */
    fun withMeta(key: String, value: Any): IonElement = withMetas(metaContainerOf(key to value))

    /** Returns a copy of the current node without any metadata.  (Not recursive.) */
    fun withoutMetas(): IonElement

    /** If this is an Ion integer, returns its [IntegerSize] otherwise, throws [IonElectrolyteException]. */
    val integerSize: IntegerSize get() = ionError(this, "integerSize not valid for this value")

    /** See [IonElement]. */
    val booleanValue: Boolean get() = handleNull { booleanValueOrNull }

    /** See [IonElement]. */
    val booleanValueOrNull: Boolean? get() = expectNullOr(IonType.BOOL).run { null }

    /** See [IonElement]. */
    val longValue: Long get() = handleNull { longValueOrNull }

    /** See [IonElement]. */
    val longValueOrNull: Long? get() = expectNullOr(IonType.INT).run { null }

    /** See [IonElement]. */
    val bigIntegerValue: BigInteger get() = handleNull { bigIntegerValueOrNull }

    /** See [IonElement]. */
    val bigIntegerValueOrNull: BigInteger? get() = expectNullOr(IonType.INT).run { null }

    /** See [IonElement]. */
    val textValue: String get() = handleNull { textValueOrNull }

    /** See [IonElement]. */
    val textValueOrNull: String? get() = expectNullOr(IonType.STRING, IonType.SYMBOL).run { null }

    /** See [IonElement]. */
    val stringValue: String get() = handleNull { stringValueOrNull }

    /** See [IonElement]. */
    val stringValueOrNull: String? get() = expectNullOr(IonType.STRING).run { null }

    /** See [IonElement]. */
    val symbolValue: String get() = handleNull { symbolValueOrNull }

    /** See [IonElement]. */
    val symbolValueOrNull: String? get() = expectNullOr(IonType.SYMBOL).run { null }

    /** See [IonElement]. */
    val decimalValue: Decimal get() = handleNull { decimalValueOrNull }

    /** See [IonElement]. */
    val decimalValueOrNull: Decimal? get() = expectNullOr(IonType.DECIMAL).run { null }

    /** See [IonElement]. */
    val doubleValue: Double get()  = handleNull { doubleValueOrNull }

    /** See [IonElement]. */
    val doubleValueOrNull: Double? get() = expectNullOr(IonType.FLOAT).run { null }

    /** See [IonElement]. */
    val timestampValue: Timestamp get() = handleNull { timestampValueOrNull }

    /** See [IonElement]. */
    val timestampValueOrNull: Timestamp? get() = expectNullOr(IonType.TIMESTAMP).run { null }

    /** See [IonElement]. */
    val bytesValue: IonByteArray get() = handleNull { bytesValueOrNull }

    /** See [IonElement]. */
    val bytesValueOrNull: IonByteArray? get() = expectNullOr(IonType.BLOB, IonType.CLOB).run { null }

    /** See [IonElement]. */
    val blobValue: IonByteArray get() = handleNull { blobValueOrNull }

    /** See [IonElement]. */
    val blobValueOrNull: IonByteArray? get() = expectNullOr(IonType.BLOB).run { null }

    /** See [IonElement]. */
    val clobValue: IonByteArray get() = handleNull { clobValueOrNull }

    /** See [IonElement]. */
    val clobValueOrNull: IonByteArray? get() = expectNullOr(IonType.CLOB).run { null }

    /** See [IonElement]. */
    val containerValue: IonElementContainer get() = handleNull { containerValueOrNull }

    /** See [IonElement]. */
    val containerValueOrNull: IonElementContainer? get() = expectNullOr(IonType.LIST, IonType.SEXP).run { null }

    /** See [IonElement]. */
    val listValue: IonElementContainer get() = handleNull { listValueOrNull }

    /** See [IonElement]. */
    val listValueOrNull: IonElementContainer? get() = expectNullOr(IonType.LIST).run { null }

    /** See [IonElement]. */
    val sexpValue: IonElementContainer get() = handleNull { sexpValueOrNull }

    /** See [IonElement]. */
    val sexpValueOrNull: IonElementContainer? get() = expectNullOr(IonType.SEXP).run { null }

    /** See [IonElement]. */
    val structValue: StructIonElement get() = handleNull { structValueOrNull }

    /** See [IonElement]. */
    val structValueOrNull: StructIonElement? get() = expectNullOr(IonType.STRUCT).run { null }


    /** Writes the current Ion element to the specified [IonWriter]. */
    fun writeTo(writer: IonWriter)

    /** Converts the current element to Ion text. */
    override fun toString(): String

    /** Throws an [IonElectrolyteException] if the current instance was not [IonType.NULL] or in [expectedTypes]. */
    private fun expectNullOr(vararg expectedTypes: IonType) {
        if (this.type != IonType.NULL && !expectedTypes.contains(type)) {
            ionError(this, "Expected Ion value of type ${expectedTypes.joinToString(",")} but found a value of type $type")
        }
    }

    private fun <T> handleNull(block: IonElement.() -> T?): T = block() ?: ionError(this, "Unexpected Ion null value")
}
