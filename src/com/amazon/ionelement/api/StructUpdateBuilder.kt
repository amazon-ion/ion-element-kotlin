package com.amazon.ionelement.api

import com.amazon.ionelement.api.StructUpdateBuilder.FieldUpdateMode.*
import com.amazon.ionelement.impl.*
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

/**
 * An [IonPath] represents a traversal into a nested IonElement made of [StructElement]s and [SeqElement]s
 */
public class IonPath(public val elements: List<IonPathElement>) : Collection<IonPathElement> by elements {
    public constructor(vararg elements: IonPathElement) : this(elements.asList())

    public fun tail(): IonPath {
        return IonPath(elements.drop(1))
    }
}

public sealed class IonPathElement

/**
 * Traverse into a nested [StructElement] by its field name.
 */
public data class Field(val fieldName: String) : IonPathElement()

/**
 * Traverse into a nested [SeqElement] by its numerical index.
 */
public data class Index(val index: Int) : IonPathElement()

/**
 * Fetch an element from a nested IonElement using the [path] to traverse struct fields and [SeqElement] indices
 */
public tailrec fun IonElement.getPath(path: IonPath): IonElement {
    return when (path.isEmpty()) {
        true -> this
        else -> when (val element = path.first()) {
            is Field -> this.asAnyElement().asStruct()[element.fieldName].getPath(path.tail())
            is Index -> this.asAnyElement().asSeq().values[element.index].getPath(path.tail())
        }
    }
}

/**
 * Implement [] operator for fetching a nested element
 */
public operator fun IonElement.get(path: IonPath): IonElement {
    return this.getPath(path)
}

/**
 * A [StructUpdateBuilder] builder implements a typesafe builder that allows updating a [StructElement] by adding
 * and replacing existing [StructField]s similar to the Kotlin builtin `copy` function for data classes.
 */
public class StructUpdateBuilder(
    private val sourceStruct: StructElement,
    private val defaultMode: FieldUpdateMode
) {
    private val fieldUpdates: MutableMap<String, MutableList<AnyElement>> =
        sourceStruct.fields.groupBy({ it.name }, { it.value })
            .mapValues { it.value.toMutableList() }
            .toMutableMap()

    // Determines whether new fields are added to the struct or replace existing fields of the same name
    public enum class FieldUpdateMode {
        REPLACE,
        ADD
    }

    // Sets a fields using the default [FieldUpdateMode]
    public operator fun set(fieldName: String, value: IonElement) {
        set(fieldName, value, defaultMode == REPLACE)
    }

    // Sets a fields using the provided [mode] as [FieldUpdateMode]
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

    // Gets a field, including fields that may have been added by current builder
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

/**
 * Instantiates a [StructUpdateBuilder] on the receiver [StructElement].
 *
 * @see [com.amazon.ionelement.demos.kotlin.StructUpdateDemo]
 */
public fun IonElement.update(
    defaultUpdateMode: StructUpdateBuilder.FieldUpdateMode = REPLACE,
    body: StructUpdateBuilder.() -> Unit
): StructElement {
    val builder = StructUpdateBuilder(this.asAnyElement().asStruct(), defaultUpdateMode)
    body(builder)
    return builder.build()
}
