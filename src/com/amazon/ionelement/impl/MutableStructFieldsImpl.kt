package com.amazon.ionelement.impl

import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.MutableStructFields
import com.amazon.ionelement.api.StructField
import com.amazon.ionelement.api.field
import java.util.NoSuchElementException

internal class MutableStructFieldsImpl(private val fields: MutableMap<String, MutableList<StructField>>) :
    MutableStructFields {
    override val size: Int
        get() = fields.values.sumBy { it.size }

    override fun get(fieldName: String): AnyElement {
        return requireNotNull(fields[fieldName]?.firstOrNull()?.value) {
            "Required struct field '$fieldName' missing"
        }
    }

    override fun getOptional(fieldName: String): AnyElement? {
        return fields[fieldName]?.firstOrNull()?.value
    }

    override fun getAll(fieldName: String): Collection<AnyElement> {
        return fields[fieldName]?.map { it.value } ?: mutableListOf()
    }

    override fun containsField(fieldName: String): Boolean {
        return fieldName in fields
    }

    override fun set(fieldName: String, value: IonElement) {
        val values = fields.getOrPut(fieldName, ::mutableListOf)
        values.clear()
        values.add(field(fieldName, value))
    }

    override fun setAll(fields: Iterable<StructField>) {
        fields.groupByTo(mutableMapOf(), { it.name }, { it.value }).forEach { (name, values) ->
            this.fields[name] = values.map { field(name, it) }.toMutableList()
        }
    }

    override fun add(fieldName: String, value: IonElement): Boolean {
        value as AnyElement
        val values = fields[fieldName]
        if (values == null) {
            fields[fieldName] = mutableListOf(field(fieldName, value))
        } else {
            values.add(field(fieldName, value))
        }
        return true
    }

    override fun add(element: StructField): Boolean {
        return add(element.name, element.value)
    }

    override fun remove(element: StructField): Boolean {
        return fields[element.name]?.remove(element) ?: false
    }

    override fun clearField(fieldName: String): Boolean {
        fields[fieldName]?.clear() ?: return false
        return true
    }

    override fun removeAll(elements: Collection<StructField>): Boolean {
        var modified = false
        for (element in elements) {
            modified = remove(element) || modified
        }
        return modified
    }

    override fun plusAssign(fields: Collection<StructField>) {
        addAll(fields)
    }

    override fun addAll(elements: Collection<StructField>): Boolean {
        elements.forEach { add(it) }
        return true
    }

    override fun clear() {
        fields.clear()
    }

    override fun contains(element: StructField): Boolean {
        return fields[element.name]?.contains(element) ?: false
    }

    override fun containsAll(elements: Collection<StructField>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean {
        return fields.all { it.value.isEmpty() }
    }

    override fun iterator(): MutableIterator<StructField> {
        return MutableStructFieldsIterator(fields)
    }

    override fun retainAll(elements: Collection<StructField>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            val field = it.next()
            if (field !in elements) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

internal class MutableStructFieldsIterator(fieldsMap: MutableMap<String, MutableList<StructField>>) :
    MutableIterator<StructField> {

    private val mapIterator = fieldsMap.iterator()
    private var listIterator: MutableIterator<StructField> =
        if (mapIterator.hasNext()) {
            mapIterator.next().value.iterator()
        } else {
            EMPTY_ITERATOR
        }

    override fun hasNext(): Boolean {
        if (listIterator.hasNext()) {
            return true
        }

        nextFieldList()

        return listIterator.hasNext()
    }

    override fun next(): StructField {
        nextFieldList()

        return listIterator.next()
    }

    override fun remove() {
        listIterator.remove()
    }

    private fun nextFieldList() {
        while (!listIterator.hasNext()) {
            if (mapIterator.hasNext()) {
                listIterator = mapIterator.next().value.iterator()
            } else {
                listIterator = EMPTY_ITERATOR
                break
            }
        }
    }

    companion object {
        private val EMPTY_ITERATOR = object : MutableIterator<StructField> {
            override fun hasNext(): Boolean {
                return false
            }

            override fun next(): StructField {
                throw NoSuchElementException()
            }

            override fun remove() {
                throw IllegalStateException()
            }
        }
    }
}
