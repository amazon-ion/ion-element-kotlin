package com.amazon.ionelement.api

/** Returns a shallow copy of the current node with the specified additional annotations. */
fun Element.withAnnotations(vararg additionalAnnotations: String): IonElement =
    copy(annotations = this.annotations + additionalAnnotations)


/** Returns a shallow copy of the current node with the specified additional annotations. */
fun Element.withAnnotations(additionalAnnotations: Iterable<String>): IonElement =
    withAnnotations(*additionalAnnotations.toList().toTypedArray())

/** Returns a shallow copy of the current node with all annotations removed.. */
fun Element.withoutAnnotations(): IonElement =
    when {
        this.annotations.isNotEmpty() -> copy(annotations = emptyList())
        else -> this.asIonElement()
    }

/**
 * Returns a shallow copy of the current node with the specified additional metadata, overwriting any metas
 * that already exist with the same keys.
 */
fun Element.withMetas(additionalMetas: MetaContainer): IonElement =
    copy(metas = metaContainerOf(metas.toList().union(additionalMetas.toList()).toList()))

/**
 * Returns a shallow copy of the current node with the specified additional meta, overwriting any meta
 * that previously existed with the same key.
 *
 * When adding multiple metas, consider [withMetas] instead.
 */
fun Element.withMeta(key: String, value: Any): IonElement = withMetas(metaContainerOf(key to value))

/** Returns a shallow copy of the current node without any metadata. */
fun Element.withoutMetas(): IonElement =
    copy(metas = emptyMetaContainer(), annotations = annotations)


/**
 * Returns the string representation of the symbol in the first element of this container.
 *
 * If the first element is not a symbol or this container has no elements, throws [IonElectrolyteException],
 */
val SeqElement.tag get() = this.head.symbolValue

/**
 * Returns the first element of this container.
 *
 * If this container has no elements, throws [IonElectrolyteException].
 */
val SeqElement.head: IonElement
    get() =
        when (this.values.size) {
            0 -> ionError(this, "Cannot get head of empty container")
            else -> this.values.first()
        }

/**
 * Returns a sub-list containing all elements of this container except the first.
 *
 * If this container has no elements, throws [IonElectrolyteException].
 */
val SeqElement.tail: List<IonElement> get()  =
    when (this.values.size) {
        0 -> ionError(this, "Cannot get tail of empty container")
        else -> this.values.subList(1, this.values.size)
    }

