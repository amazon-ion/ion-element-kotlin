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

import com.amazon.ion.Decimal
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonType
import com.amazon.ion.Timestamp
import java.math.BigInteger

/**
 * Represents an immutable Ion value, its type annotations and metadata.
 *
 * TODO: include guidance explaining that [IonElement] should be used whenever the type of the Ion data is not
 * guaranteed.  Data type and nullability constraints can be concisely expressed with the `*Value` and `*ValueOrNull`
 * functions.  Type safety is also possible with the `as*[OrNull]` functions.
 *
 * The table below shows which properties should be used to access the raw values for each of the given [IonType]s.
 *
 * TODO: the table below is out of date.
 *
 * | When the [ElementType] is... | The Valid Accessors Are (others will throw [IonElectrolyteException])      |
 * |------------------------------|----------------------------------------------------------------------------|
 * | [ElementType.NULL]           | ...any function with the `OrNull` suffix.                                  |
 * | [ElementType.BOOL]           | [booleanValue], [booleanValueOrNull], [asBoolean], [asBooleanOrNull]       |
 * | [ElementType.INT]            | [longValue], [longValueOrNull], [bigIntegerValue], [bigIntegerValueOrNull], [asInt], [asIntOrNull] |
 * | [ElementType.STRING]         | [textValue], [textValueOrNull], [stringValue], [stringValueOrNull],         |
 * | [ElementType.SYMBOL]         | [textValue], [textValueOrNull], [symbolValue], [symbolValueOrNull]         |
 * | [ElementType.DECIMAL]        | [decimalValue], [decimalValueOrNull]                                       |
 * | [ElementType.TIMESTAMP]      | [timestampValue], [timestampValueOrNull]                                   |
 * | [ElementType.CLOB]           | [bytesValue], [bytesValueOrNull], [clobValue], [clobValueOrNull]           |
 * | [ElementType.BLOB]           | [bytesValue], [bytesValueOrNull], [blobValue], [blobValueOrNull]           |
 * | [ElementType.LIST]           | [asContainer], [asContainerOrNull], [asSeq], [asSeqOrNull], [asList] [asSexpOrNull]   |
 * | [ElementType.SEXP]           | [asContainer], [asContainerOrNull], [asSeq], [asSeqOrNull], [asSexp] [asSexpOrNull]   |
 * | [ElementType.STRUCT]         | [asContainer], [asContainerOrNull], [asStruct] [asStructOrNull] |
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
 */
interface IonElement : Element {

    /** See [IonElement]. */
    fun asBoolean(): BoolElement

    /** See [IonElement]. */
    fun asBooleanOrNull(): BoolElement?

    /** See [IonElement]. */
    fun asInt(): IntElement

    /** See [IonElement]. */
    fun asIntOrNull(): IntElement?

    /** See [IonElement]. */
    fun asDecimal(): DecimalElement

    /** See [IonElement]. */
    fun asDecimalOrNull(): DecimalElement?

    /** See [IonElement]. */
    fun asDouble(): FloatElement

    /** See [IonElement]. */
    fun asDoubleOrNull(): FloatElement?

    /** See [IonElement]. */
    fun asText(): TextElement

    /** See [IonElement]. */
    fun asTextOrNull(): TextElement?

    /** See [IonElement]. */
    fun asString(): StringElement

    /** See [IonElement]. */
    fun asStringOrNull(): StringElement?

    /** See [IonElement]. */
    fun asSymbol(): SymbolElement

    /** See [IonElement]. */
    fun asSymbolOrNull(): SymbolElement?

    /** See [IonElement]. */
    fun asTimestamp(): TimestampElement

    /** See [IonElement]. */
    fun asTimestampOrNull(): TimestampElement?

    /** See [IonElement]. */
    fun asLob(): LobElement

    /** See [IonElement]. */
    fun asLobOrNull(): LobElement?

    /** See [IonElement]. */
    fun asBlob(): BlobElement

    /** See [IonElement]. */
    fun asBlobOrNull(): BlobElement?

    /** See [IonElement]. */
    fun asClob(): ClobElement

    /** See [IonElement]. */
    fun asClobOrNull(): ClobElement?

    /** See [IonElement]. */
    fun asContainer(): ContainerElement

    /** See [IonElement]. */
    fun asContainerOrNull(): ContainerElement?

    /** See [IonElement]. */
    fun asSeq(): SeqElement

    /** See [IonElement]. */
    fun asSeqOrNull(): SeqElement?

    /** See [IonElement]. */
    fun asList(): ListElement

    /** See [IonElement]. */
    fun asListOrNull(): ListElement?

    /** See [IonElement]. */
    fun asSexp(): SexpElement

    /** See [IonElement]. */
    fun asSexpOrNull(): SexpElement?

    /** See [IonElement]. */
    fun asStruct(): StructElement

    /** See [IonElement]. */
    fun asStructOrNull(): StructElement?

    /**
     * If this is an Ion integer, returns its [IntegerSize] otherwise, throws [IonElectrolyteException].
     *
     * TODO: replace with an enum type that does not include `INT`: https://github.com/amzn/ion-element-kotlin/issues/23
     */
    val integerSize: IntegerSize

    /** See [IonElement]. */
    val booleanValue: Boolean

    /** See [IonElement]. */
    val booleanValueOrNull: Boolean?

    /** See [IonElement]. */
    val longValue: Long

    /** See [IonElement]. */
    val longValueOrNull: Long?

    /** See [IonElement]. */
    val bigIntegerValue: BigInteger

    /** See [IonElement]. */
    val bigIntegerValueOrNull: BigInteger?

    /** See [IonElement]. */
    val textValue: String

    /** See [IonElement]. */
    val textValueOrNull: String?

    /** See [IonElement]. */
    val stringValue: String

    /** See [IonElement]. */
    val stringValueOrNull: String?

    /** See [IonElement]. */
    val symbolValue: String

    /** See [IonElement]. */
    val symbolValueOrNull: String?

    /** See [IonElement]. */
    val decimalValue: Decimal

    /** See [IonElement]. */
    val decimalValueOrNull: Decimal?

    /** See [IonElement]. */
    val doubleValue: Double

    /** See [IonElement]. */
    val doubleValueOrNull: Double?

    /** See [IonElement]. */
    val timestampValue: Timestamp

    /** See [IonElement]. */
    val timestampValueOrNull: Timestamp?

    /** See [IonElement]. */
    val bytesValue: IonByteArray

    /** See [IonElement]. */
    val bytesValueOrNull: IonByteArray?

    /** See [IonElement]. */
    val blobValue: IonByteArray

    /** See [IonElement]. */
    val blobValueOrNull: IonByteArray?

    /** See [IonElement]. */
    val clobValue: IonByteArray

    /** See [IonElement]. */
    val clobValueOrNull: IonByteArray?
}
