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

package com.amazon.ionelement.api

/**
 * A field within an Ion struct.
 *
 * This type uses value-based equality—two [StructField]s are equal if their names and values are equal.
 * All implementations of [StructField] MUST use [hashField] to override [Any.hashCode].
 */
public interface StructField {
    public val name: String
    public val value: AnyElement

    public operator fun component1(): String
    public operator fun component2(): AnyElement
}
