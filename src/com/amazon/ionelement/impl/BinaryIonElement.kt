/*
 * Copyright (c) 2020. Amazon.com, Inc. or its affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.ionelement.impl

import com.amazon.ionelement.api.IonByteArray

internal abstract class BinaryIonElement: IonElementBase(), IonByteArray {
    protected abstract val bytes: ByteArray

    override val bytesValueOrNull: IonByteArray get() = this

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

