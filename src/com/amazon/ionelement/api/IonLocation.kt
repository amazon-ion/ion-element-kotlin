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

val ION_LOCATION_META_TAG = "\$ion_location"

sealed class IonLocation

data class IonTextLocation(val line: Long, val charOffset: Long) : IonLocation() {
    override fun toString(): String = "$line:$charOffset"
}

data class IonBinaryLocation(val byteOffset: Long): IonLocation() {
    override fun toString(): String = byteOffset.toString()
}

fun locationToString(loc: IonLocation?) = loc?.toString() ?: "<unknown location>"

val MetaContainer.location: IonLocation?
    get() = this[ION_LOCATION_META_TAG] as? IonLocation
