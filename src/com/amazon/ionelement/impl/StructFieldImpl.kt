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

package com.amazon.ionelement.impl

import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.StructField

/**
 * Provides an interface for storing an Ion `struct`'s field and its value.
 */
internal data class StructFieldImpl(
    override val name: String,
    override val value: AnyElement
) : StructField {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructField) return false
        if (name != other.name) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + value.hashCode()
    }
}
