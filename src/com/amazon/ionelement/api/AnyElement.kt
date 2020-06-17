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
 * Represents an Ion element whose type is not known at compile-time.
 *
 * [IonElement] is returned by all of the [IonElementLoader] functions and is the data type for children of [ListElement],
 * [SexpElement] and [StructElement]. If the type of an Ion element is known in advance, one can use the narrowed
 * [IonElement] APIs that can be asserted at runtime.
 *
 * Two categories of methods are present on this type:
 *
 * - Value accessors (the `*Value` and `*ValueOrNull` properties)
 * - Narrowing functions (`as*()` and `as*OrNull()`
 *
 * Use of the accessor functions allow for concise expression of two purposes simultaneously:
 *
 * - An expectation of the Ion type of the given element
 * - An expectation of the nullability of the given element
 *
 * If either of these expectations is violated an [IonElementException] is thrown.  If the given element
 * has a [IonLocation] in its metadata, it included with the [IonElementException] which can be used to
 * generate error an message that points to the specific location of the failure within text (i.e. line & column) or
 * binary Ion data (i.e. byte offset).
 *
 * Value Accessor Examples:
 *
 * ```
 * val e: AnyElement = loadSingleElement("1")
 * val value: Long = e.longValue
 * // e.longValue throws if e is null or not an INT
 * ```
 *
 * ```
 * val e: AnyElement = loadSingleElement("[1, 2]")
 * val values: List<Long> = e.listValues.map { it.longValue }
 * // e.listValues.map { it.longValue } throws if:
 * //  - e is null or not a list
 * //  - any child element in e is null or not an int
 * ```
 *
 * ```
 * val e: AnyElement = loadSingleElement("some_symbol")
 * val value: String = e.textValue
 * // throws if (e is null) or (not a STRING or SYMBOL).
 * ```
 *
 * Narrowing Function Examples:
 *
 * ```
 * val e: AnyElement = loadSingleElement("1")
 * val n: IntElement = e.asInt()
 * // e.asInt() throws if e is null or not an INT
 * ```
 *
 * ```
 * val e: AnyElement = loadSingleElement("[1, 2]")
 * val l: ListElement = e.asList()
 * // e.asList() throws if e is null or not a list
 *
 * val values: List<IntElement> = l.values.map { it.asInt() }
 * // l.values.map { it.asInt() } throws if: any child element in l is null or not an int
 * ```
 *
 * ```
 * val e: AnyElement = loadSingleElement("some_symbol")
 * val t: String = e.textValue
 * // throws if (e is null) or (not a STRING or SYMBOL).
 * ```
 *
 * #### Deciding which accessor function to use
 *
 * **Note:  for the sake of brevity, the following section omits the nullable narrowing functions (`as*OrNull`) and
 * nullable value accessors (`*OrNull`).  These should be used whenever an Ion-null value is allowed.
 *
 * The table below shows which accessor functions can be used for each [ElementType].
 *
 * | [ElementType]    | Value Accessors                                 | Narrowing Functions                    |
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
 * - The value returned from [containerValues] when the [type] is [STRUCT] is the values within the struct without
 * their field names.
 *
 * #### Equality
 *
 * Any accessor function returning [Collection<AnyElement>] or [List<AnyElement]] uses the definition of equality for
 * the [Object.equals] and [Object.hashCode] functions that is defined by [List<T>].  This is different than equality
 * as defined by the Ion specification.
 *
 * For example:
 *
 * ```
 * val list: AnyElement = loadSingleElement("[1, 2, 3]").asAnyElement()
 * val sexp: AnyElement = loadsingleElement("(1 2 3)").asAnyElement()
 *
 * // The following is `true` because equality is defined by `List<T>`.
 * sexp.values.equals(list.sexp.values)
 *
 * // The following is false because equality is defined by the Ion specification which specifies that lists and
 * // s-expressions are not equivalent.
 * sexp.equals(list)
 * ```
 *
 * @see [IonElement]
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
     * If this is an Ion integer, returns its [IntegerSize] otherwise, throws [IonElementException].
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
    val containerValues: Collection<AnyElement>

    /** See [AnyElement]. */
    val containerValuesOrNull: Collection<AnyElement>?

    /** See [AnyElement]. */
    val seqValues: List<AnyElement>

    /** See [AnyElement]. */
    val seqValuesOrNull: List<AnyElement>?

    /** See [AnyElement]. */
    val listValues: List<AnyElement>

    /** See [AnyElement]. */
    val listValuesOrNull: List<AnyElement>?

    /** See [AnyElement]. */
    val sexpValues: List<AnyElement>

    /** See [AnyElement]. */
    val sexpValuesOrNull: List<AnyElement>?

    /** See [AnyElement]. */
    val structFields: Collection<IonStructField>

    /** See [AnyElement]. */
    val structFieldsOrNull: Collection<IonStructField>?

}
