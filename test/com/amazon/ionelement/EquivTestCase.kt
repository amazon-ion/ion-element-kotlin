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

package com.amazon.ionelement

import com.amazon.ionelement.api.*
import com.amazon.ionelement.impl.*
import org.junit.jupiter.api.Assertions

data class EquivTestCase(val left: String, val right: String, val isEquiv: Boolean) {
    fun checkEquivalence() {
        val loader = createIonElementLoader()

        // Note: metas are not relevant to equivalence tests.
        val leftElement = loader.loadSingleElement(left).withMeta("leftMeta", 1)
        val rightElement = loader.loadSingleElement(right).withMeta("rightMeta", 2)

        // It seems unlikely that we should ever return zero--if we do it probably is a bug.
        // (this may need to be revisited in the future)
        Assertions.assertNotEquals(0, leftElement.hashCode())
        Assertions.assertNotEquals(0, rightElement.hashCode())

        // Check hashCode against normative implementation
        Assertions.assertEquals(hashElement(leftElement), leftElement.hashCode(), "hashCode() does not match normative implementation")
        Assertions.assertEquals(hashElement(rightElement), rightElement.hashCode(), "hashCode() does not match normative implementation")

        // Equivalence should be reflexive
        checkEquivalence(true, leftElement, leftElement)
        checkEquivalence(true, rightElement, rightElement)

        // Equivalence should be symmetric
        checkEquivalence(isEquiv, leftElement, rightElement)

        // Check reflexivity again, this time bypassing the reference equality check that happens first .equals calls
        checkEquivalence(true, leftElement, loader.loadSingleElement(left))
        checkEquivalence(true, rightElement, loader.loadSingleElement(right))

        // Adding annotations should not change the result
        val leftElementWithAnnotation = leftElement.withAnnotations("some_annotation")
        val rightElementWithAnnotation = rightElement.withAnnotations("some_annotation")
        checkEquivalence(
            isEquiv,
            leftElementWithAnnotation,
            rightElementWithAnnotation
        )

        // Adding an annotation to only one side will force them to be not equivalent
        checkEquivalence(false, leftElement.withAnnotations("some_annotation"), rightElement)

        // Adding metas has no effect
        checkEquivalence(isEquiv, leftElement.withMeta("foo", 1), rightElement)

        // Nesting the values within a struct should not change the result
        fun nest(ie: AnyElement) = ionStructOf("nested" to ie)
        checkEquivalence(isEquiv, nest(leftElement), nest(rightElement))

        // Try using a proxy to make sure that equivalence is not tied to a particular implementation.
        val leftProxy = leftElement.createProxy()
        val rightProxy = rightElement.createProxy()
        Assertions.assertEquals(leftElement, leftProxy)
        Assertions.assertEquals(rightProxy, rightElement)

        // Equivalence should be transitive
        checkEquivalence(isEquiv, leftElement, rightProxy)
        checkEquivalence(isEquiv, leftProxy, rightElement)
        checkEquivalence(isEquiv, leftProxy, rightProxy)
    }

    private fun checkEquivalence(equiv: Boolean, first: IonElement, second: IonElement) {
        if (equiv) {
            Assertions.assertTrue(areElementsEqual(first, second), "Elements should be equivalent.")
            Assertions.assertEquals(first, second, "equals() implementation does not match normative equivalence implementation")
            Assertions.assertEquals(first.hashCode(), second.hashCode(), "Elements' hash codes should be equal")
        } else {
            Assertions.assertFalse(areElementsEqual(first, second), "Elements should not be equivalent.")
            Assertions.assertNotEquals(first, second, "equals() implementation does not match normative equivalence implementation")
            // Note that two different [IonElement]s *can* have the same hash code and this might one day
            // break the build and may necessitate removing the assertion below. However, if it does happen we
            // should evaluate if the hashing algorithm is sufficient or not since that seems unlikely
            Assertions.assertNotEquals(first.hashCode(), second.hashCode(), "Elements' hash codes should not be equal")
        }
    }

    private fun IonElement.createProxy(): AnyElement {
        return if (isNull) {
            NullElementProxy(this.asAnyElement())
        } else when (this) {
            is BoolElement -> BoolElementProxy(this)
            is IntElement -> IntElementProxy(this)
            is FloatElement -> FloatElementProxy(this)
            is DecimalElement -> DecimalElementProxy(this)
            is TimestampElement -> TimestampElementProxy(this)
            is StringElement -> StringElementProxy(this)
            is SymbolElement -> SymbolElementProxy(this)
            is BlobElement -> BlobElementProxy(this)
            is ClobElement -> ClobElementProxy(this)
            is ListElement -> ListElementProxy(this)
            is SexpElement -> SexpElementProxy(this)
            is StructElement -> StructElementProxy(this)
            else -> TODO("Unreachable")
        }
    }

    /**
     * This is an alternate implementation of the whole `IonElement` hierarchy so that we can test to make sure that the
     * implementations of equals do not have a hard dependency on any implementation details that could be accessed by
     * casting to a specific implementation. It's okay to use optimizations if "other" is a specific implementation, but
     * the result must not change just because "other" is a different implementation of the same interfaces.
     *
     * Why don't we use mocks for this? It's because AnyElement and e.g. BoolElement have conflicting method signatures
     * for e.g. [copy] ("Return types of inherited members are incompatible").
     */
    private abstract class AnyElementProxy<T : AnyElementProxy<T>>(open val delegate: IonElement) : AnyElement by delegate as AnyElement

