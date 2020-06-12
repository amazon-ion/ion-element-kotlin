package com.amazon.ionelement.api

import com.amazon.ion.Decimal
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonWriter
import com.amazon.ion.Timestamp
import java.math.BigInteger

// QUESTION/TODO:  Verify that we are consistent about using *either* the Kotlin type name or the Ion type name.
// i.e. Ion uses "float" file Kotlin's equivalent is "Double".  Int vs Long, etc.  I think in some places
// we use one or the other but the intent is that the only place we use the Ion type name is for members of ElementType.
// everywhere else should refer to the Kotlin type name.
// The question here is: is this a good idea or if we should just use the Ion type name everywhere?

/**
 * TODO:  probably length documentation.
 *
 * TODO: when writing documentation, note that every implementation of [Element] must also implement [IonElement].
 * The reason:  has to do with the need for all constructor functions accept [Element] but for the collection elements
 * to return instances of [IonElement].
 *
 */
interface Element {
    /** The Ion data type of the current node.  */
    val type: ElementType

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

    /** Writes the current Ion element to the specified [IonWriter]. */
    fun writeTo(writer: IonWriter)

    /** Converts the current element to Ion text. */
    override fun toString(): String
}


/**
 * Represents an Ion null value.  (Typed or untyped.)
 *
 * Includes no additional functionality over [Element], but serves to provide additional type safety when
 * working with elements that represnt Ion null values..
 */
interface NullElement : Element

// TODO: "Bool" or "Boolean"
/** Represents a Ion bool. */
interface BooleanElement : Element {
    val booleanValue: Boolean
}

/** Represents a Ion bool. */
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
 * Represents a Ion bool.
 *
 * QUESTION/TODO: "Double" to be consistent with Kotlin or "Float" to be consistent with Ion?
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
 * working with elements that must be an Ion strings.
 */
interface StringElement : TextElement

/**
 * Represents an Ion symbol.
 *
 * Includes no additional functionality over [TextElement], but serves to provide additional type safety when
 * working with elements that must be an Ion strings.
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
 * working with elements that must be an Ion strings.
 */
interface BlobElement : LobElement

/**
 * Represents an Ion clob.
 *
 * Includes no additional functionality over [LobElement], but serves to provide additional type safety when
 * working with elements that must be an Ion strings.
 */
interface ClobElement : LobElement

/** Represents an Ion list, s-expression or struct. */
interface ContainerElement : Element {
    val values: Collection<IonElement>
}

/**
 * Represents an Ion list or s-expression.
 *
 * Includes common functional the list operations such as [head] and [tail].
 */
interface SeqElement : ContainerElement {
    override val values: List<IonElement>

    /**
     * Returns the string representation of the symbol in the first element of this container.
     *
     * If the first element is not a symbol or this container has no elements, throws [IonElectrolyteException],
     */
    val tag get() = this.head.symbolValue

    /**
     * Returns the first element of this container.
     *
     * If this container has no elements, throws [IonElectrolyteException].
     */
    val head: IonElement
        get() =
            when (this.values.size) {
                0 -> ionError(this, "Cannot get head of empty container")
                else -> this.values.first()
            }

    /**
     * Returns a sub-list containing all elements of this container except the first.
     *
     * If this container has no elements, throws [IonElectrolyteException].
     */
    val tail: List<IonElement> get()  =
        when (this.values.size) {
            0 -> ionError(this, "Cannot get tail of empty container")
            else -> this.values.subList(1, this.values.size)
        }
}

/**
 * Represents an Ion list.
 *
 * Includes no additional functionality over [SeqElement], but serves to provide additional type safety when
 * working with elements that must be an Ion list.
 */
interface ListElement : SeqElement

/**
 * Represents an Ion s-expression.
 *
 * Includes no additional functionality over [SeqElement], but serves to provide additional type safety when
 * working with elements that must be an Ion s-expression.
 */
interface SexpElement : SeqElement

/**
 * Represents an Ion struct.
 *
 * Includes functions for accessing the fields of a struct.
 */
interface StructElement : ContainerElement {

    /** This struct's unordered collection of fields. */
    val fields: Collection<IonStructField>

    /** A list of the unique field names contained within this struct. */
    val fieldNames: List<String>

    /**
     * Retrieves the value of the first field found with the specified name.
     * The search order is undefined.
     *
     * @throws IonElectrolyteException If there are no fields with the specified [fieldName].
     */
    operator fun get(fieldName: String): IonElement

    /**
     * Retrieves the first field found with the specified name or `null` if no such field exists.
     *
     * This is the same as [get], except it returns `null` instead of throwing if the field doesn't exist.
     *
     * QUESTION/TODO:  I shied a way from the "OrNull" suffix for this one since we use that term for different purpose
     * in IonElement.  Functional
     */
    fun findOne(fieldName: String): IonElement?

    /**
     * Retrieves all fields or an empty iterable if there are no field names.
     *
     * QUESTION/TODO: is this the correct return type?
     */
    fun getAll(fieldName: String): Iterable<IonElement>
}



