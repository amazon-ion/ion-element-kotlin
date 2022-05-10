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

import com.amazon.ionelement.api.createIonElementLoader
import com.amazon.ionelement.api.ionBool
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionListOf
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionTimestamp
import com.amazon.ionelement.api.loadSingleElement
import com.amazon.ionelement.api.toIonElement
import com.amazon.ionelement.api.toIonValue
import com.amazon.ionelement.util.ION
import com.amazon.ionelement.util.INCLUDE_LOCATION_META
import com.amazon.ionelement.util.IonElementLoaderTestCase
import com.amazon.ionelement.util.convertToString
import org.junit.jupiter.api.Assertions.assertEquals
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
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun parametersForDemoTest() = listOf(
            IonElementLoaderTestCase("true", ionBool(true)),

            IonElementLoaderTestCase("false", ionBool(false)),

            IonElementLoaderTestCase("1", ionInt(1)),

            IonElementLoaderTestCase("existence::42", ionInt(1).withAnnotations("existence")),

            IonElementLoaderTestCase("\"some string\"", ionString("some string")),

            IonElementLoaderTestCase("2019-10-30T04:23:59Z", ionTimestamp("2019-10-30T04:23:59Z")),

            IonElementLoaderTestCase("[1, 2, 3]", ionListOf(ionInt(1), ionInt(2), ionInt(3))),

            IonElementLoaderTestCase("(1 2 3)", ionListOf(ionInt(1), ionInt(2), ionInt(3))),

            IonElementLoaderTestCase(
                "{ foo: 1, bar: 2, bat: 3 }",
                ionStructOf(
                    "foo" to ionInt(1),
                    "bar" to ionInt(2),
                    "bat" to ionInt(3))))
    }
}

