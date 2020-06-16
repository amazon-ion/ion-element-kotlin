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
import com.amazon.ion.Timestamp
import com.amazon.ionelement.api.ElementType.BLOB
import com.amazon.ionelement.api.ElementType.BOOL
import com.amazon.ionelement.api.ElementType.CLOB
import com.amazon.ionelement.api.ElementType.DECIMAL
import com.amazon.ionelement.api.ElementType.INT
import com.amazon.ionelement.api.ElementType.LIST
import com.amazon.ionelement.api.ElementType.NULL
import com.amazon.ionelement.api.ElementType.SEXP
import com.amazon.ionelement.api.ElementType.STRING
import com.amazon.ionelement.api.ElementType.STRUCT
import com.amazon.ionelement.api.ElementType.SYMBOL
import com.amazon.ionelement.api.ElementType.TIMESTAMP
import java.math.BigInteger

/**
 * Represents an Ion element of an unknown type.
 *
 * This type is used whenever the type of Ion data is not guaranteed.  For example, it is returned by all of the
 * [IonElementLoader] functions and is the data type for children of [ListElement], [SexpElement] and [StructElement].
 * If the type of an Ion element is known in advance, it is generally better to use the narrowed [IonElement] instead.
 *
 * Two categories of methods are present on this type:
 *
 * - Value accessors (the `*Value` and `*ValueOrNull` properties)
 * - Narrowing functions (`as*()` and `as*OrNull()`
 *
 * Each of those categories also include nullable and non-nullable variations of those types.
 *
 * Use of the accessor functions allow for concise expression of two purposes simultaneously:
 *
 * - An expectation of the Ion type of the given element
 * - An expectation of the nullability of the given element
 *
 * If either of these expectations is violated and [IonElectrolyteException] is thrown.  If the given element
 * has a [IonLocation] in its metadata, it included with the [IonElectrolyteException] which can be used to
 * generate helpful error messages which point to the specific location of the failure within text or binary Ion data.
 *
 * Examples:
 *
 * ```
 * val e: AnyElement = loadSingleElement("1")
 * // throws if e is not an ion null and an [INT].
 * val value: Long = e.longValue
 * ```
 *
 * ```
 * val e: AnyElement = loadSingleElement("[1, 2]")
 * // throws if e is null or not a list or if any of its elements are null or not an INT
 * val value: List<Long> = e.listValues.map { it.longValue }
 * ```
 *
 * ```
 * val e: AnyElement = loadSingleElement("some_symbol")
 * // throws if e is not an ion null and an [SYMBOL] or [STRING].
 * val value: String = e.textValue
 * ```
 *
 * #### Deciding which accessor function to use
 *
 * **Note:  for the sake of brevity, the following section omits the nullable narrowing functions (`as*OrNull`) and
 * nullable value accessors (`*OrNull`).  These should be used wherever an Ion-null value is allowed.
 *
 * The table below shows which properties should be used to access the raw values for each [ElementType].
 *
 * | [ElementType]    | Value accessors                                 | Narrowing Functions                    |
 * |------------------------------|------------------------------------------------------------------------------|
 * | [NULL]           | (any with `OrNull` suffix)                      | (any with `OrNull` suffix)             |
 * | [BOOL]           | [booleanValue]                                  | [asBoolean]                            |
 * | [INT]            | [longValue], [bigIntegerValue]                  | [asInt]                                |
 * | [STRING]         | [textValue], [stringValue]                      | [asText], [asString]                   |
 * | [SYMBOL]         | [textValue], [symbolValue]                      | [asText], [asSymbol]                   |
 * | [DECIMAL]        | [decimalValue]                                  | [asDecimal]                            |
 * | [TIMESTAMP]      | [timestampValue]                                | [asTimestamp]                          |
 * | [CLOB]           | [bytesValue], [clobValue]                       | [asLob], [asClob]                      |
 * | [BLOB]           | [bytesValue], [blobValue]                       | [asLob], [asBlob]                      |
 * | [LIST]           | [containerValues], [seqValues], [listValues]    | [asContainer], [asSeq], [asList]       |
 * | [SEXP]           | [containerValues], [seqValues], [sexpValues]    | [asContainer], [asSeq], [asSexp]       |
 * | [STRUCT]         | [containerValues], [structFields]               | [asContainer], [asStruct]              |
 *
 * Notes:
 * - The value returned from [containerValues] when the [type] is [STRUCT] is the values within the struct,
 * without their field names.
 */
interface AnyElement : IonElement {

    /** See [AnyElement]. */
    fun asBoolean(): BoolElement

    /** See [AnyElement]. */
    fun asBooleanOrNull(): BoolElement?

    /** See [AnyElement]. */
    fun asInt(): IntElement

    /** See [AnyElement]. */
    fun asIntOrNull(): IntElement?

    /** See [AnyElement]. */
    fun asDecimal(): DecimalElement

    /** See [AnyElement]. */
    fun asDecimalOrNull(): DecimalElement?

