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
import com.amazon.ion.system.IonReaderBuilder
import com.amazon.ionelement.api.*
import com.amazon.ionelement.util.INCLUDE_LOCATION_META
import com.amazon.ionelement.util.ION
import com.amazon.ionelement.util.IonElementLoaderTestCase
import com.amazon.ionelement.util.convertToString
import java.math.BigInteger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Basic examples of what instantiating instances of IonElement looks like from Kotlin.
 *
 * Also demonstrates converting to and from the mutable DOM.
 */
class IonElementLoaderTests {
    @ParameterizedTest
    @MethodSource("parametersForDemoTest")
    fun kotlinIdiomaticTest(tc: IonElementLoaderTestCase) {
        val parsedIonValue = ION.singleValue(tc.textIon)
        val parsedIonElement = loadSingleElement(tc.textIon, INCLUDE_LOCATION_META)

        // Text generated from both should match
        assertEquals(convertToString(parsedIonValue), parsedIonElement.toString())

        // Converting from IonElement to IonValue results in an IonValue that is equivalent to the parsed IonValue
        assertEquals(parsedIonElement.toIonValue(ION), parsedIonValue)

        // Converting from IonValue to IonElement should result in an IonElement that is equivalent to the
        // parsed IonElement
        assertEquals(parsedIonValue.toIonElement(), parsedIonElement)

        assertEquals(tc.expectedElement, parsedIonElement)
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun parametersForDemoTest() = listOf(
            IonElementLoaderTestCase("true", ionBool(true)),

            IonElementLoaderTestCase("false", ionBool(false)),

            IonElementLoaderTestCase("1", ionInt(1)),

            IonElementLoaderTestCase("9223372036854775807", ionInt(Long.MAX_VALUE)),

            IonElementLoaderTestCase("9223372036854775808", ionInt(BigInteger.valueOf(Long.MAX_VALUE) + BigInteger.ONE)),

            IonElementLoaderTestCase("existence::42", ionInt(42).withAnnotations("existence")),

            IonElementLoaderTestCase("0.", ionDecimal(Decimal.ZERO)),

            IonElementLoaderTestCase("1e0", ionFloat(1.0)),

            IonElementLoaderTestCase("2019-10-30T04:23:59Z", ionTimestamp("2019-10-30T04:23:59Z")),

            IonElementLoaderTestCase("\"some string\"", ionString("some string")),

            IonElementLoaderTestCase("'some symbol'", ionSymbol("some symbol")),

            IonElementLoaderTestCase("{{\"some clob\"}}", ionClob("some clob".encodeToByteArray())),

            IonElementLoaderTestCase("{{ }}", ionBlob(byteArrayOf())),

            IonElementLoaderTestCase("[1, 2, 3]", ionListOf(ionInt(1), ionInt(2), ionInt(3))),

            IonElementLoaderTestCase("(1 2 3)", ionSexpOf(ionInt(1), ionInt(2), ionInt(3))),

            IonElementLoaderTestCase(
                "{ foo: 1, bar: 2, bat: 3 }",
                ionStructOf(
                    "foo" to ionInt(1),
                    "bar" to ionInt(2),
                    "bat" to ionInt(3)
                )
            ),

            // Nested container cases
            IonElementLoaderTestCase("((null.list))", ionSexpOf(ionSexpOf(ionNull(ElementType.LIST)))),
            IonElementLoaderTestCase("(1 (2 3))", ionSexpOf(ionInt(1), ionSexpOf(ionInt(2), ionInt(3)))),
            IonElementLoaderTestCase("{foo:[1]}", ionStructOf("foo" to ionListOf(ionInt(1)))),
            IonElementLoaderTestCase("[{foo:1}]", ionListOf(ionStructOf("foo" to ionInt(1)))),
            IonElementLoaderTestCase("{foo:{bar:1}}", ionStructOf("foo" to ionStructOf("bar" to ionInt(1)))),
            IonElementLoaderTestCase("{foo:[{}]}", ionStructOf("foo" to ionListOf(ionStructOf(emptyList())))),
            IonElementLoaderTestCase("[{}]", ionListOf(ionStructOf(emptyList()))),
            IonElementLoaderTestCase("[{}, {}]", ionListOf(ionStructOf(emptyList()), ionStructOf(emptyList()))),
            IonElementLoaderTestCase("[{foo:1, bar: 2}]", ionListOf(ionStructOf("foo" to ionInt(1), "bar" to ionInt(2)))),
            IonElementLoaderTestCase(
                "{foo:[{bar:({})}]}",
                ionStructOf("foo" to ionListOf(ionStructOf("bar" to ionSexpOf(ionStructOf(emptyList())))))
            ),
        )
    }

    @Test
    fun `regardless of depth, no StackOverflowError is thrown`() {
        // Throws StackOverflowError in ion-element@v1.2.0 and prior versions when there's ~4k nested containers
        // Run for every container type to ensure that they all correctly fall back to the iterative impl.

        val listData = "[".repeat(999999) + "]".repeat(999999)
        loadAllElements(listData)

        val sexpData = "(".repeat(999999) + ")".repeat(999999)
        loadAllElements(sexpData)

        val structData = "{a:".repeat(999999) + "b" + "}".repeat(999999)
        loadAllElements(structData)
    }

    @ParameterizedTest
    @MethodSource("parametersForDemoTest")
    fun `deeply nested values should be loaded correctly`(tc: IonElementLoaderTestCase) {
        // Wrap everything in many layers of Ion lists so that we can be sure to trigger the iterative fallback.
        val nestingLevels = 500
        val textIon = "[".repeat(nestingLevels) + tc.textIon + "]".repeat(nestingLevels)
        var expectedElement = tc.expectedElement
        repeat(nestingLevels) { expectedElement = ionListOf(expectedElement) }

        val parsedIonValue = ION.singleValue(textIon)
        val parsedIonElement = loadSingleElement(textIon, INCLUDE_LOCATION_META)

        // Text generated from both should match
        assertEquals(convertToString(parsedIonValue), parsedIonElement.toString())

        // Converting from IonElement to IonValue results in an IonValue that is equivalent to the parsed IonValue
        assertEquals(parsedIonElement.toIonValue(ION), parsedIonValue)

        // Converting from IonValue to IonElement should result in an IonElement that is equivalent to the
        // parsed IonElement
        assertEquals(parsedIonValue.toIonElement(), parsedIonElement)

        assertEquals(expectedElement, parsedIonElement)
    }

    @Test
    fun `loadCurrentElement throws exception when not positioned on a value`() {
        val reader = IonReaderBuilder.standard().build("foo")
        // We do not advance to the first value in the reader.
        assertThrows<IllegalArgumentException> { loadCurrentElement(reader) }
    }

    @Test
    fun `loadSingleElement throws exception when no values in reader`() {
        assertThrows<IllegalArgumentException> { loadSingleElement("") }
    }

    @Test
    fun `loadSingleElement throws exception when more than one values in reader`() {
        assertThrows<IllegalArgumentException> { loadSingleElement("a b") }
    }
}
