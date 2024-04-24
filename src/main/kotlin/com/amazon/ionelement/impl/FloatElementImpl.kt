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
import com.amazon.ionelement.api.PersistentMetaContainer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentMap

internal class FloatElementImpl(
    override val doubleValue: Double,
    override val annotations: PersistentList<String>,
    override val metas: PersistentMetaContainer
) : AnyElementBase(), FloatElement {
    override val type: ElementType get() = ElementType.FLOAT

    override fun copy(annotations: List<String>, metas: MetaContainer): FloatElementImpl =
        FloatElementImpl(doubleValue, annotations.toEmptyOrPersistentList(), metas.toPersistentMap())

    override fun withAnnotations(vararg additionalAnnotations: String): FloatElementImpl = _withAnnotations(*additionalAnnotations)
    override fun withAnnotations(additionalAnnotations: Iterable<String>): FloatElementImpl = _withAnnotations(additionalAnnotations)
    override fun withoutAnnotations(): FloatElementImpl = _withoutAnnotations()
    override fun withMetas(additionalMetas: MetaContainer): FloatElementImpl = _withMetas(additionalMetas)
    override fun withMeta(key: String, value: Any): FloatElementImpl = _withMeta(key, value)
    override fun withoutMetas(): FloatElementImpl = _withoutMetas()

    override fun writeContentTo(writer: IonWriter) = writer.writeFloat(doubleValue)

    override fun equals(other: Any?): Boolean = isEquivalentTo(other)
    override fun hashCode(): Int = hashElement(this)
}
