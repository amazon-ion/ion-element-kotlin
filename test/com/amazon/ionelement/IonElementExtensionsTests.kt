package com.amazon.ionelement

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.*
import com.amazon.ionelement.util.ArgumentsProviderBase
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class IonElementExtensionsTests : ArgumentsProviderBase() {

    override fun getParameters(): List<IonElement> = listOf(
        ionInt(1),
        ionInt(BigInteger("${Long.MAX_VALUE}000")),
        ionFloat(1.0),
        ionDecimal(Decimal.valueOf("1.0")),
        ionString("hello"),
        ionSymbol("world"),
        ionBool(true),
        ionNull(ElementType.BOOL),
        ionTimestamp("2022-01-01"),
        emptyIonList(),
        emptyIonSexp(),
        emptyIonStruct(),
        emptyClob(),
        emptyBlob()
    )

    @ParameterizedTest
    @ArgumentsSource(IonElementExtensionsTests::class)
    fun withAnnotationsVariadic(element: IonElement) {
        val oneAnno = element.withAnnotations("foo")
        assertSame(oneAnno, oneAnno.withAnnotations())
        assertEquals(1, oneAnno.annotations.size)
        assertTrue(oneAnno.annotations.contains("foo"))

        val twoAnno1 = element.withAnnotations("foo", "bar")
        assertSame(twoAnno1, twoAnno1.withAnnotations())
        assertEquals(2, twoAnno1.annotations.size)
        assertTrue(twoAnno1.annotations.contains("foo"))
        assertTrue(twoAnno1.annotations.contains("bar"))

        val twoAnno2 = element.withAnnotations("foo").withAnnotations("bar")
        assertSame(twoAnno2, twoAnno2.withAnnotations())
        assertEquals(2, twoAnno2.annotations.size)
        assertTrue(twoAnno2.annotations.contains("foo"))
        assertTrue(twoAnno2.annotations.contains("bar"))
    }

    @ParameterizedTest
    @ArgumentsSource(IonElementExtensionsTests::class)
    fun withAnnotationsList(element: IonElement) {
        val twoAnno1 = element.withAnnotations(listOf("foo", "bar"))
        assertSame(twoAnno1, twoAnno1.withAnnotations())
        assertEquals(2, twoAnno1.annotations.size)
        assertTrue(twoAnno1.annotations.contains("foo"))
        assertTrue(twoAnno1.annotations.contains("bar"))

        val twoAnno2 = element.withAnnotations(listOf("foo")).withAnnotations(listOf("bar"))
        assertSame(twoAnno2, twoAnno2.withAnnotations())
        assertEquals(2, twoAnno2.annotations.size)
        assertTrue(twoAnno2.annotations.contains("foo"))
        assertTrue(twoAnno2.annotations.contains("bar"))

        val noAnnos = twoAnno1.withoutAnnotations()
        assertEquals(0, noAnnos.annotations.size)
    }

    @ParameterizedTest
    @ArgumentsSource(IonElementExtensionsTests::class)
    fun withoutAnnotations(element: IonElement) {
        val hasAnnos = element.withAnnotations("foo", "bar").withMeta("foo", 42)
        assertEquals(2, hasAnnos.annotations.size)

        val noAnnos = hasAnnos.withoutAnnotations()
        assertEquals(0, noAnnos.annotations.size)
        assertSame(noAnnos, noAnnos.withAnnotations())
        // also check that metas haven't been lost.
        assertEquals(42, noAnnos.metas["foo"])
    }

    @ParameterizedTest
    @ArgumentsSource(IonElementExtensionsTests::class)
    fun metas(element: IonElement) {
        val oneMeta = element.withMeta("foo", 42)
        assertEquals(1, oneMeta.metas.size)
        assertEquals(42, oneMeta.metas["foo"])

        val twoMeta1 = element.withMeta("foo", 42).withMeta("bar", 43)
        assertEquals(42, twoMeta1.metas["foo"])
        assertEquals(43, twoMeta1.metas["bar"])

        val twoMeta2 = element.withMetas(metaContainerOf("foo" to 52, "bar" to 53))
        assertEquals(52, twoMeta2.metas["foo"])
        assertEquals(53, twoMeta2.metas["bar"])

        // overwrites "bar" in twoMeta2
        val twoMeta3 = twoMeta2.withMeta("bar", 64)
        assertEquals(52, twoMeta3.metas["foo"])
        assertEquals(64, twoMeta3.metas["bar"])

        // Also overwrites "bar" in twoMeta2
        val twoMeta4 = twoMeta2.withMetas(metaContainerOf("bar" to 73))
        assertEquals(52, twoMeta4.metas["foo"])
        assertEquals(73, twoMeta4.metas["bar"])

        val noMetas = twoMeta4.withoutMetas()
        assertEquals(0, noMetas.annotations.size)
    }

    @ParameterizedTest
    @ArgumentsSource(IonElementExtensionsTests::class)
    fun withoutMetas(element: IonElement) {
        val hasMetas = element.withAnnotations("foo").withMeta("foo", 42)
        assertEquals(1, hasMetas.annotations.size)
        assertTrue(hasMetas.annotations.contains("foo"))

        val noMetas = hasMetas.withoutMetas()
        assertEquals(1, noMetas.annotations.size)
        assertSame(noMetas, noMetas.withAnnotations())

        // also check that annotations haven't been lost
        assertEquals(1, noMetas.annotations.size)
        assertTrue(noMetas.annotations.contains("foo"))
    }

    @Test
    fun head() {
        val sexp = loadSingleElement("(1 2 3)").asSexp()
        assertEquals(ionInt(1).asAnyElement(), sexp.head)
    }

    @Test
    fun tail() {
        val sexp = loadSingleElement("(1 2 3)").asSexp()

        assertEquals(listOf<IonElement>(ionInt(2), ionInt(3)), sexp.tail)
        assertEquals(listOf<IonElement>(ionInt(3)), sexp.tail.tail)
        assertEquals(listOf(), sexp.tail.tail.tail)
        assertThrows<IllegalArgumentException> { sexp.tail.tail.tail.tail }
    }
}
