package com.amazon.ionelement.api

import com.amazon.ion.Decimal
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonWriter
import com.amazon.ion.Timestamp
import java.math.BigInteger

/**
 * Represents an immutable Ion element.
 *
 * Specifies the contract that is common to all Ion elements but does not specify the type of data being represented.
 *
 * #### IonElement Hierarchy
 *
 * Each type in the following hierarchy extends [IonElement] by adding strongly typed accessor functions that
 * correspond to one or more Ion data types.  Except for [AnyElement], the types inheriting from [IonElement] are
 * referred to as "narrow types".
 *
 * - [IonElement]
 *     - [AnyElement]
 *     - [BoolElement]
 *     - [IntElement]
 *     - [FloatElement]
 *     - [DecimalElement]
 *     - [TimestampElement]
 *     - [TextElement]
 *         - [StringElement]
 *         - [SymbolElement]
 *     - [LobElement]
 *         - [BlobElement]
 *         - [ClobElement]
 *     - [ContainerElement]
 *         - [SeqElement]
 *             - [ListElement]
 *             - [SexpElement]
 *         - [StructElement]
 *
 * #### Equivalence
 *
 * All implementations of [IonElement] implement [Object.equals] and [Object.hashCode] according to the Ion
 * specification.
 *
 * Collections returned from the following properties implement [Object.equals] and [Object.hashCode] according to the
 * requirements of [List<T>], wherein order is significant.
 *
 * - [ContainerElement.values]
 * - [SeqElement.values]
 * - [ListElement.values]
 * - [SexpElement.values]
 * - [StructElement.values]

 * Be aware that this can yield inconsistent results when working with structs, due to their unordered nature.
 *
 * ```
 * val s = loadSingleElement("{ a: 1, b: 2 }").asStruct()
 * val l = loadSingleElement("[1, 2]").asList()
 *
 * // The following has an undefined result because the order of values returned by [StructElement.values] is not
 * // guaranteed:
 *
 * s.values.equals(l.values)
 * ```
 *
 * When in doubt, prefer use of [Object.equals] and [Object.hashCode] on the [IonElement] instance.
 */
interface IonElement {

    /**
     * All [IonElement] implementations must convertible to [AnyElement].
     *
     * Since all [IonElement] implementations in this library also implement [AnyElement] this is no more
     * expensive than a cast.  The purpose of this interface function is to be very clear about the requirement
     * that all implementations of [IonElement] are convertible to [AnyElement].
     */
    fun asAnyElement(): AnyElement

    /** The Ion data type of the current node.  */
    val type: ElementType

    /** This [IonElement]'s metadata. */
    val metas: MetaContainer

    /** This element's Ion type annotations. */
    val annotations: List<String>

    /** Returns true if the current value is `null.null` or any typed null. */
    val isNull: Boolean

    /** Returns a shallow copy of the current node, replacing the annotations and metas with those specified. */
    fun copy(annotations: List<String> = this.annotations, metas: MetaContainer = this.metas): IonElement

    /** Writes the current Ion element to the specified [IonWriter]. */
    fun writeTo(writer: IonWriter)

    /** Converts the current element to Ion text. */
    override fun toString(): String
}


/** Represents a Ion bool. */
interface BoolElement : IonElement {
    val booleanValue: Boolean
    override fun copy(annotations: List<String>, metas: MetaContainer): BoolElement
}

/** Represents a Ion timestamp. */
interface TimestampElement : IonElement {
    val timestampValue: Timestamp
    override fun copy(annotations: List<String>, metas: MetaContainer): TimestampElement
}

/** Represents a Ion int. */
interface IntElement : IonElement {
    val integerSize: IntegerSize
    val longValue: Long
    val bigIntegerValue: BigInteger
    override fun copy(annotations: List<String>, metas: MetaContainer): IntElement
}

/** Represents a Ion decimal. */
interface DecimalElement : IonElement {
    val decimalValue: Decimal
    override fun copy(annotations: List<String>, metas: MetaContainer): DecimalElement
}

/**
 * Represents a Ion float.
 */
interface FloatElement : IonElement {
    val doubleValue: Double
    override fun copy(annotations: List<String>, metas: MetaContainer): FloatElement
}

/** Represents an Ion string or symbol. */
interface TextElement : IonElement {
    val textValue: String
    override fun copy(annotations: List<String>, metas: MetaContainer): TextElement
}

