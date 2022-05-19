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

@file:JvmName("Constants")
package com.amazon.ionelement.util

import com.amazon.ion.Decimal

@JvmField
val TOP_LEVEL_STRUCTS_ION_TEXT = """
stock_item::{
    name: "Fantastic Widget",
    price: 12.34,
    countInStock: 2,
    orders: [
        { customerId: 123, state: WA },
        { customerId: 456, state: "HI" }
    ]
}
stock_item::{ // stock item has no name
    price: 23.45,
    countInStock: 20,
    orders: [
        { customerId: 234, state: "VA" },
        { customerId: 567, state: MI }
    ]
}
    """

data class StockItem(val name: String, val price: Decimal, val countInStock: Long, val orders: List<Order>)
data class Order(val customerId: Long, val state: String)
