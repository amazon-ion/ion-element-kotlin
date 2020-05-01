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
import com.amazon.ionelement.api.IonElementContainer
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.emptyMetaContainer
import com.amazon.ion.IonWriter

internal abstract class OrderedIonElementArray(
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
): IonElementBase(), IonElementContainer {
    abstract val values: List<IonElement>

    override val containerValueOrNull: IonElementContainer get() = this

    override fun writeContentTo(writer: IonWriter) {
        writer.stepIn(type)
        values.forEach {
            it.writeTo(writer)
        }
        writer.stepOut()
    }
    // List<IonElement> implementation (inherited from Ion
    override val size: Int get() = values.size
    override fun get(index: Int): IonElement = values[index]
    override fun iterator(): Iterator<IonElement> = values.iterator()
    override fun contains(element: IonElement): Boolean = values.contains(element)
    override fun containsAll(elements: Collection<IonElement>): Boolean = values.containsAll(elements)
    override fun isEmpty(): Boolean = values.isEmpty()
    override fun indexOf(element: IonElement): Int = values.indexOf(element)
    override fun lastIndexOf(element: IonElement): Int = values.lastIndexOf(element)
    override fun listIterator(): ListIterator<IonElement> = values.listIterator()
    override fun listIterator(index: Int): ListIterator<IonElement> = values.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<IonElement> = values.subList(fromIndex, toIndex)
}

