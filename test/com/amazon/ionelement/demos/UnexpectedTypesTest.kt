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

package com.amazon.ionelement.demos

import com.amazon.ionelement.api.IonElementException
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.IonLocation
import com.amazon.ionelement.api.IonTextLocation
import com.amazon.ionelement.api.createIonElementLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Demonstrates throwing of [IonElementException] when an unexpected data type or nulliness is encountered.
 */
class UnexpectedTypesTest {
    private val loader = createIonElementLoader(includeLocations = true)

    data class TestCase(
        val ionText: String,
        val expectedIonLocation: IonLocation,
        val block: (AnyElement) -> Unit)


    @ParameterizedTest
    @MethodSource("parametersForIonElectrolyteExceptionTest")
    fun ionElectrolyteExceptionTest(tc: TestCase) {
        val ionElement = loader.loadSingleElement(tc.ionText)
        val ex = assertThrows<IonElementException> { tc.block(ionElement) }
        assertEquals(tc.expectedIonLocation, ex.location)
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun parametersForIonElectrolyteExceptionTest() = listOf(
            TestCase("\n\n    1", IonTextLocation(3, 5)) { it.stringValueOrNull },
            TestCase("\n   null.string", IonTextLocation(2, 4)) {
                it.asListOrNull() ?: error("Unexpected null")
            },
            TestCase("""{ some_field: [ 1, 2, 3, 4, "err" ] }""", IonTextLocation(1, 27)) {
                it.asStructOrNull()?.run {
                    get("some_field").asContainerOrNull()?.values?.map { it.longValueOrNull ?: error("Unexpected null") }
                } ?: error("Unexpected null")
            }
        )
    }

}