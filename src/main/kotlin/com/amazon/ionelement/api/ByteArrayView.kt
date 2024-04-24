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

package com.amazon.ionelement.api

/**
 * An immutable wrapper over a [ByteArray].
 *
 * This type uses value-based equality—two [ByteArrayView]s are equal if they have equal content.
 * Implementations MUST override [Any.hashCode] to return the same integer that would be return by calling
 * [copyOfBytes]`()`.[contentHashCode][ByteArray.contentHashCode]`()`.
 */
public interface ByteArrayView : Iterable<Byte> {
    public fun size(): Int
    public operator fun get(index: Int): Byte

    public fun copyOfBytes(): ByteArray
}
