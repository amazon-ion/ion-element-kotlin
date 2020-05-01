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

package com.amazon.ionelement

import com.amazon.ionelement.api.emptyBlob
import com.amazon.ionelement.api.emptyClob
import com.amazon.ionelement.api.emptyIonList
import com.amazon.ionelement.api.emptyIonSexp
import com.amazon.ionelement.api.emptyIonStruct
import com.amazon.ionelement.api.ionBool
import com.amazon.ionelement.api.ionDecimal
import com.amazon.ionelement.api.ionFloat
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionNull
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionelement.api.ionTimestamp
import com.amazon.ionelement.util.ArgumentsProviderBase
import com.amazon.ionelement.util.randomIonElement
import com.amazon.ionelement.util.randomSeed
import com.amazon.ion.Decimal
import com.amazon.ion.IonType
import com.amazon.ion.Timestamp
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource


private val ALL_NULLS = IonType.values()
    .filter { it != IonType.DATAGRAM }
    .map { ionNull(it) }

/** Expands a [EquivTestCase] by adding variations that include type annotations. */
private fun List<EquivTestCase>.includeAnnotations(): List<EquivTestCase> {
    val sameAnnotationBothSides = this.map {
        // Maintains equivalence requirement
        EquivTestCase("foo::${it.left}", "foo::${it.right}", isEquiv = it.isEquiv)
    }

    val differentAnnotationEachSide = this.map {
        // Forces non-equivalent values
        EquivTestCase("foo::${it.left}", "bar::${it.right}", isEquiv = false)
    }

    val annotationAddedOnlyToOneSide = this.map {
        // Forces non-equivalent values
        EquivTestCase("foo::${it.left}", it.right, isEquiv = false)
    }

    return this + sameAnnotationBothSides + differentAnnotationEachSide + annotationAddedOnlyToOneSide
}


/**
 * The goal of this test class is to validate the result of `equals` and `hashCode` for all implementations
 * [IonElement].  Note that we do not include different textual representations of the same value (e.g. `null` and
 * `null.int` or `0.0` and `0d0`) since these have the same in-memory representation and therefore testing them would
 * be redundant.
 */
class EquivalenceTests {

    @Test
    fun randomEquivalenceTests() {
        // This is not parameterized like the other tests because it makes little sense to have 10k individual tests
        // appear in the IDE.
        repeat(10000) {
            val ionText = randomIonElement().toString()
            try {
                EquivTestCase(ionText, ionText, isEquiv = true).checkEquivalence()
            } catch (ex: Throwable) {
                // We catch `Throwable` here so that *any* exception will result in a test failure that
                // includes the randomSeed in the message.  This may be needed to reproduce the error condition during
                // debugging. Note that we do not send the random seed to stdout or stderr because it is common to hide
                // them during CI builds such as gradle, therefore it is important that the seed be included in the
                // failure message.
                fail("Test failed. See cause. Seed was $randomSeed, ionText was $ionText", ex)
            }
        }
    }

    // Verifies that every null value is not equivalent to a non-null value of every type.
    @ParameterizedTest
    @ArgumentsSource(NullAndNotNullEquivalenceTests::class)
    fun nullAndNotNullEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class NullAndNotNullEquivalenceTests : ArgumentsProviderBase() {

        private val nonNullValues = listOf(
            ionBool(true),
            ionInt(1),
            ionFloat(1.0),
            ionDecimal(Decimal.ZERO),
            ionString("foo"),
            ionSymbol("foo"),
            ionTimestamp(Timestamp.valueOf("2001T")),
            emptyBlob(),
            emptyClob(),
            emptyIonList(),
            emptyIonSexp(),
            emptyIonStruct()
        )

        override fun getParameters(): List<Any> = nonNullValues
            .map { nonNullValue ->
                ALL_NULLS.map { nullValue ->
                    EquivTestCase(
                        left = nullValue.toString(),
                        right = nonNullValue.toString(),
                        isEquiv = false)
                }
            }.flatten()
            .includeAnnotations()
    }

