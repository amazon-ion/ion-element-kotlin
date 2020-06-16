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
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.SeqElement
import com.amazon.ionelement.api.emptyMetaContainer

internal abstract class SeqElementBase(
    override val values: List<AnyElement>,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
): IonElementBase(), SeqElement {

    override val containerValues: Iterable<AnyElement> get() = values
    override val seqValues: Iterable<AnyElement> get() = values

    override val size: Int
        get() = values.size

    override fun writeContentTo(writer: IonWriter) {
        writer.stepIn(type.toIonType())
        values.forEach {
            it.writeTo(writer)
        }
        writer.stepOut()
    }
}


