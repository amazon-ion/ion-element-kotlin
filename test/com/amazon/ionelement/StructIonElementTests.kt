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
import com.amazon.ionelement.api.IonStructField
import com.amazon.ionelement.api.createIonElementLoader
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionStructOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class StructIonElementTests {

    @Test
    fun fieldAccessorTests() {
        val struct =  createIonElementLoader().loadSingleElement("{ a: 1, b: 2, b: 3 }").structValueOrNull!!

        val structFields = struct.toList()
        assertEquals(3, structFields.size)
        structFields.assertHasField("a", ionInt(1))
        structFields.assertHasField("b", ionInt(2))
        structFields.assertHasField("b", ionInt(3))

        assertEquals(3, struct.size)
        val fields = struct.fieldNames
        assertEquals(2, fields.size)
        assertTrue(fields.containsAll(listOf("a", "b")))

        val values = struct.values
        assertEquals(3, values.size)
        assertTrue(values.containsAll(listOf(ionInt(1), ionInt(2), ionInt(3))))

        assertEquals(ionInt(1), struct.first("a"))
        assertEquals(ionInt(2), struct.first("b"))

        assertEquals(ionInt(1), struct.firstOrNull("a"))
        assertEquals(ionInt(2), struct.firstOrNull("b"))

        val ex = assertThrows<IonElectrolyteException> { struct.first("z" ) }
        assertTrue(ex.message!!.contains("'z'"))

        assertNull(struct.firstOrNull("z"))
    }

    private fun List<IonStructField>.assertHasField(fieldName: String, value: IonElement) {
        assertTrue(this.any { it.name == fieldName && it.value == value })
    }

}