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
import com.amazon.ion.Decimal
import com.amazon.ion.IonType
import com.amazon.ion.IonWriter

internal class DecimalIonElement(
    val value: Decimal,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
) : IonElementBase() {
    override val type get() = IonType.DECIMAL
    override val decimalValueOrNull: Decimal get() = value

    override fun clone(annotations: List<String>, metas: MetaContainer): IonElement =
        DecimalIonElement(value, annotations, metas)

    override fun writeContentTo(writer: IonWriter) = writer.writeDecimal(value)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecimalIonElement

        // `==` considers `0d0` and `-0d0` to be equivalent.  `Decimal.equals` does not.
        if (!Decimal.equals(value, other.value)) return false
        if (annotations != other.annotations) return false
        // Note: metas intentionally omitted!

        return true
    }

    override fun hashCode(): Int {
        var result = value.isNegativeZero.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + annotations.hashCode()
        // Note: metas intentionally omitted!
        return result
    }
}
