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

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.emptyMetaContainer
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonType
import com.amazon.ion.IonWriter
import java.math.BigInteger

internal class IntIonElement(
    val value: Long,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
) : IonElementBase() {
    override val type: IonType get() = IonType.INT
    override val longValueOrNull: Long get() = value
    override val bigIntegerValueOrNull: BigInteger? get() = BigInteger.valueOf(value)

    // It is also possible to return IntegerSize.INT, but why would we I do not know...
    override val integerSize: IntegerSize get() = IntegerSize.LONG

    override fun clone(annotations: List<String>, metas: MetaContainer): IonElement =
        IntIonElement(value, annotations, metas)

    override fun writeContentTo(writer: IonWriter) = writer.writeInt(value)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntIonElement

        if (value != other.value) return false
        if (annotations != other.annotations) return false
        // Note: metas intentionally omitted!

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + annotations.hashCode()
        // Note: metas intentionally omitted!
        return result
    }
}

