package com.amazon.ionelement.api

import com.amazon.ion.Decimal
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonWriter
import com.amazon.ion.Timestamp
import java.math.BigInteger

/**
 * TODO:  probably lengthy documentation.
 *
 */
interface Element {

    /**
     * All [Element] implementations must convertible to [IonElement].
     *
     * Since all [Element] implementations in this library also implement [IonElement] this is no more
     * expensive than a cast.  The purpose of this interface function is to be very clear about the requirement
     * that all implementations of [Element] are convertible to [IonElement].
     */
    fun asIonElement(): IonElement

    /** The Ion data type of the current node.  */
    val type: ElementType

    /** This [Element]'s metadata. */
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
interface BoolElement : Element {
    val booleanValue: Boolean
}

/** Represents a Ion timestamp. */
interface TimestampElement : Element {
    val timestampValue: Timestamp
}

/** Represents a Ion int. */
interface IntElement : Element {
    val integerSize: IntegerSize
    val longValue: Long
    val bigIntegerValue: BigInteger
}

/** Represents a Ion decimal. */
interface DecimalElement : Element {
    val decimalValue: Decimal
}

/**
 * Represents a Ion float.
 */
interface FloatElement : Element {
    val doubleValue: Double
}

/** Represents an Ion string or symbol. */
interface TextElement : Element {
    val textValue: String
}
/**
 * Represents an Ion string.
 *
 * Includes no additional functionality over [TextElement], but serves to provide additional type safety when
 * working with elements that must be Ion strings.
 */
interface StringElement : TextElement

/**
 * Represents an Ion symbol.
 *
 * Includes no additional functionality over [TextElement], but serves to provide additional type safety when
 * working with elements that must be Ion symbols.
 */
interface SymbolElement : TextElement

/** Represents an Ion clob or blob. */
interface LobElement : Element {
    val bytesValue:  IonByteArray
}

/**
 * Represents an Ion blob.
 *
 * Includes no additional functionality over [LobElement], but serves to provide additional type safety when
 * working with elements that must be Ion blobs.
 */
interface BlobElement : LobElement

/**
 * Represents an Ion clob.
 *
 * Includes no additional functionality over [LobElement], but serves to provide additional type safety when
 * working with elements that must be Ion clobs.
 */
interface ClobElement : LobElement

/**
 * Represents an Ion list, s-expression or struct.
 *
 * Items within [values] may or may not be in a defined order.  The order is defined for lists and s-expressions,
 * but undefined for structs.
 */
interface ContainerElement : Element {
    /** The number of values in this container. */
    val size: Int

    val values: Iterable<IonElement>
}

/**
 * Represents an ordered collection element such as an Ion list or s-expression.
 *
 * Includes no additional functionality over [ContainerElement], but serves to provide additional type safety when
 * working with ordered collection elements.
 */
interface SeqElement : ContainerElement
/**
 * Represents an Ion list.
 *
 * Includes no additional functionality over [SeqElement], but serves to provide additional type safety when
 * working with elements that must be Ion lists.
 */
interface ListElement : SeqElement

/**
 * Represents an Ion s-expression.
 *
 * Includes no additional functionality over [SeqElement], but serves to provide additional type safety when
 * working with elements that must be Ion s-expressions.
 */
interface SexpElement : SeqElement

/**
 * Represents an Ion struct.
 *
 * Includes functions for accessing the fields of a struct.
 */
interface StructElement : ContainerElement {

    /** This struct's unordered collection of fields. */
    val fields: Iterable<IonStructField>

    /** A list of the unique field names contained within this struct. */
    val fieldNames: Iterable<String>

    /**
     * Retrieves the value of the first field found with the specified name.
     *
     * In the case of multiple fields with the specified name, the caller assume that one is picked at random.
     *
     * @throws IonElectrolyteException If there are no fields with the specified [fieldName].
     */
    operator fun get(fieldName: String): IonElement

    /** The same as [get] but returns a null reference if the field does not exist.  */
    fun getOptional(fieldName: String): IonElement?


    /** Retrieves all values with a given field name, which may be none if the field does not exist. */
    fun getAll(fieldName: String): Iterable<IonElement>
}
