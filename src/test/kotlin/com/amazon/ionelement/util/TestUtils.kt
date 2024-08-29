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

@file:JvmName("TestUtils")
package com.amazon.ionelement.util
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionelement.api.IonElementLoaderOptions

@JvmField
val ION = IonSystemBuilder.standard().build()

fun convertToString(ionValue: IonValue): String {
    val builder = StringBuilder()
    ION.newTextWriter(builder).use {
        ionValue.writeTo(it)
    }
    return builder.toString()
}

/**
 * A convenience instance of [IonElementLoaderOptions] with all the defaults except with
 * [IonElementLoaderOptions.includeLocationMeta] set to `true`.
 *
 * This is to support this somewhat commonly used scenario.
 */
val INCLUDE_LOCATION_META = IonElementLoaderOptions { includeLocationMeta = true }
