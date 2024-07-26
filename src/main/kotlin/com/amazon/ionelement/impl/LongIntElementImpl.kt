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
import com.amazon.ionelement.impl.collections.*
import java.math.BigInteger

internal class LongIntElementImpl(
    override val longValue: Long,
    override val annotations: ImmutableList<String>,
    override val metas: ImmutableMetaContainer
) : AnyElementBase(), IntElement {
    override val integerSize: IntElementSize get() = IntElementSize.LONG
    override val type: ElementType get() = ElementType.INT

    override val bigIntegerValue: BigInteger get() = BigInteger.valueOf(longValue)

    override fun copy(annotations: List<String>, metas: MetaContainer): LongIntElementImpl =
        LongIntElementImpl(longValue, annotations.toImmutableList(), metas.toImmutableMap())

    override fun withAnnotations(vararg additionalAnnotations: String): LongIntElementImpl = _withAnnotations(*additionalAnnotations)
    override fun withAnnotations(additionalAnnotations: Iterable<String>): LongIntElementImpl = _withAnnotations(additionalAnnotations)
    override fun withoutAnnotations(): LongIntElementImpl = _withoutAnnotations()
    override fun withMetas(additionalMetas: MetaContainer): LongIntElementImpl = _withMetas(additionalMetas)
    override fun withMeta(key: String, value: Any): LongIntElementImpl = _withMeta(key, value)
    override fun withoutMetas(): LongIntElementImpl = _withoutMetas()

    override fun writeContentTo(writer: IonWriter) = writer.writeInt(longValue)

    override fun equals(other: Any?): Boolean = isEquivalentTo(other)
    override fun hashCode(): Int = hashElement(this)
}
