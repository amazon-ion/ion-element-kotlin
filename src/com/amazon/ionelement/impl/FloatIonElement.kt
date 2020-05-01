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
import com.amazon.ion.IonType
import com.amazon.ion.IonWriter
import kotlin.math.sign

internal class FloatIonElement(
    val value: Double,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
) : IonElementBase() {
    override val type: IonType get() = IonType.FLOAT
    override val doubleValueOrNull: Double get() = value

    override fun clone(annotations: List<String>, metas: MetaContainer): IonElement =
        FloatIonElement(value, annotations, metas)

    override fun writeContentTo(writer: IonWriter) = writer.writeFloat(value)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FloatIonElement

        // compareTo() distinguishes between 0.0 and -0.0 while `==` operator does not.
        if (value.compareTo(other.value) != 0) return false
        if (annotations != other.annotations) return false
        // Note: metas intentionally omitted!

        return true
    }

    override fun hashCode(): Int {
        var result = value.compareTo(0.0).hashCode() // <-- causes 0e0 to have a different hash code than -0e0
        result = 31 * result + value.hashCode()
        result = 31 * result + annotations.hashCode()
        // Note: metas intentionally omitted!
        return result
    }

}