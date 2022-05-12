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
 * generate error an message that points to the specific location of the failure within the Ion-text document
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
public interface AnyElement : IonElement {

    /** See [AnyElement]. */
    public fun asBoolean(): BoolElement

    /** See [AnyElement]. */
    public fun asBooleanOrNull(): BoolElement?

    /** See [AnyElement]. */
    public fun asInt(): IntElement

    /** See [AnyElement]. */
    public fun asIntOrNull(): IntElement?

    /** See [AnyElement]. */
    public fun asDecimal(): DecimalElement

    /** See [AnyElement]. */
    public fun asDecimalOrNull(): DecimalElement?

    /** See [AnyElement]. */
    public fun asFloat(): FloatElement

    /** See [AnyElement]. */
    public fun asFloatOrNull(): FloatElement?

    /** See [AnyElement]. */
    public fun asText(): TextElement

    /** See [AnyElement]. */
    public fun asTextOrNull(): TextElement?

    /** See [AnyElement]. */
    public fun asString(): StringElement

    /** See [AnyElement]. */
    public fun asStringOrNull(): StringElement?

    /** See [AnyElement]. */
    public fun asSymbol(): SymbolElement

    /** See [AnyElement]. */
    public fun asSymbolOrNull(): SymbolElement?

    /** See [AnyElement]. */
    public fun asTimestamp(): TimestampElement

    /** See [AnyElement]. */
    public fun asTimestampOrNull(): TimestampElement?

    /** See [AnyElement]. */
    public fun asLob(): LobElement

    /** See [AnyElement]. */
    public fun asLobOrNull(): LobElement?

    /** See [AnyElement]. */
    public fun asBlob(): BlobElement

    /** See [AnyElement]. */
    public fun asBlobOrNull(): BlobElement?

    /** See [AnyElement]. */
    public fun asClob(): ClobElement

    /** See [AnyElement]. */
    public fun asClobOrNull(): ClobElement?

    /** See [AnyElement]. */
    public fun asContainer(): ContainerElement

    /** See [AnyElement]. */
    public fun asContainerOrNull(): ContainerElement?

    /** See [AnyElement]. */
    public fun asSeq(): SeqElement

    /** See [AnyElement]. */
    public fun asSeqOrNull(): SeqElement?

    /** See [AnyElement]. */
    public fun asList(): ListElement

    /** See [AnyElement]. */
    public fun asListOrNull(): ListElement?

    /** See [AnyElement]. */
    public fun asSexp(): SexpElement

    /** See [AnyElement]. */
    public fun asSexpOrNull(): SexpElement?

    /** See [AnyElement]. */
    public fun asStruct(): StructElement

    /** See [AnyElement]. */
    public fun asStructOrNull(): StructElement?

    /**
     * If this is an Ion integer, returns its [IntElementSize] otherwise, throws [IonElementConstraintException].
     */
    public val integerSize: IntElementSize

    /** See [AnyElement]. */
    public val booleanValue: Boolean

    /** See [AnyElement]. */
    public val booleanValueOrNull: Boolean?

    /** See [AnyElement]. */
    public val longValue: Long

    /** See [AnyElement]. */
    public val longValueOrNull: Long?

    /** See [AnyElement]. */
    public val bigIntegerValue: BigInteger

    /** See [AnyElement]. */
    public val bigIntegerValueOrNull: BigInteger?

    /** See [AnyElement]. */
    public val textValue: String

    /** See [AnyElement]. */
    public val textValueOrNull: String?

    /** See [AnyElement]. */
    public val stringValue: String

    /** See [AnyElement]. */
    public val stringValueOrNull: String?

    /** See [AnyElement]. */
    public val symbolValue: String

    /** See [AnyElement]. */
    public val symbolValueOrNull: String?

    /** See [AnyElement]. */
    public val decimalValue: Decimal

    /** See [AnyElement]. */
    public val decimalValueOrNull: Decimal?

    /** See [AnyElement]. */
    public val doubleValue: Double

    /** See [AnyElement]. */
    public val doubleValueOrNull: Double?

    /** See [AnyElement]. */
    public val timestampValue: Timestamp

    /** See [AnyElement]. */
    public val timestampValueOrNull: Timestamp?

    /** See [AnyElement]. */
    public val bytesValue: ByteArrayView

    /** See [AnyElement]. */
    public val bytesValueOrNull: ByteArrayView?

    /** See [AnyElement]. */
    public val blobValue: ByteArrayView

    /** See [AnyElement]. */
    public val blobValueOrNull: ByteArrayView?

    /** See [AnyElement]. */
    public val clobValue: ByteArrayView

    /** See [AnyElement]. */
    public val clobValueOrNull: ByteArrayView?

    /** See [AnyElement]. */
    public val containerValues: Collection<AnyElement>

    /** See [AnyElement]. */
    public val containerValuesOrNull: Collection<AnyElement>?

    /** See [AnyElement]. */
    public val seqValues: List<AnyElement>

    /** See [AnyElement]. */
    public val seqValuesOrNull: List<AnyElement>?

    /** See [AnyElement]. */
    public val listValues: List<AnyElement>

    /** See [AnyElement]. */
    public val listValuesOrNull: List<AnyElement>?

    /** See [AnyElement]. */
    public val sexpValues: List<AnyElement>

    /** See [AnyElement]. */
    public val sexpValuesOrNull: List<AnyElement>?

    /** See [AnyElement]. */
    public val structFields: Collection<StructField>

    /** See [AnyElement]. */
    public val structFieldsOrNull: Collection<StructField>?

    override fun copy(annotations: List<String>, metas: MetaContainer): AnyElement
    override fun withAnnotations(vararg additionalAnnotations: String): AnyElement
    override fun withAnnotations(additionalAnnotations: Iterable<String>): AnyElement
    override fun withoutAnnotations(): AnyElement
    override fun withMetas(additionalMetas: MetaContainer): AnyElement
    override fun withMeta(key: String, value: Any): AnyElement
    override fun withoutMetas(): AnyElement
}
