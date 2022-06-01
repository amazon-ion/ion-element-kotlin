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
 * [SexpElement] and [StructElement]. If the type of an Ion element is not known at compile-time, the type may be easily
 * asserted at runtime and a narrower [IonElement] interface may be obtained instead.
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
 * If either of these expectations is violated an [IonElementConstraintException] is thrown.  If the given element
 * has an [IonLocation] in its metadata, it is included with the [IonElementConstraintException] which can be used to
 * generate an error message that points to the specific location of the failure within the Ion-text document
 * (i.e. line & column) or within the Ion-binary document (i.e. byte offset).
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
 * nullable value accessors (`*ValueOrNull` and `*ValuesOrNull`).  These should be used whenever an Ion-null value is
 * allowed.
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
 * the [Object.equals] and [Object.hashCode] functions that is defined by [List<T>].  This is different from equality
 * as defined by the Ion specification.
 *
 * For example:
 *
 * ```
 * val list: AnyElement = loadSingleElement("[1, 2, 3]").asAnyElement()
 * val sexp: AnyElement = loadSingleElement("(1 2 3)").asAnyElement()
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
public interface AnyElement : IonElement {

    /**
     * Attempts to narrow this element to a [BoolElement].
     * If this element is not an Ion `bool`, or if it is `null.bool`, throws an [IonElementConstraintException].
     */
    public fun asBoolean(): BoolElement

    /**
     * Attempts to narrow this element to a [BoolElement] or `null`.
     * If this element is not an Ion `bool` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asBooleanOrNull(): BoolElement?

    /**
     * Attempts to narrow this element to a [IntElement].
     * If this element is not an Ion `int`, or if it is `null.int`, throws an [IonElementConstraintException].
     */
    public fun asInt(): IntElement

    /**
     * Attempts to narrow this element to a [IntElement] or `null`.
     * If this element is not an Ion `bool` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asIntOrNull(): IntElement?

    /**
     * Attempts to narrow this element to a [DecimalElement].
     * If this element is not an Ion `decimal`, or if it is `null.decimal`, throws an [IonElementConstraintException].
     */
    public fun asDecimal(): DecimalElement

    /**
     * Attempts to narrow this element to a [DecimalElement] or `null`.
     * If this element is not an Ion `decimal` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asDecimalOrNull(): DecimalElement?

    /**
     * Attempts to narrow this element to a [FloatElement].
     * If this element is not an Ion `float`, or if it is `null.float`, throws an [IonElementConstraintException].
     */
    public fun asFloat(): FloatElement

    /**
     * Attempts to narrow this element to a [FloatElement] or `null`.
     * If this element is not an Ion `float` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asFloatOrNull(): FloatElement?

    /**
     * Attempts to narrow this element to a [TextElement].
     * If this element is not an Ion `symbol` or `string`, or if it is `null.symbol` or `null.string`, throws an
     * [IonElementConstraintException].
     */
    public fun asText(): TextElement

    /**
     * Attempts to narrow this element to a [TextElement] or `null`.
     * If this element is not an Ion `symbol`, Ion `string` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asTextOrNull(): TextElement?

    /**
     * Attempts to narrow this element to a [StringElement].
     * If this element is not an Ion `string`, or if it is `null.string`, throws an [IonElementConstraintException].
     */
    public fun asString(): StringElement

    /**
     * Attempts to narrow this element to a [StringElement] or `null`.
     * If this element is not an Ion `string` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asStringOrNull(): StringElement?

    /**
     * Attempts to narrow this element to a [SymbolElement].
     * If this element is not an Ion `symbol`, or if it is `null.symbol`, throws an [IonElementConstraintException].
     */
    public fun asSymbol(): SymbolElement

    /**
     * Attempts to narrow this element to a [SymbolElement] or `null`.
     * If this element is not an Ion `symbol` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asSymbolOrNull(): SymbolElement?

    /**
     * Attempts to narrow this element to a [TimestampElement].
     * If this element is not an Ion `timestamp`, or if it is `null.timestamp`, throws an [IonElementConstraintException].
     */
    public fun asTimestamp(): TimestampElement

    /**
     * Attempts to narrow this element to a [TimestampElement] or `null`.
     * If this element is not an Ion `timestamp` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asTimestampOrNull(): TimestampElement?

    /**
     * Attempts to narrow this element to a [LobElement].
     * If this element is not an Ion `blob` or `clob`, or if it is `null.blob` or `null.clob`, throws an
     * [IonElementConstraintException].
     */
    public fun asLob(): LobElement

    /**
     * Attempts to narrow this element to a [LobElement] or `null`.
     * If this element is not an Ion `blob`, Ion `clob`, or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asLobOrNull(): LobElement?

    /**
     * Attempts to narrow this element to a [BlobElement].
     * If this element is not an Ion `blob`, or if it is `null.blob`, throws an [IonElementConstraintException].
     */
    public fun asBlob(): BlobElement

    /**
     * Attempts to narrow this element to a [BlobElement] or `null`.
     * If this element is not an Ion `blob` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asBlobOrNull(): BlobElement?

    /**
     * Attempts to narrow this element to a [ClobElement].
     * If this element is not an Ion `clob`, or if it is `null.clob`, throws an [IonElementConstraintException].
     */
    public fun asClob(): ClobElement

    /**
     * Attempts to narrow this element to a [ClobElement] or `null`.
     * If this element is not an Ion `clob` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asClobOrNull(): ClobElement?

    /**
     * Attempts to narrow this element to a [ContainerElement].
     * If this element is not an Ion `list`, `sexp`, or `struct, or if it is any Ion `null`, throws an
     * [IonElementConstraintException].
     */
    public fun asContainer(): ContainerElement

    /**
     * Attempts to narrow this element to a [ContainerElement] or `null`.
     * If this element is not an Ion `list`, `sexp`, `struct`, or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asContainerOrNull(): ContainerElement?

    /**
     * Attempts to narrow this element to a [SeqElement].
     * If this element is not an Ion `list` or `sexp`, or if it is any Ion `null`, throws an
     * [IonElementConstraintException].
     */
    public fun asSeq(): SeqElement

    /**
     * Attempts to narrow this element to a [SeqElement] or `null`.
     * If this element is not an Ion `list`, `sexp`, or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asSeqOrNull(): SeqElement?

    /**
     * Attempts to narrow this element to a [ListElement].
     * If this element is not an Ion `list`, or if it is `null.list`, throws an [IonElementConstraintException].
     */
    public fun asList(): ListElement

    /**
     * Attempts to narrow this element to a [ListElement] or `null`.
     * If this element is not an Ion `list` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asListOrNull(): ListElement?

    /**
     * Attempts to narrow this element to a [SexpElement].
     * If this element is not an Ion `sexp`, or if it is `null.sexp`, throws an [IonElementConstraintException].
     */
    public fun asSexp(): SexpElement

    /**
     * Attempts to narrow this element to a [SexpElement] or `null`.
     * If this element is not an Ion `sexp` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asSexpOrNull(): SexpElement?

    /**
     * Attempts to narrow this element to a [StructElement].
     * If this element is not an Ion `struct`, or if it is `null.struct`, throws an [IonElementConstraintException].
     */
    public fun asStruct(): StructElement

    /**
     * Attempts to narrow this element to a [StructElement] or `null`.
     * If this element is not an Ion `struct` or `null.null`, throws an [IonElementConstraintException].
     */
    public fun asStructOrNull(): StructElement?

    /**
     * If this is an Ion integer, returns its [IntElementSize] otherwise, throws [IonElementConstraintException].
     */
    public val integerSize: IntElementSize

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `bool`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val booleanValue: Boolean

    /**
     * Gets the value of the element, assuming that it is an Ion `bool` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val booleanValueOrNull: Boolean?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `int`.
     * If that assumption is violated, throws [IonElementConstraintException].
     * Also throws [IonElementConstraintException] if the value is outside the range of a 64-bit signed integer.
     */
    public val longValue: Long

    /**
     * Gets the value of the element, assuming that it is an Ion `long` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     * Also throws [IonElementConstraintException] if the value is outside the range of a 64-bit signed integer.
     */
    public val longValueOrNull: Long?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `int`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val bigIntegerValue: BigInteger

    /**
     * Gets the value of the element, assuming that it is an Ion `int` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val bigIntegerValueOrNull: BigInteger?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `string` or `symbol`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val textValue: String

    /**
     * Gets the value of the element, assuming that it is an Ion `string` or `symbol` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val textValueOrNull: String?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `string`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val stringValue: String

    /**
     * Gets the value of the element, assuming that it is an Ion `string` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val stringValueOrNull: String?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `symbol`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val symbolValue: String

    /**
     * Gets the value of the element, assuming that it is an Ion `symbol` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val symbolValueOrNull: String?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `decimal`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val decimalValue: Decimal

    /**
     * Gets the value of the element, assuming that it is an Ion `decimal` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val decimalValueOrNull: Decimal?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `float`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val doubleValue: Double

    /**
     * Gets the value of the element, assuming that it is an Ion `float` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val doubleValueOrNull: Double?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `timestamp`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val timestampValue: Timestamp

    /**
     * Gets the value of the element, assuming that it is an Ion `timestamp` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val timestampValueOrNull: Timestamp?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `blob` or `clob`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val bytesValue: ByteArrayView

    /**
     * Gets the value of the element, assuming that it is an Ion `blob` or `clob` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val bytesValueOrNull: ByteArrayView?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `blob`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val blobValue: ByteArrayView

    /**
     * Gets the value of the element, assuming that it is an Ion `blob` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val blobValueOrNull: ByteArrayView?

    /**
     * Gets the value of the element, assuming that it is a non-null Ion `clob`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val clobValue: ByteArrayView

    /**
     * Gets the value of the element, assuming that it is an Ion `clob` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val clobValueOrNull: ByteArrayView?

    /**
     * Gets a [Collection] of the Ion elements contained in this element, assuming that it is a non-null Ion `list`, `sexp` or `struct`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val containerValues: Collection<AnyElement>

    /**
     * Gets a nullable [Collection] of the Ion elements contained in this element, assuming that it is an Ion `list`, `sexp` or `struct` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val containerValuesOrNull: Collection<AnyElement>?

    /**
     * Gets a [List] of the Ion elements contained in this element, assuming that it is a non-null Ion `list` or `sexp`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val seqValues: List<AnyElement>

    /**
     * Gets a nullable [List] of the Ion elements contained in this element, assuming that it is an Ion `list` or `sexp` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val seqValuesOrNull: List<AnyElement>?

    /**
     * Gets a [List] of the Ion elements contained in this element, assuming that it is a non-null Ion `list`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val listValues: List<AnyElement>

    /**
     * Gets a nullable [List] of the Ion elements contained in this element, assuming that it is an Ion `list` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val listValuesOrNull: List<AnyElement>?

    /**
     * Gets a [List] of the Ion elements contained in this element, assuming that it is a non-null Ion `sexp`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val sexpValues: List<AnyElement>

    /**
     * Gets a nullable [List] of the Ion elements contained in this element, assuming that it is an Ion `sexp` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val sexpValuesOrNull: List<AnyElement>?

    /**
     * Gets a [Collection] of the fields contained in this element, assuming that it is a non-null Ion `struct`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val structFields: Collection<StructField>

    /**
     * Gets a nullable [Collection] of the fields contained in this element, assuming that it is an Ion `struct` or `null.null`.
     * If that assumption is violated, throws [IonElementConstraintException].
     */
    public val structFieldsOrNull: Collection<StructField>?

    override fun copy(annotations: List<String>, metas: MetaContainer): AnyElement
    override fun withAnnotations(vararg additionalAnnotations: String): AnyElement
    override fun withAnnotations(additionalAnnotations: Iterable<String>): AnyElement
    override fun withoutAnnotations(): AnyElement
    override fun withMetas(additionalMetas: MetaContainer): AnyElement
    override fun withMeta(key: String, value: Any): AnyElement
    override fun withoutMetas(): AnyElement
}
