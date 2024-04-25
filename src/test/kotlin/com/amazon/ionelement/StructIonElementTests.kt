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

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.IonElementException
import com.amazon.ionelement.api.StructField
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.loadSingleElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class StructIonElementTests {
    val struct = loadSingleElement("{ a: 1, b: 2, b: 3 }").asStruct()

    @Test
    fun size() {
        assertEquals(3, struct.size, "struct size should be 3")
    }

    @Test
    fun fields() {
        val structFields = struct.fields.toList()
        assertEquals(3, structFields.size)
        structFields.assertHasField("a", ionInt(1))
        structFields.assertHasField("b", ionInt(2))
        structFields.assertHasField("b", ionInt(3))
    }

    @Test
    fun values() {
        val values = struct.values.toList()
        assertEquals(3, values.size, "3 values should be present")
        assertDoesNotThrow("value 1 should be present") { values.single { it.longValue == 1L } }
        assertDoesNotThrow("value 2 should be present") { values.single { it.longValue == 2L } }
        assertDoesNotThrow("value 3 should be present") { values.single { it.longValue == 3L } }
    }

    @Test
    fun get() {
        assertEquals(
            ionInt(1), struct["a"],
            "value is returned when field is present"
        )

        val b1 = struct["b"]
        assertTrue(
            listOf(ionInt(2), ionInt(3)).any { it == b1 },
            "any value of the b field is returned (duplicate field name)"
        )

        val ex = assertThrows<IonElementException>("exception is thrown when field is not present") {
            struct["z"]
        }
        assertTrue(
            ex.message!!.contains("'z'"),
            "Exception message must contain the missing field"
        )
    }

    @Test
    fun getOptional() {
        assertEquals(
            ionInt(1), struct.getOptional("a"),
            "value is returned when field is present"
        )

        val b2 = struct.getOptional("b")
        assertTrue(
            listOf(ionInt(1), ionInt(2)).any { it == b2 },
            "any value of the b field is returned (duplicate field name)"
        )

        assertNull(
            struct.getOptional("z"),
            "null is returned when the field is not present."
        )
    }

    @Test
    fun containsField() {
        assertTrue(struct.containsField("a"))
        assertTrue(struct.containsField("b"))
        assertFalse(struct.containsField("z"))
    }

    private fun Iterable<StructField>.assertHasField(expectedName: String, expectedValue: IonElement) {
        assertTrue(this.any { (name, value) -> name == expectedName && value == expectedValue }, "Must have field '$expectedName'")
    }
}
