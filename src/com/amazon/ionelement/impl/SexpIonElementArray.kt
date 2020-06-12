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

import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.SexpElement
import com.amazon.ionelement.api.emptyMetaContainer

internal class SexpIonElementArray (
    values: List<IonElement>,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
):  SeqElementBase(values), SexpElement {
    override val type: ElementType get() = ElementType.SEXP

    override fun clone(annotations: List<String>, metas: MetaContainer): IonElement =
        SexpIonElementArray(values, annotations, metas)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SexpIonElementArray

        if (values != other.values) return false
        if (annotations != other.annotations) return false
        // Note: [metas] intentionally omitted!

        return true
    }

    override fun hashCode(): Int {
        var result = values.hashCode()
        result = 31 * result + annotations.hashCode()
        // Note: [metas] intentionally omitted!
        return result
    }
}