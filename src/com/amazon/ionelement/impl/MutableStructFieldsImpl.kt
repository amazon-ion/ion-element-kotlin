package com.amazon.ionelement.impl

import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.MutableStructFields
import com.amazon.ionelement.api.StructField
import com.amazon.ionelement.api.field

internal class MutableStructFieldsImpl(private val fields: MutableMap<String, MutableList<AnyElement>>) :
    MutableStructFields {

    override fun get(fieldName: String): AnyElement {
        return requireNotNull(fields[fieldName]?.firstOrNull()) {
            "Required struct field '$fieldName' missing"
        }
    }

    override fun getOptional(fieldName: String): AnyElement? {
        return fields[fieldName]?.firstOrNull()
    }

    override fun getAll(fieldName: String): Collection<AnyElement> {
        return fields[fieldName] ?: mutableListOf()
    }

    override fun containsField(fieldName: String): Boolean {
        return fieldName in fields
    }

    override fun set(fieldName: String, value: IonElement): MutableStructFields {
        val values = fields.getOrPut(fieldName, ::mutableListOf)
        values.clear()
        values.add(value as AnyElement)
        return this
    }

    override fun setAll(fields: Iterable<StructField>): MutableStructFields {
        fields.groupByTo(mutableMapOf(), { it.name }, { it.value }).forEach { (name, values) ->
            this.fields[name] = values
        }
        return this
    }

    override fun add(fieldName: String, value: IonElement): MutableStructFields {
        value as AnyElement
        val values = fields[fieldName]
        if (values == null) {
            fields[fieldName] = mutableListOf(value)
        } else {
            values.add(value)
        }

        return this
    }

    override fun add(field: StructField): MutableStructFields {
        add(field.name, field.value)
        return this
    }

    override fun remove(field: StructField): MutableStructFields {
        fields[field.name]?.remove(field.value)
        return this
    }

    override fun removeAll(fieldName: String): MutableStructFields {
        fields[fieldName]?.clear()
        return this
    }

    override fun plusAssign(field: StructField) {
        add(field.name, field.value)
    }

    override fun plusAssign(fields: Iterable<StructField>) {
        fields.forEach { add(it) }
    }

    override fun iterator(): Iterator<StructField> {
        return fields.flatMap { (fieldName, values) ->
            values.map { value ->
                field(fieldName, value)
            }
        }.iterator()
    }
}
