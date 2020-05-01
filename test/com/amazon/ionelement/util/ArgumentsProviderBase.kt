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

package com.amazon.ionelement.util

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

/**
 * Reduces some of the boilerplate associated with the style of parameterized testing frequently
 * utilized in this package.
 *
 * Since JUnit5 requires `@JvmStatic` on its `@MethodSource` argument factory methods, this requires all
 * of the argument lists to reside in the companion object of a test class.  This can be annoying since it
 * forces the test to be separated from its tests cases.
 *
 * Classes that derive from this class can be defined near the `@ParameterizedTest` functions instead.
 */
abstract class ArgumentsProviderBase : ArgumentsProvider {

    abstract fun getParameters(): List<Any>

    @Throws(Exception::class)
    override fun provideArguments(extensionContext: ExtensionContext): Stream<out Arguments>? {
        return getParameters().map { Arguments.of(it) }.stream()
    }

}