/**
 * Represents an Ion string.
 *
 * Includes no additional functionality over [TextElement], but serves to provide additional type safety when
 * working with elements that must be Ion strings.
 */
interface StringElement : TextElement {
    override fun copy(annotations: List<String>, metas: MetaContainer): StringElement
}

/**
 * Represents an Ion symbol.
 *
 * Includes no additional functionality over [TextElement], but serves to provide additional type safety when
 * working with elements that must be Ion symbols.
 */
interface SymbolElement : TextElement {
    override fun copy(annotations: List<String>, metas: MetaContainer): SymbolElement
}

/** Represents an Ion clob or blob. */
interface LobElement : IonElement {
    val bytesValue:  ByteArrayView
    override fun copy(annotations: List<String>, metas: MetaContainer): LobElement
}

/**
 * Represents an Ion blob.
 *
 * Includes no additional functionality over [LobElement], but serves to provide additional type safety when
 * working with elements that must be Ion blobs.
 */
interface BlobElement : LobElement {
    override fun copy(annotations: List<String>, metas: MetaContainer): BlobElement
}

/**
 * Represents an Ion clob.
 *
 * Includes no additional functionality over [LobElement], but serves to provide additional type safety when
 * working with elements that must be Ion clobs.
 */
interface ClobElement : LobElement {
    override fun copy(annotations: List<String>, metas: MetaContainer): ClobElement
}

/**
 * Represents an Ion list, s-expression or struct.
 *
 * Items within [values] may or may not be in a defined order.  The order is defined for lists and s-expressions,
 * but undefined for structs.
 *
 * #### Equality
 *
 * See the note about equivalence in the documentation for [IonElement].
 *
 * @see [IonElement]
 */
interface ContainerElement : IonElement {
    /** The number of values in this container. */
    val size: Int

    val values: Collection<AnyElement>

    override fun copy(annotations: List<String>, metas: MetaContainer): ContainerElement
}

/**
 * Represents an ordered collection element such as an Ion list or s-expression.
 *
 * Includes no additional functionality over [ContainerElement], but serves to provide additional type safety when
 * working with ordered collection elements.
 *
 * #### Equivalence
 *
 * See the note about equivalence in the documentation for [IonElement].
 *
 * @see [IonElement]
 */
interface SeqElement : ContainerElement {
    override fun copy(annotations: List<String>, metas: MetaContainer): SeqElement

    /** Narrows the return type of [ContainerElement.values] to [List<AnyElement>]. */
    override val values: List<AnyElement>
}
/**
 * Represents an Ion list.
 *
 * Includes no additional functionality over [SeqElement], but serves to provide additional type safety when
 * working with elements that must be Ion lists.
 *
 * #### Equivalence
 *
 * See the note about equivalence in the documentation for [IonElement].
 *
 * @see [IonElement]
 */
interface ListElement : SeqElement {
    override fun copy(annotations: List<String>, metas: MetaContainer): ListElement
}

/**
 * Represents an Ion s-expression.
 *
 * Includes no additional functionality over [SeqElement], but serves to provide additional type safety when
 * working with elements that must be Ion s-expressions.
 *
 * #### Equivalence
 *
 * See the note about equivalence in the documentation for [IonElement].
 *
 * @see [IonElement]
 */
interface SexpElement : SeqElement {
    override fun copy(annotations: List<String>, metas: MetaContainer): SexpElement
}

/**
 * Represents an Ion struct.
 *
 * Includes functions for accessing the fields of a struct.
 *
 * #### Equivalence
 *
 * See the note about equivalence in the documentation for [IonElement].
 *
 * @see [IonElement]
 */
interface StructElement : ContainerElement {

    /** This struct's unordered collection of fields. */
    val fields: Collection<StructField>

    /**
     * Retrieves the value of the first field found with the specified name.
     *
     * In the case of multiple fields with the specified name, the caller assume that one is picked at random.
     *
     * @throws IonElectrolyteException If there are no fields with the specified [fieldName].
     */
    operator fun get(fieldName: String): AnyElement

    /** The same as [get] but returns a null reference if the field does not exist.  */
    fun getOptional(fieldName: String): AnyElement?


    /** Retrieves all values with a given field name. Returns an empty iterable if the field does not exist. */
    fun getAll(fieldName: String): Iterable<AnyElement>

    override fun copy(annotations: List<String>, metas: MetaContainer): StructElement
}
