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

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.loadAllElements
import com.amazon.ionelement.util.INCLUDE_LOCATION_META
import com.amazon.ionelement.util.ION
import com.amazon.ionelement.util.Order
import com.amazon.ionelement.util.StockItem
import com.amazon.ionelement.util.TOP_LEVEL_STRUCTS_ION_TEXT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnyElementAccessorsDemo {

    /**
     * Demonstrates data extraction with non-nullable data type accessors.
     * (Seems so much more concise and readable... IMHO...)
     */
    @Test
    fun extractFromStructsDemo() {
        val stockItems = ION.newReader(TOP_LEVEL_STRUCTS_ION_TEXT).use { reader ->
            loadAllElements(reader, INCLUDE_LOCATION_META)
                .map { stockItem: AnyElement ->
                    stockItem.asStruct().run {
                        StockItem(
                            getOptional("name")?.textValue ?: "<unknown name>",
                            get("price").decimalValue,
                            get("countInStock").longValue,
                            get("orders").asList().values.map { order ->
                                order.asStruct().run {
                                    Order(
                                        get("customerId").longValue,
                                        get("state").textValue
                                    )
                                }
                            }
                        )
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
                )
            ),
            StockItem(
                "<unknown name>", Decimal.valueOf("23.45"), 20,
                listOf(
                    Order(234, "VA"),
                    Order(567, "MI")
                )
            )
        )

        assertEquals(expectedTestCases, stockItems)
    }
}
