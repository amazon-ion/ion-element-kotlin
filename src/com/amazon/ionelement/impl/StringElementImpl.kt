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
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.StringElement

internal class StringElementImpl(
    value: String,
    override val annotations: List<String>,
    override val metas: MetaContainer
): TextElementBase(value), StringElement {
    override val type: ElementType get() = ElementType.STRING

    override val stringValue: String get() = textValue

    override fun copy(annotations: List<String>, metas: MetaContainer): StringElement =
        StringElementImpl(textValue, annotations, metas)

    override fun writeContentTo(writer: IonWriter) = writer.writeString(textValue)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringElementImpl

        if (textValue != other.textValue) return false
        if (annotations != other.annotations) return false
        // Note: metas intentionally omitted!

        return true
    }

    override fun hashCode(): Int {
        var result = textValue.hashCode()
        result = 31 * result + annotations.hashCode()
        // Note: metas intentionally omitted!
        return result
    }
}