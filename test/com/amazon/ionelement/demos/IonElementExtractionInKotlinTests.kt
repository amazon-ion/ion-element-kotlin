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

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.createIonElementLoader
import com.amazon.ionelement.util.ION
import com.amazon.ionelement.util.Order
import com.amazon.ionelement.util.StockItem
import com.amazon.ionelement.util.TOP_LEVEL_STRUCTS_ION_TEXT
import com.amazon.ion.Decimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IonElementExtractionInKotlinTests {

    /**
     * Demonstrates data extraction with non-nullable data type accessors.
     * (Seems so much more concise and readable... IMHO...)
     */
    @Test
    fun extractFromStructsDemo() {
        val stockItems = ION.newReader(TOP_LEVEL_STRUCTS_ION_TEXT).use { reader ->
            createIonElementLoader(includeLocations = true)
                .loadAllElements(reader)
                .map { stockItem: IonElement ->
                    stockItem.structValue.run {
                        StockItem(
                            firstOrNull("name")?.textValue ?: "<unknown name>",
                            first("price").decimalValue,
                            first("countInStock").longValue,
                            first("orders").containerValue.map { order ->
                                order.structValue.run {
                                    Order(
                                        first("customerId").longValue,
                                        first("state").textValue)
                                }
                            })
                    }
                }
        }.asSequence().toList()

        assertTestCaseExtraction(stockItems)
    }

    private fun assertTestCaseExtraction(stockItems: List<StockItem>) {
        val expectedTestCases = listOf(
            StockItem(
                "Fantastic Widget", Decimal.valueOf("12.34"), 2,
                listOf(
                    Order(123, "WA"),
                    Order(456, "HI")
                )),
            StockItem(
                "<unknown name>", Decimal.valueOf("23.45"), 20,
                listOf(
                    Order(234, "VA"),
                    Order(567, "MI")
                )))


        assertEquals(expectedTestCases, stockItems)
    }
}

