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

package com.amazon.ionelement.impl

import com.amazon.ion.IonType
import com.amazon.ion.IonWriter
import com.amazon.ionelement.api.*
import com.amazon.ionelement.api.ImmutableMetaContainer
import com.amazon.ionelement.api.constraintError
import com.amazon.ionelement.impl.collections.*
import java.util.function.Consumer

// TODO: Consider creating a StructElement variant with optimizations that assume no duplicate field names.
internal class StructElementImpl(
    private val allFields: ImmutableList<StructField>,
    override val annotations: ImmutableList<String>,
    override val metas: ImmutableMetaContainer
) : AnyElementBase(), StructElement {

    override val type: ElementType get() = ElementType.STRUCT
    override val size: Int get() = allFields.size

    // Note that we are not using `by lazy` here because it requires 2 additional allocations and
    // has been demonstrated to significantly increase memory consumption!
    private var valuesBackingField: ImmutableList<AnyElement>? = null
    override val values: Collection<AnyElement>
        get() {
            if (valuesBackingField == null) {
                valuesBackingField = fields.map { it.value }.toImmutableListUnsafe()
            }
            return valuesBackingField!!
        }
    override val containerValues: Collection<AnyElement> get() = values
    override val structFields: Collection<StructField> get() = allFields
    override val fields: Collection<StructField> get() = allFields

    // Note that we are not using `by lazy` here because it requires 2 additional allocations and
    // has been demonstrated to significantly increase memory consumption!
    private var fieldsByNameBackingField: ImmutableMap<String, ImmutableList<AnyElement>>? = null

    /** Lazily calculated map of field names and lists of their values. */
    private val fieldsByName: Map<String, List<AnyElement>>
        get() {
            if (fieldsByNameBackingField == null) {
                fieldsByNameBackingField =
                    fields
                        .groupBy { it.name }
                        .map { structFieldGroup -> structFieldGroup.key to structFieldGroup.value.map { it.value }.toImmutableListUnsafe() }
                        .toMap()
                        .toImmutableMapUnsafe()
            }
            return fieldsByNameBackingField!!
        }

    override fun mutableFields(): MutableStructFields {
        val internalMap = mutableMapOf<String, MutableList<StructField>>()
        return MutableStructFieldsImpl(
            fieldsByName.mapValuesTo(internalMap) { (name, values) ->
                values.map { field(name, it) }.toMutableList()
            }
        )
    }

    override fun update(mutator: MutableStructFields.() -> Unit): StructElement {
        val mutableFields = mutableFields()
        mutableFields.apply(mutator)
        return ionStructOf(mutableFields, annotations, metas)
    }

    override fun update(mutator: Consumer<MutableStructFields>): StructElement {
        val mutableFields = mutableFields()
        mutator.accept(mutableFields)
        return ionStructOf(mutableFields, annotations, metas)
    }

    override fun get(fieldName: String): AnyElement =
        fieldsByName[fieldName]?.firstOrNull() ?: constraintError(this, "Required struct field '$fieldName' missing")

    override fun getOptional(fieldName: String): AnyElement? =
        fieldsByName[fieldName]?.firstOrNull()

    override fun getAll(fieldName: String): Iterable<AnyElement> = fieldsByName[fieldName] ?: emptyList()

    override fun containsField(fieldName: String): Boolean = fieldsByName.containsKey(fieldName)

    override fun copy(annotations: List<String>, metas: MetaContainer): StructElementImpl =
        StructElementImpl(allFields, annotations.toImmutableList(), metas.toImmutableMap())

    override fun withAnnotations(vararg additionalAnnotations: String): StructElementImpl = _withAnnotations(*additionalAnnotations)

    override fun withAnnotations(additionalAnnotations: Iterable<String>): StructElementImpl = _withAnnotations(additionalAnnotations)

    override fun withoutAnnotations(): StructElementImpl = _withoutAnnotations()
    override fun withMetas(additionalMetas: MetaContainer): StructElementImpl = _withMetas(additionalMetas)
    override fun withMeta(key: String, value: Any): StructElementImpl = _withMeta(key, value)
    override fun withoutMetas(): StructElementImpl = _withoutMetas()

    override fun writeContentTo(writer: IonWriter) {
        writer.stepIn(IonType.STRUCT)
        fields.forEach {
            writer.setFieldName(it.name)
            it.value.writeTo(writer)
        }
        writer.stepOut()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructElement) return false
        if (annotations != other.annotations) return false

        // We might avoid potentially expensive checks if the `fields` are the same instance, which
        // could occur if the only difference between the two StructElements is the metas.
        if (fields !== other.fields) {

            // We might avoid materializing fieldsByName by checking fields.size first
            if (this.size != other.size) return false

            if (other is StructElementImpl) {
                if (this.fieldsByName.size != other.fieldsByName.size) return false
                // If we make it this far we can compare the list of field names in both
                if (this.fieldsByName.keys != other.fieldsByName.keys) return false
            }

            // This is potentially expensive, but so is a deep comparison, and at least hashcode can be cached.
            if (this.hashCode() != other.hashCode()) return false

            // Compare the frequency of every StructField to make sure they occur the same number of times.
            if (fieldCounts() != other.fieldCounts()) return false
        }
        // Metas intentionally not included here.

        return true
    }

    /** Creates a map of [StructField] to its [Int] frequency in this [StructElement]. */
    private fun StructElement.fieldCounts(): Map<StructField, Int> {
        val counts = mutableMapOf<StructField, Int>()
        fields.forEach { counts.increment(it) }
        return counts
    }

    /**
     * Increments the value for [key]. If [key] is not present, the current value is assumed to be 0 and [key] is added
     * to the map with a value of 1.
     */
    private fun <K> MutableMap<K, Int>.increment(key: K) {
        set(key, 1 + (get(key) ?: 0))
    }

    // Note that we are not using `by lazy` here because it requires 2 additional allocations and
    // has been demonstrated to significantly increase memory consumption!
    private var cachedHashCode: Int? = null
    override fun hashCode(): Int {
        if (this.cachedHashCode == null) {
            cachedHashCode = hashElement(this)
        }
        return this.cachedHashCode!!
    }
}