    // Verifies that every null value is not equivalent to any other null value
    // except another null value of the same type.
    @ParameterizedTest
    @ArgumentsSource(NullAndNullEquivalenceTests::class)
    fun nullAndNullEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class NullAndNullEquivalenceTests : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = ALL_NULLS
            .map { left ->
                ALL_NULLS.map { right ->
                    EquivTestCase(
                        left = left.toString(),
                        right = right.toString(),
                        isEquiv = left.type == right.type)
                }
            }.flatten()
            .includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(BoolEquivalenceTestsArgumentsProvider::class)
    fun boolEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class BoolEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "true",
                "true",
                isEquiv = true),
            EquivTestCase(
                "false",
                "false",
                isEquiv = true),
            EquivTestCase(
                "true",
                "false",
                isEquiv = false)
            ).includeAnnotations()
    }
    @ParameterizedTest
    @ArgumentsSource(IntEquivalenceTestsArgumentsProvider::class)
    fun intEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class IntEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "0",
                "0",
                isEquiv = true),
            EquivTestCase(
                "1",
                "2",
                isEquiv = false)
        ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(FloatEquivalenceTestsArgumentsProvider::class)
    fun floatEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class FloatEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            // floats
            EquivTestCase(
                "0.12e4",
                "0.12e4",
                isEquiv = true),
            EquivTestCase(
                "0.12e4",
                "0.23e4",
                isEquiv = false),
            EquivTestCase(
                "0e0",
                "0e0",
                isEquiv = true),
            EquivTestCase(
                "0e0",
                "-0e0",
                isEquiv = false),
            EquivTestCase(
                "nan",
                "nan",
                isEquiv = true),
            EquivTestCase(
                "+inf",
                "+inf",
                isEquiv = true),
            EquivTestCase(
                "-inf",
                "-inf",
                isEquiv = true),
            EquivTestCase(
                "nan",
                "+inf",
                isEquiv = false),
            EquivTestCase(
                "nan",
                "-inf",
                isEquiv = false),
            EquivTestCase(
                "+inf",
                "-inf",
                isEquiv = false)
            ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(DecimalEquivalenceTestsArgumentsProvider::class)
    fun decimalEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class DecimalEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "0.0",
                "0.0",
                isEquiv = true),
            EquivTestCase(
                "1.0",
                "1.0",
                isEquiv = true),
            EquivTestCase(
                "0.0",
                "-0.0",
                isEquiv = false),
            EquivTestCase(
                "1.0",
                "1.00",
                isEquiv = false),
            EquivTestCase(
                "0.0",
                "0.00",
                isEquiv = false)
            ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(StringEquivalenceTestsArgumentsProvider::class)
    fun stringEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class StringEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "\"some string\"",
                "\"some string\"",
                isEquiv = true),
            EquivTestCase(
                "\"some string\"",
                "\"another string\"",
                isEquiv = false)
            ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(SymbolEquivalenceTestsArgumentsProvider::class)
    fun symbolEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class SymbolEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "'some symbol'",
                "'some symbol'",
                isEquiv = true),
            EquivTestCase(
                "'some symbol'",
                "'another symbol'",
                isEquiv = false)
        ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(BlobEquivalenceTestsArgumentsProvider::class)
    fun blobEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class BlobEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "{{}}",
                "{{}}",
                isEquiv = true),
            EquivTestCase(
                "{{}}",
                "{{ VG8gaW5maW5pdHkuLi4gYW5kIGJleW9uZCE= }}",
                isEquiv = false),
            EquivTestCase(
                "{{ VG8gaW5maW5pdHkuLi4gYW5kIGJleW9uZCE= }}",
                "{{ VG8gaW5maW5pdHkuLi4gYW5kIGJleW9uZCE= }}",
                isEquiv = true),
            EquivTestCase(
                "{{ VG8gaW5maW5pdHkuLi4gYW5kIGJleW9uZCE= }}",
                "{{ TWFrZSBpdCBzbyE= }}",
                isEquiv = false)
        ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(ClobEquivalenceTestsArgumentsProvider::class)
    fun clobEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class ClobEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "{{ \"\" }}",
                "{{ \"\" }}" ,
                isEquiv = true),
            EquivTestCase(
                "{{ \"\" }}",
                "{{ \"A non-empty CLOB.\" }}" ,
                isEquiv = false),
            EquivTestCase(
                "{{ \"This is a CLOB of text.\" }}",
                "{{ \"This is a CLOB of text.\" }}" ,
                isEquiv = true),
            EquivTestCase(
                "{{ \"This is a CLOB of text.\" }}",
                "{{ \"This is a another CLOB of text.\" }}" ,
                isEquiv = false)
        ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(TimestampEquivalenceArgumentsProvider::class)
    fun timestampEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    // we mainly rely on ion-java for timestamp equivalence so this isn't going to be super extensive
    class TimestampEquivalenceArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "2001T",
                "2001T",
                isEquiv = true),
            EquivTestCase(
                "2001T",
                "3001T",
                isEquiv = false),
            EquivTestCase(
                "2001T",
                "2001-01T",
                isEquiv = false),
            EquivTestCase(
                "2001-01T",
                "2001-01T",
                isEquiv = true),
            EquivTestCase(
                "2001-01T",
                "2001-02T",
                isEquiv = false),
            EquivTestCase(
                "2001-01-01T23:59:59Z",
                "2001-01-01T23:59:59Z",
                isEquiv = true),
            EquivTestCase(
                "2001-01-01T23:59:58Z",
                "2001-01-01T23:59:59Z",
                isEquiv = false),
            EquivTestCase(
                // These values do represent the same instant in time but they are not equivalent since
                // the offset is specified differently.
                "2001-01-01T10:00:00-08:00",
                "2001-01-01T18:00:00Z",
                isEquiv = false)
        ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(StructEquivalenceTestsArgumentsProvider::class)
    fun structEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class StructEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "{}",
                "{}",
                isEquiv = true),
            EquivTestCase(
                "{ a: 1 }",
                "{ a: 1 }",
                isEquiv = true),
            EquivTestCase(
                "{ a: 1 }",
                "{ a: 2 }",
                isEquiv = false),
            EquivTestCase(
                "{ a: 1, a: 1 }",
                "{ a: 1, a: 1 }",
                isEquiv = true),
            EquivTestCase(
                "{ a: 1 }",
                "{ a: 1, a: 1 }",
                isEquiv = false),
            EquivTestCase(
                "{ a: 1, a: 1 }",
                "{ a: 1, a: 2 }",
                isEquiv = false),
            EquivTestCase(
                "{ a: 1, a: 1 }",
                "{ a: 2, a: 1 }",
                isEquiv = false),
            EquivTestCase(
                "{ a: 1, a: 2 }",
                "{ a: 1, a: 2 }",
                isEquiv = true),
            EquivTestCase(
                "{ a: 1, a: 2 }",
                "{ a: 2, a: 1 }",
                isEquiv = true),
            EquivTestCase(
                "{ a: 1, b: 2 }",
                "{ b: 2, a: 1 }",
                isEquiv = true),
            EquivTestCase(
                "{ a: 1, b: 2, b: 3 }",
                "{ b: 3, b: 2, a: 1 }",
                isEquiv = true)
        ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(ListsEquivalenceArgumentsProvider::class)
    fun listAndStructsEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class ListsEquivalenceArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "[]",
                "[]",
                isEquiv = true),
            EquivTestCase(
                "[1]",
                "[1,1]",
                isEquiv = false),
            EquivTestCase(
                "[1]",
                "[2]",
                isEquiv = false),
            EquivTestCase(
                "[1]",
                "[1]",
                isEquiv = true),
            EquivTestCase(
                "[1,1]",
                "[1,1]",
                isEquiv = true),
            EquivTestCase(
                "[1,2]",
                "[2,1]",
                isEquiv = false),
            EquivTestCase(
                "[(1)]",
                "[(1)]",
                isEquiv = true)
        ).includeAnnotations()
    }

    @ParameterizedTest
    @ArgumentsSource(SexpEquivalenceTestsArgumentsProvider::class)
    fun sexpEquivalenceTests(tc: EquivTestCase) = tc.checkEquivalence()
    class SexpEquivalenceTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            EquivTestCase(
                "()",
                "()",
                isEquiv = true),
            EquivTestCase(
                "(1)",
                "(1 1)",
                isEquiv = false),
            EquivTestCase(
                "(1)",
                "(2)",
                isEquiv = false),
            EquivTestCase(
                "(1)",
                "(1)",
                isEquiv = true),
            EquivTestCase(
                "(1 1)",
                "(1 1)" ,
                isEquiv = true),
            EquivTestCase(
                "(1 2)",
                "(2 1)" ,
                isEquiv = false),
            EquivTestCase(
                "([1])",
                "([1])" ,
                isEquiv = true)
        ).includeAnnotations()
    }

}

