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

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.ElementType.BLOB
import com.amazon.ionelement.api.ElementType.BOOL
import com.amazon.ionelement.api.ElementType.CLOB
import com.amazon.ionelement.api.ElementType.DECIMAL
import com.amazon.ionelement.api.ElementType.FLOAT
import com.amazon.ionelement.api.ElementType.INT
import com.amazon.ionelement.api.ElementType.LIST
import com.amazon.ionelement.api.ElementType.NULL
import com.amazon.ionelement.api.ElementType.SEXP
import com.amazon.ionelement.api.ElementType.STRING
import com.amazon.ionelement.api.ElementType.STRUCT
import com.amazon.ionelement.api.ElementType.SYMBOL
import com.amazon.ionelement.api.ElementType.TIMESTAMP
import com.amazon.ionelement.api.IonElementException
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.loadSingleElement
import com.amazon.ionelement.impl.ByteArrayViewImpl
import java.math.BigInteger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class AnyElementTests {
    @ParameterizedTest
    @MethodSource("parametersForValueAccessorsTest")
    fun valueAccessorTests(tc: TestCase) {
        val element = loadSingleElement(tc.ionText)
        assertElementProperties(element, tc.elementType, tc.expectedValue)
    }

    @Test
    fun structFieldsTest() {
        /** Test what we were unable to in [assertAccessors] */
        val s = loadSingleElement("{ foo: 1, bar: 2 }").structFields
        assertEquals(2, s.count())
        s.single { it.name == "foo" && it.value.longValue == 1L }
        s.single { it.name == "bar" && it.value.longValue == 2L }
    }

    @Test
    fun bigIntegerValueFromLongIntElement() {
        val longValues = listOf(1, 0, -1, Long.MIN_VALUE, Long.MAX_VALUE)
        longValues.map {
            val bigInt = loadSingleElement(it.toString())
            val bigIntValue = bigInt.bigIntegerValue
            assertEquals(BigInteger.valueOf(it), bigIntValue)
        }
    }

    companion object {
        data class TestCase(val ionText: String, val elementType: ElementType, val expectedValue: Any?)

        @JvmStatic
        @Suppress("unused")
        fun parametersForValueAccessorsTest() = listOf(
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

            TestCase("{{ YSBibG9i }}", BLOB, ByteArrayViewImpl(Charsets.UTF_8.encode("a blob").array())),
            TestCase("{{ \"a clob\" }}", CLOB, ByteArrayViewImpl(Charsets.UTF_8.encode("a clob").array())),

            // Containers
            TestCase("[1]", LIST, listOf(ionInt(1))),
            TestCase("(2)", SEXP, listOf(ionInt(2))),
            // Note, this test case only checks `.containerValues`
            TestCase("{ foo: 42 }", STRUCT, listOf(ionInt(42)))
        )

        private fun assertElementProperties(element: AnyElement, elementType: ElementType, expectedValue: Any?) {
            assertEquals(elementType, element.type)

            assertEquals(expectedValue == null, element.isNull)
            assertThrowsForWrongAccessorTypes(element)

            assertAccessors(element, expectedValue)
        }

        private fun checkThrows(block: () -> Unit) {
            assertThrows<IonElementException> { block() }
        }

        /** Asserts the null and non-null value accessor properties return the expected values. */
        private fun assertAccessors(element: AnyElement, expectedValue: Any?) {
            fun checkEquals(v: Any?) = assertEquals(expectedValue, v)
            with(element) {
                when (type) {
                    NULL -> {
                        assertNull(booleanValueOrNull)
                        assertNull(longValueOrNull)
                        assertNull(doubleValueOrNull)
                        assertNull(decimalValueOrNull)
                        assertNull(timestampValueOrNull)
                        assertNull(textValueOrNull)
                        assertNull(stringValueOrNull)
                        assertNull(symbolValueOrNull)
                        assertNull(bytesValueOrNull)
                        assertNull(blobValueOrNull)
                        assertNull(clobValueOrNull)
                        assertNull(containerValuesOrNull)
                        assertNull(seqValuesOrNull)
                        assertNull(listValuesOrNull)
                        assertNull(sexpValuesOrNull)
                        assertNull(structFieldsOrNull)

                        assertNull(asBooleanOrNull())
                        assertNull(asIntOrNull())
                        assertNull(asFloatOrNull())
                        assertNull(asDecimalOrNull())
                        assertNull(asTimestampOrNull())
                        assertNull(asTextOrNull())
                        assertNull(asStringOrNull())
                        assertNull(asSymbolOrNull())
                        assertNull(asLobOrNull())
                        assertNull(asBlobOrNull())
                        assertNull(asClobOrNull())
                        assertNull(asContainerOrNull())
                        assertNull(asSeqOrNull())
                        assertNull(asListOrNull())
                        assertNull(asSexpOrNull())
                        assertNull(asStructOrNull())
                    }
                    BOOL -> {
                        if (isNull) {
                            checkThrows { booleanValue }
                            checkThrows { asBoolean() }
                        } else {
                            checkEquals(booleanValue)
                            checkEquals(asBoolean().booleanValue)
                        }
                        checkEquals(booleanValueOrNull)
                        checkEquals(asBooleanOrNull()?.booleanValue)
                    }
                    INT -> {
                        if (isNull) {
                            checkThrows { longValue }
                            checkThrows { asInt() }
                        } else {
                            checkEquals(longValue)
                            checkEquals(asInt().longValue)
                        }
                        checkEquals(longValueOrNull)
                        checkEquals(asIntOrNull()?.longValue)
                    }
                    FLOAT -> {
                        if (isNull) {
                            checkThrows { doubleValue }
                            checkThrows { asFloat() }
                        } else {
                            checkEquals(doubleValue)
                            checkEquals(asFloat().doubleValue)
                        }
                        checkEquals(doubleValueOrNull)
                        checkEquals(asFloatOrNull()?.doubleValue)
                    }
                    DECIMAL -> {
                        if (isNull) {
                            checkThrows { decimalValue }
                            checkThrows { asDecimal() }
                        } else {
                            checkEquals(decimalValue)
                            checkEquals(asDecimal().decimalValue)
                        }
                        checkEquals(decimalValueOrNull)
                        checkEquals(asDecimalOrNull()?.decimalValue)
                    }
                    TIMESTAMP -> {
                        if (isNull) {
                            checkThrows { timestampValue }
                            checkThrows { asTimestamp() }
                        } else {
                            checkEquals(timestampValue)
                            checkEquals(asTimestamp().timestampValue)
                        }
                        checkEquals(timestampValueOrNull)
                        checkEquals(asTimestampOrNull()?.timestampValue)
                    }
                    STRING -> {
                        if (isNull) {
                            checkThrows { textValue }
                            checkThrows { stringValue }
                            checkThrows { asText() }
                            checkThrows { asString() }
                        } else {
                            checkEquals(textValue)
                            checkEquals(stringValue)
                            checkEquals(asText().textValue)
                            checkEquals(asString().textValue)
                        }
                        checkEquals(textValueOrNull)
                        checkEquals(stringValueOrNull)

                        checkEquals(asTextOrNull()?.textValue)
                        checkEquals(asStringOrNull()?.textValue)
                    }
                    SYMBOL -> {
                        if (isNull) {
                            checkThrows { textValue }
                            checkThrows { symbolValue }
                            checkThrows { asText() }
                            checkThrows { asSymbol() }
                        } else {
                            checkEquals(textValue)
                            checkEquals(symbolValue)

                            checkEquals(asText().textValue)
                            checkEquals(asSymbol().textValue)
                        }
                        checkEquals(textValueOrNull)
                        checkEquals(symbolValueOrNull)

                        checkEquals(asTextOrNull()?.textValue)
                        checkEquals(asSymbolOrNull()?.textValue)
                    }
                    CLOB -> {
                        if (isNull) {
                            checkThrows { bytesValue }
                            checkThrows { clobValue }
                            checkThrows { asLob() }
                            checkThrows { asClob() }
                        } else {
                            checkEquals(bytesValue)
                            checkEquals(clobValue)
                            checkEquals(asLob().bytesValue)
                            checkEquals(asClob().bytesValue)
                        }
                        checkEquals(bytesValueOrNull)
                        checkEquals(clobValueOrNull)

                        checkEquals(asLobOrNull()?.bytesValue)
                        checkEquals(asClobOrNull()?.bytesValue)
                    }
                    BLOB -> {
                        if (isNull) {
                            checkThrows { bytesValue }
                            checkThrows { blobValue }

                            checkThrows { asLob().bytesValue }
                            checkThrows { asBlob().bytesValue }
                        } else {
                            checkEquals(bytesValue)
                            checkEquals(blobValue)

                            checkEquals(asLob().bytesValue)
                            checkEquals(asBlob().bytesValue)
                        }
                        checkEquals(bytesValueOrNull)
                        checkEquals(blobValueOrNull)
                        checkEquals(asLobOrNull()?.bytesValue)
                        checkEquals(asBlobOrNull()?.bytesValue)
                    }
                    LIST -> {
                        if (isNull) {
                            checkThrows { containerValues }
                            checkThrows { seqValues }
                            checkThrows { listValues }

                            checkThrows { asContainer() }
                            checkThrows { asSeq() }
                            checkThrows { asList() }
                        } else {
                            checkEquals(containerValues)
                            checkEquals(seqValues)
                            checkEquals(listValues)

                            checkEquals(asContainer().values)
                            checkEquals(asSeq().values)
                            checkEquals(asList().values)
                        }
                        checkEquals(containerValuesOrNull)
                        checkEquals(seqValuesOrNull)
                        checkEquals(listValuesOrNull)

                        checkEquals(asContainerOrNull()?.values)
                        checkEquals(asSeqOrNull()?.values)
                        checkEquals(asListOrNull()?.values)
                    }
                    SEXP -> {
                        if (isNull) {
                            checkThrows { containerValues }
                            checkThrows { seqValues }
                            checkThrows { sexpValues }

                            checkThrows { asContainer() }
                            checkThrows { asSeq() }
                            checkThrows { asSexp() }
                        } else {
                            checkEquals(containerValues)
                            checkEquals(seqValues)
                            checkEquals(sexpValues)

                            checkEquals(asContainer().values)
                            checkEquals(asSeq().values)
                            checkEquals(asSexp().values)
                        }
                        checkEquals(containerValuesOrNull)
                        checkEquals(seqValuesOrNull)
                        checkEquals(sexpValuesOrNull)

                        checkEquals(asContainerOrNull()?.values)
                        checkEquals(asSeqOrNull()?.values)
                        checkEquals(asSexpOrNull()?.values)
                    }
                    STRUCT -> {
                        if (isNull) {
                            checkThrows { containerValues }
                            checkThrows { asContainer() }
                        } else {
                            checkEquals(containerValues)
                            checkEquals(asContainer().values)
                        }
                        checkEquals(containerValuesOrNull)
                        checkEquals(asContainerOrNull()?.values)

                        // NOTE:  we do not test field names here, only values.  See [structFieldsTest].
                    }
                }
            }
        }

        val TEXT_TYPES = listOf(SYMBOL, STRING)
        val LOB_TYPES = listOf(BLOB, CLOB)
        val CONTAINER_TYPES = listOf(LIST, SEXP, STRUCT)

        /**
         * Asserts that accessing a value accessor for the wrong type of the given [element] throws correctly.
         *
         * Note that in the [AnyElement] implementation the type check occurs before the null check so this
         * tests both the non-null and null value accessors.
         */
        private fun assertThrowsForWrongAccessorTypes(element: AnyElement) {
            with(element) {
                if (type == NULL) {
                    // No checks needed in this case because:
                    // - Non-null accessors will throw for due to the value being unexpectedly null. (checked in [assertAccessors]).
                    // - *OrNull accessors never throw in this case.
                    return
                }

                if (type != BOOL) {
                    checkThrows { booleanValue }
                    checkThrows { booleanValueOrNull }

                    checkThrows { asBoolean() }
                    checkThrows { asBooleanOrNull() }
                }

                if (type != INT) {
                    checkThrows { longValue }
                    checkThrows { longValueOrNull }

                    checkThrows { asInt() }
                    checkThrows { asIntOrNull() }
                }

                if (type != FLOAT) {
                    checkThrows { doubleValue }
                    checkThrows { doubleValueOrNull }

                    checkThrows { asFloat() }
                    checkThrows { asFloatOrNull() }
                }

                if (type != DECIMAL) {
                    checkThrows { decimalValue }
                    checkThrows { decimalValueOrNull }

                    checkThrows { asDecimal() }
                    checkThrows { asDecimalOrNull() }
                }

                if (type != TIMESTAMP) {
                    checkThrows { timestampValue }
                    checkThrows { timestampValueOrNull }

                    checkThrows { asTimestamp() }
                    checkThrows { asTimestampOrNull() }
                }

                if (type !in TEXT_TYPES) {
                    checkThrows { textValue }
                    checkThrows { textValueOrNull }

                    checkThrows { asText() }
                    checkThrows { asTextOrNull() }
                }

                if (type != SYMBOL) {
                    checkThrows { symbolValue }
                    checkThrows { symbolValueOrNull }

                    checkThrows { asSymbol() }
                    checkThrows { asSymbolOrNull() }
                }

                if (type != STRING) {
                    checkThrows { stringValue }
                    checkThrows { stringValueOrNull }
                }

                if (type !in LOB_TYPES) {
                    checkThrows { bytesValue }
                    checkThrows { bytesValueOrNull }

                    checkThrows { asLob() }
                    checkThrows { asLobOrNull() }
                }
                if (type != CLOB) {
                    checkThrows { clobValue }
                    checkThrows { clobValueOrNull }

                    checkThrows { asClob() }
                    checkThrows { asClobOrNull() }
                }
                if (type != BLOB) {
                    checkThrows { blobValue }
                    checkThrows { blobValueOrNull }

                    checkThrows { asBlob() }
                    checkThrows { asBlobOrNull() }
                }

                if (type !in CONTAINER_TYPES) {
                    checkThrows { containerValues }
                    checkThrows { containerValuesOrNull }

                    checkThrows { asContainer() }
                    checkThrows { asContainerOrNull() }
                }

                if (type != LIST) {
                    checkThrows { listValues }
                    checkThrows { listValuesOrNull }

                    checkThrows { asList() }
                    checkThrows { asListOrNull() }
                }
                if (type != SEXP) {
                    checkThrows { sexpValues }
                    checkThrows { sexpValuesOrNull }

                    checkThrows { asSexp() }
                    checkThrows { asSexpOrNull() }
                }
                if (type != STRUCT) {
                    checkThrows { structFields }
                    checkThrows { structFieldsOrNull }

                    checkThrows { asStruct() }
                    checkThrows { asStructOrNull() }
                }
            }
        }
    }
}
