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

import com.amazon.ionelement.api.IonElectrolyteException
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.createIonElementLoader
import com.amazon.ionelement.api.ionBlob
import com.amazon.ionelement.api.ionClob
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionListOf
import com.amazon.ionelement.api.ionSexpOf
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ion.Decimal
import com.amazon.ion.IonType
import com.amazon.ion.IonType.BLOB
import com.amazon.ion.IonType.BOOL
import com.amazon.ion.IonType.CLOB
import com.amazon.ion.IonType.DECIMAL
import com.amazon.ion.IonType.TIMESTAMP
import com.amazon.ion.IonType.FLOAT
import com.amazon.ion.IonType.INT
import com.amazon.ion.IonType.LIST
import com.amazon.ion.IonType.NULL
import com.amazon.ion.IonType.SEXP
import com.amazon.ion.IonType.STRING
import com.amazon.ion.IonType.STRUCT
import com.amazon.ion.IonType.SYMBOL
import com.amazon.ion.IonType.DATAGRAM
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ValueAccessorTests {
    @ParameterizedTest
    @MethodSource("parametersForValueAccessorsTest")
    fun valueAccessorTests(tc: TestCase) {

        val element = createIonElementLoader().loadSingleElement(tc.ionText)
        assertElementProperties(element, tc.ionType, tc.expectedValue)
    }

    companion object {
        data class TestCase(val ionText: String, val ionType: IonType, val expectedValue: Any?)

        @JvmStatic
        @Suppress("unused")
        fun parametersForValueAccessorsTest() = listOf(
            // Kotlin/JVM's interpretation of equality is acceptable for nulls and scalars.
            // This means we should pass JVM data types for the expected values here.

            // Nulls first
            TestCase("null", NULL, null),
            TestCase("null.null", NULL, null),
            TestCase("null.bool", BOOL, null),
            TestCase("null.int", INT, null),
            TestCase("null.float", FLOAT, null),
            TestCase("null.decimal", DECIMAL, null),
            TestCase("null.string", STRING, null),
            TestCase("null.symbol", SYMBOL, null),
            TestCase("null.clob", CLOB, null),
            TestCase("null.blob", BLOB, null),
            TestCase("null.list", LIST, null),
            TestCase("null.sexp", SEXP, null),
            TestCase("null.struct", STRUCT, null),

            // Scalar values next
            TestCase("true", BOOL, true),
            TestCase("false", BOOL, false),
            TestCase("42", INT, 42L),
            TestCase("1234e-3", FLOAT, 1.234),
            TestCase("1.234", DECIMAL, Decimal.valueOf(1234, 3)),
            TestCase("\"a string\"", STRING, "a string"),
            TestCase("a_symbol", SYMBOL, "a_symbol"),
            // We have to rely on IonElement's definition of equality for the Ion types below.
            TestCase("{{ YSBibG9i }}", BLOB, ionBlob(Charsets.UTF_8.encode("a blob").array())),
            TestCase("{{ \"a clob\" }}", CLOB, ionClob(Charsets.UTF_8.encode("a clob").array())),

            // Containers
            TestCase("[1]", LIST, ionListOf(ionInt(1))),
            TestCase("(2)", SEXP, ionSexpOf(ionInt(2))),
            TestCase("{ foo: 42 }", STRUCT, ionStructOf("foo" to ionInt(42))))


        private fun assertElementProperties(element: IonElement, ionType: IonType, expectedValue: Any?) {
            assertEquals(ionType, element.type)

            assertEquals(expectedValue == null, element.isNull)
            assertThrowsForWrongAccessorTypes(element)

            if(expectedValue != null) {
                assertValidValueAccessors(element, expectedValue)
            } else {
                assertNonNullPropertiesThrowWhenNull(element)
                assertOrNullValueAccessorsReturnNullWhenNull(element)
            }
        }

        /** Asserts the null and non-null value accessor properties return the expected value. */
        private fun assertValidValueAccessors(element: IonElement, expectedValue: Any?) {
            val values = with(element) {
                when (type) {
                    NULL -> {
                        assertEquals(0,
                            listOfNotNull(
                                booleanValueOrNull,
                                longValueOrNull,
                                doubleValueOrNull,
                                decimalValueOrNull,
                                timestampValueOrNull,
                                textValueOrNull,
                                stringValueOrNull,
                                symbolValueOrNull,
                                containerValueOrNull,
                                listValueOrNull,
                                sexpValueOrNull,
                                structValueOrNull),
                            "All *OrNull value accessors should return null for IonType.NULL")
                        return
                    }
                    BOOL -> listOf(booleanValue, booleanValueOrNull)
                    INT -> listOf(longValue, longValueOrNull)
                    FLOAT -> listOf(doubleValue, doubleValueOrNull)
                    DECIMAL -> listOf(decimalValue, decimalValueOrNull)
                    TIMESTAMP -> listOf(timestampValue, timestampValueOrNull)
                    STRING -> listOf(textValue, textValueOrNull, stringValue, stringValueOrNull)
                    SYMBOL -> listOf(textValue, textValueOrNull, symbolValue, symbolValueOrNull)
                    CLOB -> listOf(bytesValue, bytesValueOrNull, clobValue, clobValueOrNull)
                    BLOB -> listOf(bytesValue, bytesValueOrNull, blobValue, blobValueOrNull)
                    LIST -> listOf(containerValue, containerValueOrNull, listValue, listValueOrNull)
                    SEXP -> listOf(containerValue, containerValueOrNull, sexpValue, sexpValueOrNull)
                    STRUCT -> listOf(structValue, structValueOrNull)
                    DATAGRAM -> error("DATAGRAM not supported")
                }
            }

            values.forEachIndexed { i, value ->
                assertEquals(expectedValue, value, "Value at index $i must match the expected value")
            }
        }

        /**
         * Asserts that the return values of all of the `*OrNull` properties are null as appropriate for the [IonType]
         * of the specified [IonElement].
         */
        private fun assertOrNullValueAccessorsReturnNullWhenNull(element: IonElement) {
            assertTrue(element.isNull)
            return with(element) {
                when(type) {
                    NULL -> {
                        //For untyped nulls, all accessors should return null
                        assertNull(booleanValueOrNull)
                        assertNull(longValueOrNull)
                        assertNull(bigIntegerValueOrNull)
                        assertNull(decimalValueOrNull)
                        assertNull(textValueOrNull)
                        assertNull(stringValueOrNull)
                        assertNull(symbolValueOrNull)
                        assertNull(timestampValueOrNull)
                        assertNull(containerValueOrNull)
                        assertNull(listValueOrNull)
                        assertNull(listValueOrNull)
                        assertNull(bytesValueOrNull)
                        assertNull(clobValueOrNull)
                        assertNull(blobValueOrNull)
                    }
                    // For typed nulls, only the corresponding *OrNull accessor should return null
                    // (the other accessors should throw, but this is tested in [assertThrowsForWrongType])
                    BOOL -> assertNull(booleanValueOrNull)
                    INT -> assertNull(longValueOrNull).also { assertNull(bigIntegerValueOrNull) }
                    FLOAT -> assertNull(doubleValueOrNull)
                    DECIMAL -> assertNull(decimalValueOrNull)
                    TIMESTAMP -> assertNull(timestampValueOrNull)
                    SYMBOL -> assertNull(symbolValueOrNull).also { assertNull(textValueOrNull) }
                    STRING -> assertNull(stringValueOrNull).also { assertNull(textValueOrNull) }
                    CLOB -> assertNull(clobValueOrNull).also { assertNull(bytesValueOrNull) }
                    BLOB -> assertNull(blobValueOrNull).also { assertNull(bytesValueOrNull) }
                    LIST -> assertNull(listValueOrNull).also { assertNull(containerValueOrNull) }
                    SEXP -> assertNull(sexpValueOrNull).also { assertNull(containerValueOrNull) }
                    STRUCT -> assertNull(structValueOrNull)
                    DATAGRAM -> error("IonType.DATAGRAM is unsupported")
                }
            }
        }


        /**
         * Asserts that the return values of all of the non-null value accessor properties throw correctly when
         * the element is null.
         */
        private fun assertNonNullPropertiesThrowWhenNull(element: IonElement) {
            assertTrue(element.isNull)
            return with(element) {
                when(type) {
                    NULL -> {
                        //For untyped nulls, all non-null accessors should throw
                        assertThrows<IonElectrolyteException> { booleanValue }
                        assertThrows<IonElectrolyteException> { longValue }
                        assertThrows<IonElectrolyteException> { bigIntegerValue }
                        assertThrows<IonElectrolyteException> { decimalValue }
                        assertThrows<IonElectrolyteException> { textValue }
                        assertThrows<IonElectrolyteException> { stringValue }
                        assertThrows<IonElectrolyteException> { symbolValue }
                        assertThrows<IonElectrolyteException> { timestampValue }
                        assertThrows<IonElectrolyteException> { containerValue }
                        assertThrows<IonElectrolyteException> { listValue }
                        assertThrows<IonElectrolyteException> { listValue }
                        assertThrows<IonElectrolyteException> { bytesValue }
                        assertThrows<IonElectrolyteException> { clobValue }
                        assertThrows<IonElectrolyteException> { blobValue }
                    }
                    // For typed nulls, only the corresponding * accessor should return null
                    // (the other accessors should throw, but this is tested in [assertThrowsForWrongType]
                    BOOL -> assertThrows<IonElectrolyteException> { booleanValue }
                    INT -> assertThrows<IonElectrolyteException> { longValue }.also { assertThrows<IonElectrolyteException> { bigIntegerValue } }
                    FLOAT -> assertThrows<IonElectrolyteException> { doubleValue }
                    DECIMAL -> assertThrows<IonElectrolyteException> { decimalValue }
                    TIMESTAMP -> assertThrows<IonElectrolyteException> { timestampValue }
                    SYMBOL -> assertThrows<IonElectrolyteException> { symbolValue }.also { assertThrows<IonElectrolyteException> { textValue } }
                    STRING -> assertThrows<IonElectrolyteException> { stringValue }.also { assertThrows<IonElectrolyteException> { textValue } }
                    CLOB -> assertThrows<IonElectrolyteException> { clobValue }.also { assertThrows<IonElectrolyteException> { bytesValue } }
                    BLOB -> assertThrows<IonElectrolyteException> { blobValue }.also { assertThrows<IonElectrolyteException> { bytesValue } }
                    LIST -> assertThrows<IonElectrolyteException> { listValue }.also { assertThrows<IonElectrolyteException> { containerValue } }
                    SEXP -> assertThrows<IonElectrolyteException> { sexpValue }.also { assertThrows<IonElectrolyteException> { containerValue } }
                    STRUCT -> assertThrows<IonElectrolyteException> { structValue }
                    DATAGRAM -> error("IonType.DATAGRAM is unsupported")
                }
            }
        }

        /**
         * Asserts that accessing a value accessor for the wrong type of the given [element] throws correctly.
         *
         * Note that in the [IonElement] implementation the type check occurs before the null check so this
         * tests both the non-null and null value accessors.
         */
        private fun assertThrowsForWrongAccessorTypes(element: IonElement) {
            with(element) {
                if(type == NULL) {
                    // No checks needed in this case because:
                    // - Non-null accessors will throw for due to the value being unexpectedly null.
                    // (That is checked in [assertNonNullPropertiesThrowWhenNull]).
                    // - Null accessors will never throw
                    return
                }
                if (type != BOOL) {
                    assertThrows<IonElectrolyteException>("booleanValue") { booleanValue }
                    assertThrows<IonElectrolyteException>("booleanValueOrNull") { booleanValueOrNull }
                }
                if (type != INT) {
                    assertThrows<IonElectrolyteException>("longValue") { longValue }
                    assertThrows<IonElectrolyteException>("longValueOrNull") { longValueOrNull }
                }
                if (type != FLOAT) {
                    assertThrows<IonElectrolyteException>("doubleValue") { doubleValue }
                     assertThrows<IonElectrolyteException>("doubleValueOrNull") { doubleValueOrNull }
                }
                if (type != DECIMAL) {
                    assertThrows<IonElectrolyteException>("decimalValue") { decimalValue }
                    assertThrows<IonElectrolyteException>("decimalValueOrNull") { decimalValueOrNull }
                }
                if (type != TIMESTAMP) {
                    assertThrows<IonElectrolyteException>("timestampValue") { timestampValue }
                    assertThrows<IonElectrolyteException>("timestampValueOrNull") { timestampValueOrNull }
                }
                if (type != SYMBOL) {
                    assertThrows<IonElectrolyteException>("symbolValue") { symbolValue }
                    assertThrows<IonElectrolyteException>("symbolValueOrNull") { symbolValueOrNull }
                }
                if (type != STRING) {
                    assertThrows<IonElectrolyteException>("stringValue") { stringValue }
                    assertThrows<IonElectrolyteException>("stringValueOrNull") { stringValueOrNull }
                }
                if (type != CLOB) {
                    assertThrows<IonElectrolyteException>("clobValue") { clobValue }
                    assertThrows<IonElectrolyteException>("clobValueOrNull") { clobValueOrNull }
                }
                if (type != BLOB) {
                    assertThrows<IonElectrolyteException>("blobValue") { blobValue }
                    assertThrows<IonElectrolyteException>("blobValueOrNull") { blobValueOrNull }
                }
                if (type != LIST) {
                    assertThrows<IonElectrolyteException>("listValue") { listValue }
                    assertThrows<IonElectrolyteException>("listValueOrNull") { listValueOrNull }
                }
                if (type != SEXP) {
                    assertThrows<IonElectrolyteException>("sexpValue") { sexpValue }
                    assertThrows<IonElectrolyteException>("sexpValueOrNull") { sexpValueOrNull }
                }
                if (type != STRUCT) {
                    assertThrows<IonElectrolyteException>("structValue") { structValue }
                    assertThrows<IonElectrolyteException>("structValueOrNull") { structValueOrNull }
                }
                if (type == DATAGRAM) {
                    error("IonElement does not support IonType.DATAGRAM")
                }
            }
        }

    }
}