    private class NullElementProxy(override val delegate: AnyElement) : AnyElementProxy<NullElementProxy>(delegate) {
        override fun copy(annotations: List<String>, metas: MetaContainer): NullElementProxy = NullElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): NullElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): NullElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): NullElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): NullElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): NullElementProxy = _withMeta(key, value)
        override fun withoutMetas(): NullElementProxy = _withoutMetas()
    }

    private class BoolElementProxy(override val delegate: BoolElement) : AnyElementProxy<BoolElementProxy>(delegate), BoolElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): BoolElementProxy = BoolElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): BoolElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): BoolElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): BoolElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): BoolElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): BoolElementProxy = _withMeta(key, value)
        override fun withoutMetas(): BoolElementProxy = _withoutMetas()
    }

    private class IntElementProxy(override val delegate: IntElement) : AnyElementProxy<IntElementProxy>(delegate), IntElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): IntElementProxy = IntElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): IntElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): IntElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): IntElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): IntElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): IntElementProxy = _withMeta(key, value)
        override fun withoutMetas(): IntElementProxy = _withoutMetas()
    }

    private class FloatElementProxy(override val delegate: FloatElement) : AnyElementProxy<FloatElementProxy>(delegate), FloatElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): FloatElementProxy = FloatElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): FloatElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): FloatElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): FloatElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): FloatElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): FloatElementProxy = _withMeta(key, value)
        override fun withoutMetas(): FloatElementProxy = _withoutMetas()
    }

    private class DecimalElementProxy(override val delegate: DecimalElement) : AnyElementProxy<DecimalElementProxy>(delegate), DecimalElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): DecimalElementProxy = DecimalElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): DecimalElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): DecimalElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): DecimalElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): DecimalElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): DecimalElementProxy = _withMeta(key, value)
        override fun withoutMetas(): DecimalElementProxy = _withoutMetas()
    }

    private class TimestampElementProxy(override val delegate: TimestampElement) : AnyElementProxy<TimestampElementProxy>(delegate), TimestampElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): TimestampElementProxy = TimestampElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): TimestampElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): TimestampElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): TimestampElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): TimestampElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): TimestampElementProxy = _withMeta(key, value)
        override fun withoutMetas(): TimestampElementProxy = _withoutMetas()
    }

    private class SymbolElementProxy(override val delegate: SymbolElement) : AnyElementProxy<SymbolElementProxy>(delegate), SymbolElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): SymbolElementProxy = SymbolElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): SymbolElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): SymbolElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): SymbolElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): SymbolElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): SymbolElementProxy = _withMeta(key, value)
        override fun withoutMetas(): SymbolElementProxy = _withoutMetas()
    }

    private class StringElementProxy(override val delegate: StringElement) : AnyElementProxy<StringElementProxy>(delegate), StringElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): StringElementProxy = StringElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): StringElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): StringElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): StringElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): StringElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): StringElementProxy = _withMeta(key, value)
        override fun withoutMetas(): StringElementProxy = _withoutMetas()
    }

    private class BlobElementProxy(override val delegate: BlobElement) : AnyElementProxy<BlobElementProxy>(delegate), BlobElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): BlobElementProxy = BlobElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): BlobElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): BlobElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): BlobElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): BlobElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): BlobElementProxy = _withMeta(key, value)
        override fun withoutMetas(): BlobElementProxy = _withoutMetas()
    }

    private class ClobElementProxy(override val delegate: ClobElement) : AnyElementProxy<ClobElementProxy>(delegate), ClobElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): ClobElementProxy = ClobElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): ClobElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): ClobElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): ClobElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): ClobElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): ClobElementProxy = _withMeta(key, value)
        override fun withoutMetas(): ClobElementProxy = _withoutMetas()
    }

    private class ListElementProxy(override val delegate: ListElement) : AnyElementProxy<ListElementProxy>(delegate), ListElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): ListElementProxy = ListElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): ListElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): ListElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): ListElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): ListElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): ListElementProxy = _withMeta(key, value)
        override fun withoutMetas(): ListElementProxy = _withoutMetas()
    }

    private class SexpElementProxy(override val delegate: SexpElement) : AnyElementProxy<SexpElementProxy>(delegate), SexpElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): SexpElementProxy = SexpElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): SexpElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): SexpElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): SexpElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): SexpElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): SexpElementProxy = _withMeta(key, value)
        override fun withoutMetas(): SexpElementProxy = _withoutMetas()
    }

    private class StructElementProxy(override val delegate: StructElement) : AnyElementProxy<StructElementProxy>(delegate), StructElement by delegate {
        override fun copy(annotations: List<String>, metas: MetaContainer): StructElementProxy = StructElementProxy(delegate.copy(annotations, metas))
        override fun withAnnotations(vararg additionalAnnotations: String): StructElementProxy = _withAnnotations(*additionalAnnotations)
        override fun withAnnotations(additionalAnnotations: Iterable<String>): StructElementProxy = _withAnnotations(additionalAnnotations)
        override fun withoutAnnotations(): StructElementProxy = _withoutAnnotations()
        override fun withMetas(additionalMetas: MetaContainer): StructElementProxy = _withMetas(additionalMetas)
        override fun withMeta(key: String, value: Any): StructElementProxy = _withMeta(key, value)
        override fun withoutMetas(): StructElementProxy = _withoutMetas()
    }
}
