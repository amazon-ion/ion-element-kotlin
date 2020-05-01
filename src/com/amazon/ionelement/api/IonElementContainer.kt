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

package com.amazon.ionelement.api

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.ionError

/**
 * An immutable Ion list or s-expression.
 *
 * Note that the name of this is not `IonElementList` so as to avoid confusion
 * between an Ion list and a JVM list.
 */
interface IonElementContainer : IonElement, List<IonElement> {

    /**
     * Returns the string representation of the symbol in the first element of this container.
     *
     * If the first element is not a symbol or this container has no elements, throws [IonElectrolyteException],
     */
    val tag get() = this.head.symbolValue

    /**
     * Returns the first element of this container.
     *
     * If this container has no elements, throws [IonElectrolyteException].
     */
    val head: IonElement
        get() =
        when (this.size) {
            0 -> ionError(this, "Cannot get head of empty container")
            else -> this.first()
        }

    /**
     * Returns a sub-list containing all elements of this container except the first.
     *
     * If this container has no elements, throws [IonElectrolyteException].
     */
    val tail: List<IonElement> get()  =
        when (this.size) {
            0 -> ionError(this, "Cannot get tail of empty container")
            else -> this.subList(1, this.size)
        }
}

