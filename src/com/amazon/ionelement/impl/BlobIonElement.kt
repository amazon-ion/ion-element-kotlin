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

import com.amazon.ionelement.api.IonByteArray
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.MetaContainer
import com.amazon.ionelement.api.emptyMetaContainer
import com.amazon.ion.IonType
import com.amazon.ion.IonWriter

internal class BlobIonElement(
    override val bytes: ByteArray,
    override val annotations: List<String> = emptyList(),
    override val metas: MetaContainer = emptyMetaContainer()
) : BinaryIonElement() {

    override val blobValueOrNull: IonByteArray? get() = this

    override fun writeContentTo(writer: IonWriter) = writer.writeBlob(bytes)

    override fun clone(annotations: List<String>, metas: MetaContainer): IonElement =
        BlobIonElement(bytes, annotations, metas)

    override val type: IonType get() = IonType.BLOB
}