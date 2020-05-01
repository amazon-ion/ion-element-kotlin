/*
 * Copyright (c) 2020. Amazon.com, Inc. or its affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.ionelement.impl

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.IonStructField
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.StructIonElement
import com.amazon.ionelement.api.emptyMetaContainer
import com.amazon.ion.IonType
import com.amazon.ion.IonWriter

internal class StructIonElementImpl(
    override val fields: List<IonStructField>,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
): IonElementBase(), StructIonElement {

    override fun iterator(): Iterator<IonStructField> = fields.iterator()

    override val type: IonType get() = IonType.STRUCT
    override val structValueOrNull: StructIonElement get() = this

    override fun firstOrNull(fieldName: String): IonElement? =
        get(fieldName)?.firstOrNull()

    override fun get(fieldName: String): Iterable<IonElement>? = fieldsByName[fieldName] ?: emptyList()

    /** Lazily calculated map of field names and lists of their values. */
    private val fieldsByName: Map<String, List<IonElement>> by lazy(LazyThreadSafetyMode.NONE) {
        fields
            .groupBy { it.name }
            .map { structFieldGroup -> structFieldGroup.key to structFieldGroup.value.map { it.value } }
            .toMap()
    }

    override val size: Int get() = fields.size
    override val fieldNames: List<String> get() = fields.map { it.name }.distinct()
    override val values: List<IonElement> get() = fields.map { it.value }

    override fun clone(annotations: List<String>, metas: MetaContainer): IonElement =
        StructIonElementImpl(fields, annotations, metas)

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
        if (this.fields.size != other.fields.size) return false
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
            val thisSubGroup: Map<IonElement, Int> = thisFieldGroup.value.groupingBy { it }.eachCount()

            // [otherGroup] should never be null due to the `if` statement above.
            val otherGroup = other.fieldsByName[thisFieldGroup.key]
                             ?: error("unexpectedly missing other field named '${thisFieldGroup.key}'")

            val otherSubGroup: Map<IonElement, Int> = otherGroup.groupingBy { it }.eachCount()

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