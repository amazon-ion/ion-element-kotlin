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

@file: JvmName("IonMeta")
package com.amazon.ionelement.api

typealias MetaContainer = Map<String, Any>

private val EMPTY_METAS = HashMap<String, Any>()

fun emptyMetaContainer(): MetaContainer = EMPTY_METAS

inline fun <reified T> MetaContainer.metaOrNull(key: String): T? = this[key] as T
inline fun <reified T> MetaContainer.meta(key: String): T =
    metaOrNull(key) ?: error("Meta with key '$key' and type ${T::class.java} not found in MetaContainer")


fun metaContainerOf(kvps: List<Pair<String, Any>>) =
    metaContainerOf(*kvps.toTypedArray())

fun metaContainerOf(vararg kvps: Pair<String, Any>) =
    when {
        kvps.none() -> EMPTY_METAS
        else -> HashMap(mapOf(*kvps))
    }

/**
 * Merges two meta containers.  Any keys present in the receiver will be replaced by any keys in with the same
 * name in [other].
 */
operator fun MetaContainer.plus(other: MetaContainer): MetaContainer =
    HashMap<String, Any>(this.toList().union(other.toList()).toMap())

/**
 * Merges two meta containers.  Any keys present in the receiver will be replaced by any keys in with the same
 * name in [other].
 */
operator fun MetaContainer.plus(other: Iterable<Pair<String, Any>>): MetaContainer =
    HashMap<String, Any>(this.toList().union(other).toMap())

