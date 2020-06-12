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

import com.amazon.ionelement.api.Element
import com.amazon.ionelement.api.IonElectrolyteException
import com.amazon.ionelement.api.IonStructField
import com.amazon.ionelement.api.createIonElementLoader
import com.amazon.ionelement.api.ionInt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StructIonElementTests {

    @Test
    fun fieldAccessorTests() {
        val struct =  createIonElementLoader().loadSingleElement("{ a: 1, b: 2, b: 3 }").asStruct()

        // TODO: need to add similar tests for StructElement.values
        // TODO: need to review this and see if it still covers other parts of the modified API.

        val structFields = struct.fields
        assertEquals(3, struct.size)
        structFields.assertHasField("a", ionInt(1))
        structFields.assertHasField("b", ionInt(2))
        structFields.assertHasField("b", ionInt(3))

        assertEquals(3, struct.size)

        // The following tests aren't needed assuming fieldNames doesn't get changed to a [Set<String>]
//        val fields = struct.fieldNames
//        assertEquals(2, struct.size)
        //listOf("a", "b").all { expectedFieldName -> fields.any { fieldName -> fieldName == expectedFieldName }}
        //assertTrue(fields.containsAll(listOf("a", "b")))

        assertEquals(3, struct.size)
        //val values = struct.values
        //assertTrue(values.containsAll(listOf(ionInt(1), ionInt(2), ionInt(3))))
        assertTrue(
            listOf(ionInt(1), ionInt(2), ionInt(3))
                .all { expectedValue -> struct.values.any { it == expectedValue }})


        assertEquals(ionInt(1), struct["a"])
        assertEquals(ionInt(2), struct["b"])

        assertEquals(ionInt(1), struct.getOptional("a"))
        assertEquals(ionInt(2), struct.getOptional("b"))

        val ex = assertThrows<IonElectrolyteException> { struct["z"] }
        assertTrue(ex.message!!.contains("'z'"))

        assertNull(struct.getOptional("z"))
    }

    private fun Iterable<IonStructField>.assertHasField(fieldName: String, value: Element) {
        assertTrue(this.any { it.name == fieldName && it.value == value })
    }

}