package com.amazon.ionelement.api

/**
 * Represents a mutable view of a collection of struct fields.
 */
public interface MutableStructFields : Iterable<StructField> {

    public val fields: MutableMap<String, MutableList<AnyElement>>

    /**
     * Retrieves the value of the first field found with the specified name.
     *
     * In the case of multiple fields with the specified name, the caller assumes that one is picked at random.
     *
     * @throws IonElementException If there are no fields with the specified [fieldName].
     */
    public operator fun get(fieldName: String): AnyElement

    /** The same as [get] but returns a null reference if the field does not exist.  */
    public fun getOptional(fieldName: String): AnyElement?

    /** Retrieves all values with a given field name. Returns an empty iterable if the field does not exist. */
    public fun getAll(fieldName: String): Iterable<AnyElement>

    /** Returns true if this StructElement has at least one field with the given field name. */
    public fun containsField(fieldName: String): Boolean

    /**
     * If one or more fields with the specified name already exists, this replaces the value of the *first* field found.
     *
     * Otherwise, a new field with the given name and value is added to the collection
     */
    public operator fun set(fieldName: String, value: IonElement): MutableStructFields

    /**
     * Adds all the given fields to the collection. For existing fields with the same names as the fields provided, all
     * instances of those fields will be removed.
     */
    public fun setAll(fields: Iterable<StructField>): MutableStructFields

    /**
     * Adds a new field to the collection with the given name and value. The collection may have multiple fields with
     * the same name.
     */
    public fun add(fieldName: String, value: IonElement): MutableStructFields

    /** Adds the given field to the collection. The collection may have multiple fields with the same name. */
    public fun add(field: StructField): MutableStructFields

    /**
     * Removes an occurrence of a field found with the given name or does nothing if no field exists.
     * If more than one field exists with the given name, one is chosen arbitrarily to be the one removed.
     */
    public fun remove(fieldName: String): MutableStructFields

    /** Removes all fields found with the given name or does nothing if no fields exist */
    public fun removeAll(fieldName: String): MutableStructFields

    /** Adds the given field to the collection */
    public operator fun plusAssign(field: StructField)

    /** Adds all the given fields to the collection */
    public operator fun plusAssign(fields: Iterable<StructField>)

    /** Creates a new instance of a StructElement from the current fields with the given annotations and metas */
    public fun toStruct(annotations: List<String>, metas: MetaContainer): StructElement

    /** Creates a new instance of a StructElement from the current fields */
    public fun toStruct(): StructElement
}
