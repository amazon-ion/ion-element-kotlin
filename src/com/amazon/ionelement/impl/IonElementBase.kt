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
import com.amazon.ionelement.api.metaContainerOf
import com.amazon.ion.IonWriter
import com.amazon.ion.system.IonTextWriterBuilder

private val TEXT_WRITER_BUILDER = IonTextWriterBuilder.standard()

internal abstract class IonElementBase: IonElement {

    override val isNull: Boolean get() = false

    protected abstract fun clone(annotations: List<String> = this.annotations, metas: MetaContainer = this.metas): IonElement
    protected abstract fun writeContentTo(writer: IonWriter)

    override fun writeTo(writer: IonWriter) {
        if(this.annotations.any()) {
            writer.setTypeAnnotations(*this.annotations.toTypedArray())
        }
        this.writeContentTo(writer)
    }

    override fun withAnnotations(vararg additionalAnnotations: String): IonElement =
        clone(annotations = this.annotations + additionalAnnotations)

    override fun withoutAnnotations(): IonElement =
        when {
            this.annotations.isNotEmpty() -> clone(annotations = emptyList())
            else -> this
        }

    override fun withMetas(additionalMetas: MetaContainer): IonElement =
        clone(metas = metaContainerOf(metas.toList().union(additionalMetas.toList()).toList()))

    override fun withoutMetas(): IonElement =
        clone(metas = emptyMetaContainer())

    override fun toString() = StringBuilder().also { buf ->
        TEXT_WRITER_BUILDER.build(buf).use { writeTo(it) }
    }.toString()
}

