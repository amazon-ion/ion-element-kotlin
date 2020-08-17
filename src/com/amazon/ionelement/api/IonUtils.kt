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

@file:JvmName("IonUtils")
package com.amazon.ionelement.api

import com.amazon.ion.IonSystem
import com.amazon.ion.IonValue



/**
 * Bridge function that converts from an immutable [AnyElement] to a mutable [IonValue].
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
 * Bridge function that converts from the mutable [IonValue] to an [AnyElement].
 *
 * New code that does not need to integrate with uses of the mutable DOM should not use this.
 */
fun IonValue.toIonElement(): AnyElement =
    this.system.newReader(this).use { reader->
        createIonElementLoader().loadSingleElement(reader)
    }

/** Throws an [IonElementException], including the [IonLocation] (if available). */
internal fun constraintError(blame: IonElement, description: String): Nothing {
    throw IonElementConstraintException(blame.asAnyElement(), description)
}

