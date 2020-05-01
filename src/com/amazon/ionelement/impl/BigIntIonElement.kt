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

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.emptyMetaContainer
import com.amazon.ionelement.api.ionError
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonType
import com.amazon.ion.IonWriter
import java.math.BigInteger

internal class BigIntIonElement(
    val value: BigInteger,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
) : IonElementBase() {

    override val type: IonType get() = IonType.INT

    override val integerSize: IntegerSize get() = IntegerSize.BIG_INTEGER

    override val longValueOrNull: Long get() {
        if(value > MAX_LONG_AS_BIG_INT || value < MIN_LONG_AS_BIG_INT) {
            ionError(this, "Ion integer value outside of range of 64 bit signed integer, use bigIntegerValue instead.")
        }
        return value.longValueExact()
    }

    override val bigIntegerValueOrNull: BigInteger? get() = value

    override fun clone(annotations: List<String>, metas: MetaContainer): IonElement =
        BigIntIonElement(value, annotations, metas)

    override fun writeContentTo(writer: IonWriter) = writer.writeInt(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BigIntIonElement

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

internal val MAX_LONG_AS_BIG_INT = BigInteger.valueOf(Long.MAX_VALUE)
internal val MIN_LONG_AS_BIG_INT = BigInteger.valueOf(Long.MIN_VALUE)