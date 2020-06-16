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
import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.SymbolElement
import com.amazon.ionelement.api.emptyMetaContainer

internal class SymbolIonElement(
    value: String,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
): TextIonElement(value), SymbolElement {
    override val type: ElementType get() = ElementType.SYMBOL

    override val symbolValue: String get() = textValue

    override fun copy(annotations: List<String>, metas: MetaContainer): AnyElement =
        SymbolIonElement(textValue, annotations, metas)

    override fun writeContentTo(writer: IonWriter) = writer.writeSymbol(textValue)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SymbolIonElement

        if (textValue != other.textValue) return false
        if (annotations != other.annotations) return false
        // Note: metas intentionally omittted!

        return true
    }

    override fun hashCode(): Int {
        var result = textValue.hashCode()
        result = 31 * result + annotations.hashCode()
        // Note: metas intentionally omittted!
        return result
    }
}