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

import com.amazon.ion.IonWriter
import com.amazon.ionelement.api.*
import com.amazon.ionelement.api.ImmutableMetaContainer
import com.amazon.ionelement.api.constraintError
import com.amazon.ionelement.impl.collections.*
import java.math.BigInteger

internal class BigIntIntElementImpl(
    override val bigIntegerValue: BigInteger,
    override val annotations: ImmutableList<String>,
    override val metas: ImmutableMetaContainer
) : AnyElementBase(), IntElement {

    override val type: ElementType get() = ElementType.INT

    override val integerSize: IntElementSize
        get() = if (bigIntegerValue in RANGE_OF_LONG) IntElementSize.LONG else IntElementSize.BIG_INTEGER

    override val longValue: Long get() {
        if (integerSize != IntElementSize.LONG) {
            constraintError(this, "Ion integer value outside of range of 64 bit signed integer, use bigIntegerValue instead.")
        }
        return bigIntegerValue.longValueExact()
    }

    override fun copy(annotations: List<String>, metas: MetaContainer): BigIntIntElementImpl =
        BigIntIntElementImpl(bigIntegerValue, annotations.toImmutableList(), metas.toImmutableMap())

    override fun withAnnotations(vararg additionalAnnotations: String): BigIntIntElementImpl = _withAnnotations(*additionalAnnotations)
    override fun withAnnotations(additionalAnnotations: Iterable<String>): BigIntIntElementImpl = _withAnnotations(additionalAnnotations)
    override fun withoutAnnotations(): BigIntIntElementImpl = _withoutAnnotations()
    override fun withMetas(additionalMetas: MetaContainer): BigIntIntElementImpl = _withMetas(additionalMetas)
    override fun withMeta(key: String, value: Any): BigIntIntElementImpl = _withMeta(key, value)
    override fun withoutMetas(): BigIntIntElementImpl = _withoutMetas()

    override fun writeContentTo(writer: IonWriter) = writer.writeInt(bigIntegerValue)

    override fun equals(other: Any?): Boolean = isEquivalentTo(other)
    override fun hashCode(): Int = hashElement(this)
}

internal val RANGE_OF_LONG = BigInteger.valueOf(Long.MIN_VALUE)..BigInteger.valueOf(Long.MAX_VALUE)
