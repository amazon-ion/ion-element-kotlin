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

import com.amazon.ionelement.api.IonByteArray
import com.amazon.ionelement.api.LobElement

internal abstract class BinaryIonElement(
    protected val bytes: ByteArray
): IonElementBase(), IonByteArray, LobElement {

    override val bytesValue: IonByteArray get() = this

    // IonByteArray implementation
    override fun size(): Int = bytes.size
    override fun get(index: Int): Byte = bytes[index]
    override fun iterator(): Iterator<Byte> = bytes.iterator()

    override fun copyOfBytes(): ByteArray = bytes.clone()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is BinaryIonElement -> false
            type != other.type -> false
            !bytes.contentEquals(other.bytes) -> false
            annotations != other.annotations -> false
            // Metas are intentionally omitted here.
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + annotations.hashCode()
        // Metas are intentionally omitted here.
        return result
    }

}

