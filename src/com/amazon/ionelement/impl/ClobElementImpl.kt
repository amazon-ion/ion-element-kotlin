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
import com.amazon.ionelement.api.ByteArrayView
import com.amazon.ionelement.api.ClobElement
import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.MetaContainer

import com.amazon.ionelement.api.PersistentMetaContainer
import com.amazon.ionelement.api.emptyMetaContainer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

internal class ClobElementImpl(
    bytes: ByteArray,
    override val annotations: PersistentList<String>,
    override val metas: PersistentMetaContainer
) : LobElementBase(bytes), ClobElement {

    override val clobValue: ByteArrayView get() = bytesValue

    override fun writeContentTo(writer: IonWriter) = writer.writeClob(bytes)
    override fun copy(annotations: List<String>, metas: MetaContainer): ClobElement =
        ClobElementImpl(bytes, annotations.toPersistentList(), metas.toPersistentMap())

    override val type: ElementType get() = ElementType.CLOB
}
