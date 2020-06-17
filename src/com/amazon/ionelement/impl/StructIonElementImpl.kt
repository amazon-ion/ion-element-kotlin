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
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.IonStructField
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.StructElement
import com.amazon.ionelement.api.emptyMetaContainer
import com.amazon.ionelement.api.ionError

internal class StructIonElementImpl(
    private val allFields: List<IonStructField>,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
): AnyElementBase(), StructElement {

    override val type: ElementType get() = ElementType.STRUCT
    override val size = allFields.size
    override val values: Collection<AnyElement> by lazy(LazyThreadSafetyMode.NONE) { fields.map { it.value }}
    override val containerValues: Iterable<AnyElement> get() = values
    override val structFields: Iterable<IonStructField> get() = fields
    override val fields: Iterable<IonStructField> get() = allFields

    /** Lazily calculated map of field names and lists of their values. */
    private val fieldsByName: Map<String, List<AnyElement>> by lazy(LazyThreadSafetyMode.NONE) {
        fields
            .groupBy { it.name }
            .map { structFieldGroup -> structFieldGroup.key to structFieldGroup.value.map { it.value } }
            .toMap()
    }

    override fun get(fieldName: String): AnyElement =
        fieldsByName[fieldName]?.firstOrNull() ?: ionError(this, "Required struct field '$fieldName' missing")

    override fun getOptional(fieldName: String): AnyElement? =
        fieldsByName[fieldName]?.firstOrNull()

    override fun getAll(fieldName: String): Iterable<AnyElement> = fieldsByName[fieldName] ?: emptyList()

    override fun copy(annotations: List<String>, metas: MetaContainer): AnyElement =
        StructIonElementImpl(allFields, annotations, metas)

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
        if (other !is StructIonElementImpl) return false
        if (annotations != other.annotations) return false

        // We might avoid materializing fieldsByName by checking fields.size first
        if (this.size != other.size) return false
        if (this.fieldsByName.size != other.fieldsByName.size) return false

        // If we make it this far we can compare the list of field names in both
        if(this.fieldsByName.keys != other.fieldsByName.keys) return false

        // If we make it this far then we have to take the expensive approach of comparing the individual values in
        // [this] and [other].
        //
        // A field group is a list of fields with the same name. Within each field group we count the number of times
        // each value appears in both [this] and [other]. Each field group is equivalent if every value that appears n
        // times in one group also appears n times in the other group.

        this.fieldsByName.forEach { thisFieldGroup ->
            val thisSubGroup: Map<AnyElement, Int> = thisFieldGroup.value.groupingBy { it }.eachCount()

            // [otherGroup] should never be null due to the `if` statement above.
            val otherGroup = other.fieldsByName[thisFieldGroup.key]
                             ?: error("unexpectedly missing other field named '${thisFieldGroup.key}'")

            val otherSubGroup: Map<AnyElement, Int> = otherGroup.groupingBy { it }.eachCount()

            // Simple equality should work from here
            if(thisSubGroup != otherSubGroup) {
                return false
            }
        }

        // Metas intentionally not included here.

        return true
    }

    private val cachedHashCode by lazy(LazyThreadSafetyMode.NONE) {
        // Sorting the hash codes of the individual fields makes their order irrelevant.
        var result = fields.map { it.hashCode() }.sorted().hashCode()

        result = 31 * result + annotations.hashCode()

        // Metas intentionally not included here.
        result
    }

    override fun hashCode(): Int = cachedHashCode
}