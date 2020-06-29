package com.amazon.ionelement

import com.amazon.ion.Decimal
import com.amazon.ion.Timestamp
import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.SeqElement
import com.amazon.ionelement.api.StructElement
import com.amazon.ionelement.api.field
import com.amazon.ionelement.api.ionBlob
import com.amazon.ionelement.api.ionBool
import com.amazon.ionelement.api.ionClob
import com.amazon.ionelement.api.ionDecimal
import com.amazon.ionelement.api.ionFloat
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionListOf
import com.amazon.ionelement.api.ionNull
import com.amazon.ionelement.api.ionSexpOf
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionelement.api.ionTimestamp
import com.amazon.ionelement.api.metaContainerOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorTests {

    companion object {
        val dummyMetas = metaContainerOf("foo_meta" to 1)
        val dummyAnnotations = listOf("some_annotation")

        @JvmStatic
        @Suppress("unused")
        fun parametersForMetasAndAnnotationsTest() = listOf(
            ionNull(ElementType.NULL, dummyAnnotations, dummyMetas),
            ionBool(true, dummyAnnotations, dummyMetas),
            ionInt(123L, dummyAnnotations, dummyMetas),
            ionInt(BigInteger.ONE, dummyAnnotations, dummyMetas),
            ionTimestamp("2001T", dummyAnnotations, dummyMetas),
            ionTimestamp(Timestamp.valueOf("2001T"), dummyAnnotations, dummyMetas),
            ionFloat(1.0, dummyAnnotations, dummyMetas),
            ionDecimal(Decimal.ZERO, dummyAnnotations, dummyMetas),
            ionString("foo", dummyAnnotations, dummyMetas),
            ionSymbol("foo", dummyAnnotations, dummyMetas),
            ionClob(ByteArray(1), dummyAnnotations, dummyMetas),
            ionBlob(ByteArray(1), dummyAnnotations, dummyMetas),
            ionListOf(ionInt(1), annotations =  dummyAnnotations, metas = dummyMetas),
            ionListOf(listOf(ionInt(1)), dummyAnnotations, dummyMetas),
            ionSexpOf(ionInt(1), annotations = dummyAnnotations, metas = dummyMetas),
            ionSexpOf(listOf(ionInt(1)), dummyAnnotations, dummyMetas),
            ionStructOf("foo" to ionInt(1), annotations = dummyAnnotations, metas = dummyMetas),
            ionStructOf(field("foo", ionInt(1)), annotations = dummyAnnotations, metas = dummyMetas),
            ionStructOf(listOf(field("foo", ionInt(1))), annotations = dummyAnnotations, metas = dummyMetas)
        )
    }

    @ParameterizedTest
    @MethodSource("parametersForMetasAndAnnotationsTest")
    fun metasAndAnnotationsTest(elem: IonElement) {
        assertEquals(1, elem.annotations.size)
        assertTrue(elem.annotations.contains("some_annotation"))

        assertEquals(1, elem.metas.size)
        assertEquals(1, elem.metas["foo_meta"])
    }
    
    @Test
    fun scalarConstructorsValueTest() {
        assertTrue(ionNull(ElementType.NULL).isNull)
        assertEquals(true, ionBool(true).booleanValue)
        assertEquals(false, ionBool(false).booleanValue)
        assertEquals(123L, ionInt(123L).longValue)
        assertEquals(BigInteger.ONE, ionInt(BigInteger.ONE).bigIntegerValue)
        assertEquals(Timestamp.valueOf("2001T"), ionTimestamp("2001T").timestampValue)
        assertEquals(Timestamp.valueOf("2001T"), ionTimestamp(Timestamp.valueOf("2001T")).timestampValue)
        assertEquals(12.0, ionFloat(12.0).doubleValue)
        assertEquals(Decimal.ZERO, ionDecimal(Decimal.ZERO).decimalValue)
        assertEquals("foo", ionString("foo").textValue)
        assertEquals("foo", ionSymbol("foo").textValue)
        assertContentEquals(createBytes(), ionClob(createBytes()).bytesValue.copyOfBytes())
        assertContentEquals(createBytes(), ionBlob(createBytes()).bytesValue.copyOfBytes())
    }

    private fun assertContentEquals(expected: ByteArray, actual: ByteArray) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEachIndexed { i, (l, r) ->
            assertEquals(l, r, "Byte at index $i must match")
        }
    }

    private fun createBytes() = ByteArray(3).also {
        it[0] = 1
        it[1] = 2
        it[2] = 3
    }

    @Test
    fun listConstructorsValueTest() {
        assertSeqContents(ionListOf(ionInt(12)))
        assertSeqContents(ionListOf(listOf(ionInt(12))))
    }

    @Test
    fun sexpConstructorsValueTest() {
        assertSeqContents(ionSexpOf(ionInt(12)))
        assertSeqContents(ionSexpOf(listOf(ionInt(12))))
    }

    private fun assertSeqContents(seq: SeqElement) {
        assertEquals(1, seq.values.size)
        assertEquals(12, seq.values[0].longValue)
    }

    @Test
    fun structConstructorsValueTest() {
        assertStructContents(ionStructOf("foo" to ionInt(12)))
        assertStructContents(ionStructOf(field("foo", ionInt(12))))
        assertStructContents(ionStructOf(listOf(field("foo", ionInt(12)))))
    }

    private fun assertStructContents(struct: StructElement) {
        assertEquals(1, struct.size)
        assertEquals(12L, struct["foo"].longValue)
    }

}