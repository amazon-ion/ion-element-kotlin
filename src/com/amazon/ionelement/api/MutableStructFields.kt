package com.amazon.ionelement.api

/**
 * Represents a mutable view of a collection of struct fields.
 */
public interface MutableStructFields : MutableCollection<StructField> {

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
    public fun getAll(fieldName: String): Collection<AnyElement>

    /** Returns true if this StructElement has at least one field with the given field name. */
    public fun containsField(fieldName: String): Boolean

    /**
     * If one or more fields with the specified name already exists, this replaces all of them with the value provided.
     *
     * Otherwise, a new field with the given name and value is added to the collection.
     *
     * Returns true if a field was replaced.
     */
    public operator fun set(fieldName: String, value: IonElement)

    /**
     * Adds all the given fields to the collection. For existing fields with the same names as the fields provided, all
     * instances of those fields will be removed.
     */
    public fun setAll(fields: Iterable<StructField>)

    /**
     * Adds a new field to the collection with the given name and value. The collection may have multiple fields with
     * the same name.
     */
    public fun add(fieldName: String, value: IonElement): Boolean

    /** Adds the given field to the collection. The collection may have multiple fields with the same name.
     *
     * Repeated fields are allowed, so this will always return true. */
    public override fun add(element: StructField): Boolean

    /** Removes a random occurrence of a field the matches the given field, or does nothing if no field exists.
     *
     * Returns true is a field was removed. */
    public override fun remove(element: StructField): Boolean

    /** Removes all occurrence of a field the matches the given field name, or does nothing if no field exists.
     *
     * Returns true if a field was removed. */
    public fun clearField(fieldName: String): Boolean

    /** Removes all of this collection's elements that are also contained in the specified collection.
     *
     *  At most one field per element in the give collection is removed. */
    public override fun removeAll(elements: Collection<StructField>): Boolean

    /** Adds all the given fields to the collection */
    public operator fun plusAssign(fields: Collection<StructField>)
}