    /** See [AnyElement]. */
    fun asFloat(): FloatElement

    /** See [AnyElement]. */
    fun asFloatOrNull(): FloatElement?

    /** See [AnyElement]. */
    fun asText(): TextElement

    /** See [AnyElement]. */
    fun asTextOrNull(): TextElement?

    /** See [AnyElement]. */
    fun asString(): StringElement

    /** See [AnyElement]. */
    fun asStringOrNull(): StringElement?

    /** See [AnyElement]. */
    fun asSymbol(): SymbolElement

    /** See [AnyElement]. */
    fun asSymbolOrNull(): SymbolElement?

    /** See [AnyElement]. */
    fun asTimestamp(): TimestampElement

    /** See [AnyElement]. */
    fun asTimestampOrNull(): TimestampElement?

    /** See [AnyElement]. */
    fun asLob(): LobElement

    /** See [AnyElement]. */
    fun asLobOrNull(): LobElement?

    /** See [AnyElement]. */
    fun asBlob(): BlobElement

    /** See [AnyElement]. */
    fun asBlobOrNull(): BlobElement?

    /** See [AnyElement]. */
    fun asClob(): ClobElement

    /** See [AnyElement]. */
    fun asClobOrNull(): ClobElement?

    /** See [AnyElement]. */
    fun asContainer(): ContainerElement

    /** See [AnyElement]. */
    fun asContainerOrNull(): ContainerElement?

    /** See [AnyElement]. */
    fun asSeq(): SeqElement

    /** See [AnyElement]. */
    fun asSeqOrNull(): SeqElement?

    /** See [AnyElement]. */
    fun asList(): ListElement

    /** See [AnyElement]. */
    fun asListOrNull(): ListElement?

    /** See [AnyElement]. */
    fun asSexp(): SexpElement

    /** See [AnyElement]. */
    fun asSexpOrNull(): SexpElement?

    /** See [AnyElement]. */
    fun asStruct(): StructElement

    /** See [AnyElement]. */
    fun asStructOrNull(): StructElement?

    /**
     * If this is an Ion integer, returns its [IntegerSize] otherwise, throws [IonElectrolyteException].
     *
     * TODO: replace with an enum type that does not include `INT`: https://github.com/amzn/ion-element-kotlin/issues/23
     */
    val integerSize: IntegerSize

    /** See [AnyElement]. */
    val booleanValue: Boolean

    /** See [AnyElement]. */
    val booleanValueOrNull: Boolean?

    /** See [AnyElement]. */
    val longValue: Long

    /** See [AnyElement]. */
    val longValueOrNull: Long?

    /** See [AnyElement]. */
    val bigIntegerValue: BigInteger

    /** See [AnyElement]. */
    val bigIntegerValueOrNull: BigInteger?

    /** See [AnyElement]. */
    val textValue: String

    /** See [AnyElement]. */
    val textValueOrNull: String?

    /** See [AnyElement]. */
    val stringValue: String

    /** See [AnyElement]. */
    val stringValueOrNull: String?

    /** See [AnyElement]. */
    val symbolValue: String

    /** See [AnyElement]. */
    val symbolValueOrNull: String?

    /** See [AnyElement]. */
    val decimalValue: Decimal

    /** See [AnyElement]. */
    val decimalValueOrNull: Decimal?

    /** See [AnyElement]. */
    val doubleValue: Double

    /** See [AnyElement]. */
    val doubleValueOrNull: Double?

    /** See [AnyElement]. */
    val timestampValue: Timestamp

    /** See [AnyElement]. */
    val timestampValueOrNull: Timestamp?

    /** See [AnyElement]. */
    val bytesValue: IonByteArray

    /** See [AnyElement]. */
    val bytesValueOrNull: IonByteArray?

    /** See [AnyElement]. */
    val blobValue: IonByteArray

    /** See [AnyElement]. */
    val blobValueOrNull: IonByteArray?

    /** See [AnyElement]. */
    val clobValue: IonByteArray

    /** See [AnyElement]. */
    val clobValueOrNull: IonByteArray?

    /** See [AnyElement]. */
    val containerValues: Iterable<AnyElement>

    /** See [AnyElement]. */
    val containerValuesOrNull: Iterable<AnyElement>?

    /** See [AnyElement]. */
    val seqValues: Iterable<AnyElement>

    /** See [AnyElement]. */
    val seqValuesOrNull: Iterable<AnyElement>?

    /** See [AnyElement]. */
    val listValues: Iterable<AnyElement>

    /** See [AnyElement]. */
    val listValuesOrNull: Iterable<AnyElement>?

    /** See [AnyElement]. */
    val sexpValues: Iterable<AnyElement>

    /** See [AnyElement]. */
    val sexpValuesOrNull: Iterable<AnyElement>?

    /** See [AnyElement]. */
    val structFields: Iterable<IonStructField>

    /** See [AnyElement]. */
    val structFieldsOrNull: Iterable<IonStructField>?

}
