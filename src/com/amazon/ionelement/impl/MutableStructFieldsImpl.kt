package com.amazon.ionelement.impl

import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.MutableStructFields
import com.amazon.ionelement.api.StructElement
import com.amazon.ionelement.api.StructField
import com.amazon.ionelement.api.emptyIonStruct
import com.amazon.ionelement.api.field
import com.amazon.ionelement.api.ionStructOf

internal class MutableStructFieldsImpl(override val fields: MutableMap<String, MutableList<AnyElement>>) :
    MutableStructFields {

    override fun get(fieldName: String): AnyElement {
        return requireNotNull(fields[fieldName]?.firstOrNull()) {
            "Required struct field '$fieldName' missing"
        }
    }

    override fun getOptional(fieldName: String): AnyElement? {
        return fields[fieldName]?.firstOrNull()
    }

    override fun getAll(fieldName: String): Iterable<AnyElement> {
        return fields[fieldName] ?: mutableListOf()
    }

    override fun containsField(fieldName: String): Boolean {
        return fieldName in fields
    }

    override fun set(fieldName: String, value: IonElement): MutableStructFields {
        value as AnyElement
        if (fieldName in fields) {
            fields[fieldName]?.clear()
            fields[fieldName]?.add(value)
        } else {
            fields[fieldName] = mutableListOf(value)
        }

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

    override fun remove(fieldName: String): MutableStructFields {
        fields[fieldName]?.removeAt(0)
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

    override fun toStruct(annotations: List<String>, metas: MetaContainer): StructElement {
        return ionStructOf(this, annotations, metas)
    }

    override fun toStruct(): StructElement {
        return ionStructOf(this)
    }

    override fun iterator(): Iterator<StructField> {
        return fields.flatMap { (fieldName, values) ->
            values.map { value ->
                field(fieldName, value)
            }
        }.iterator()
    }
}

public fun buildStruct(body: MutableStructFields.() -> Unit): StructElement {
    val fields = emptyIonStruct().mutableFields
    body(fields)
    return fields.toStruct(emptyList(), emptyMap())
}
