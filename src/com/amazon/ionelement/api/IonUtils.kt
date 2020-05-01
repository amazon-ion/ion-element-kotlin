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

@file:JvmName("IonUtils")
package com.amazon.ionelement.api

import com.amazon.ionelement.impl.IonElementLoaderImpl
import com.amazon.ion.IonSystem
import com.amazon.ion.IonValue
import com.amazon.ionelement.api.IonElectrolyteException

/**
 * Creates an [IonElementLoader] implementation.
 *
 * The default of [includeLocations] is false since there is a non-zero performance penalty associated
 * with creating [MetaContainer] instances populated with [IonLocation] instances.
 */
fun createIonElementLoader(includeLocations: Boolean = false) =
    IonElementLoaderImpl(includeLocations)

/**
 * Bridge function that converts from an immutable [IonElement] to a mutable [IonValue].
 *
 * New code that doesn't need to integrate with existing uses of the mutable DOM should not use this.
 */
fun IonElement.toIonValue(ion: IonSystem): IonValue {
    val datagram = ion.newDatagram()
    ion.newWriter(datagram).use { writer ->
        this.writeTo(writer)
    }
    return datagram.first()
}

/**
 * Bridge function that converts from the mutable [IonValue] to an [IonElement].
 *
 * New code that does not need to integrate with uses of the mutable DOM should not use this.
 */
fun IonValue.toIonElement(): IonElement =
    this.system.newReader(this).use { reader->
        createIonElementLoader().loadSingleElement(reader)
    }

/** Throws an [IonElectrolyteException], including the [IonLocation] (if available). */
fun ionError(blame: IonElement, description: String): Nothing {
    ionError(blame.metas, description)
}

/** Throws an [IonElectrolyteException], including the [IonLocation] (if available). */
fun ionError(metas: MetaContainer, description: String): Nothing {
    throw IonElectrolyteException(metas.location, description)
}
