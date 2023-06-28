package com.amazon.ionelement.api

import com.amazon.ionelement.api.StructUpdateBuilder.FieldUpdateMode.*
import com.amazon.ionelement.impl.*
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

public class IonPath(public val elements: List<IonPathElement>) : Collection<IonPathElement> by elements {
    public constructor(vararg elements: IonPathElement) : this(elements.asList())

    public fun tail(): IonPath {
        return IonPath(elements.drop(1))
    }
}

public sealed class IonPathElement

public data class Field(val fieldName: String) : IonPathElement()

public data class Index(val index: Int) : IonPathElement()

public tailrec fun IonElement.getPath(path: IonPath): IonElement {
    return when (path.isEmpty()) {
        true -> this
        else -> when (val element = path.first()) {
            is Field -> this.asAnyElement().asStruct()[element.fieldName].getPath(path.tail())
            is Index -> this.asAnyElement().asSeq().values[element.index].getPath(path.tail())
        }
    }
}

public tailrec fun IonElement.getPath(vararg path: String): IonElement {
    return when (path.isEmpty()) {
        true -> this
        false -> this.asAnyElement().asStruct()[path.first()].getPath(*path.drop(1).toTypedArray())
    }
}

public operator fun IonElement.get(path: IonPath): IonElement {
    return this.getPath(path)
}

public class StructUpdateBuilder(
    private val sourceStruct: StructElement,
    private val defaultMode: FieldUpdateMode
) {
    public enum class FieldUpdateMode {
        REPLACE,
        ADD
    }

    private val fieldUpdates: MutableMap<String, MutableList<AnyElement>> =
        sourceStruct.fields.groupBy({ it.name }, { it.value })
            .mapValues { it.value.toMutableList() }
            .toMutableMap()

    public operator fun set(fieldName: String, value: IonElement) {
        set(fieldName, value, defaultMode == REPLACE)
    }

    public fun set(fieldName: String, value: IonElement, mode: FieldUpdateMode) {
        set(fieldName, value, mode == REPLACE)
    }

    private fun set(fieldName: String, value: IonElement, clearExistingValues: Boolean) {
        fieldUpdates.compute(fieldName) { _, oldValues ->
            if (oldValues == null) {
                mutableListOf(value.asAnyElement())
            } else {
                if (clearExistingValues) {
                    oldValues.clear()
                }
                oldValues.add(value.asAnyElement())
                oldValues
            }
        }
    }

    public operator fun get(fieldName: String): AnyElement {
        return fieldUpdates[fieldName]?.first()
            ?: constraintError(sourceStruct, "Required struct field '$fieldName' missing")
    }

    public fun build(): StructElement {
        return StructElementImpl(
            fieldUpdates.flatMap { (fieldName, values) -> values.map { field(fieldName, it) } }.toPersistentList(),
            sourceStruct.annotations.toPersistentList(),
            sourceStruct.metas.toPersistentMap()
        )
    }
}

public fun IonElement.update(
    defaultUpdateMode: StructUpdateBuilder.FieldUpdateMode = REPLACE,
    body: StructUpdateBuilder.() -> Unit
): StructElement {
    val builder = StructUpdateBuilder(this.asAnyElement().asStruct(), defaultUpdateMode)
    body(builder)
    return builder.build()
}
