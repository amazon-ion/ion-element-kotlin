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

package com.amazon.ionelement

import com.amazon.ion.IonValue
import com.amazon.ion.system.IonReaderBuilder
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ion.system.IonTextWriterBuilder
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.createIonElementLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.stream.Stream

val TESTS_ROOT_DIR = "./ion-tests/iontestdata"
val GOOD_DIR: Path = Paths.get(TESTS_ROOT_DIR, "good")

class ComplianceTests {
    companion object {
        val loader = createIonElementLoader(true)

        private val SKIP_LIST = listOf(
            // These seems to be known issues in ion-java and fail in [IonReader], before we get to it.
            "good/utf32.ion",
            "good/whitespace.ion",
            "good/utf16.ion",
            "good/symbolZero.ion",
            "good/good/item1.10n",
            "good/symbolExplicitZero.10n",
            "good/symbolImplicitZero.10n",
            "good/item1.10n",
            "good/equivs/clobNewlines.ion",
            "good/typecodes/T7-small.10n",
            "good/typecodes/T7-large.10n"
        ).map { Paths.get(TESTS_ROOT_DIR, it).toString() }
            .toSet()

        @JvmStatic
        @Suppress("unused")
        fun parametersForGoodTests(): Stream<Path> = findTestFiles(GOOD_DIR)

        @JvmStatic
        @Suppress("unused")
        fun parametersForEquivTests(): Stream<Path> = findTestFiles(Paths.get(GOOD_DIR.toString(), "equivs"))

        @JvmStatic
        @Suppress("unused")
        fun parametersForNonEquivTests(): Stream<Path> = findTestFiles(Paths.get(GOOD_DIR.toString(), "non-equivs"))

        private fun findTestFiles(rootDir: Path): Stream<Path> {
            val searchPredicate = BiPredicate<Path, BasicFileAttributes> { path, _ ->
                val pathStr = path.toString()
                (pathStr.endsWith(".ion") || pathStr.endsWith(".10n"))
                && !SKIP_LIST.contains(pathStr)
            }
            return Files.find(rootDir, 100, searchPredicate)
        }

    }

    @ParameterizedTest
    @MethodSource("parametersForGoodTests")
    fun goodTests(testFile: Path) {
        println(testFile)

        // read each value into memory in the mutable dom
        val mutableIonValue = readFileAsIonValues(testFile)

        // read each value in the file as IonElement
        val immutableIonValues = readFileAsIonElements(testFile)

        // write the file to memory in text format
        val ionText = writeIonElementToString(immutableIonValues)

        // parse the in-memory text into the immutable dom
        val mutableIonValue2 = readStringAsIonValues(ionText)

        // assert that the mutable dom instance matches the one read from the text in memory
        assertEquals(mutableIonValue, mutableIonValue2)
    }

    @ParameterizedTest
    @MethodSource("parametersForEquivTests")
    fun equivTests(testFile: Path) {
        println(testFile)
        // read each value in the file as IonElement
        val topLevelValues: List<AnyElement> = readFileAsIonElements(testFile)

        // Every top level value is a list or s-exp value containing values that should be equivalent
        topLevelValues.forEach { equivalenceGroup ->
            if(equivalenceGroup.annotations.contains("embedded_documents")) {
                val documents = equivalenceGroup.asSeqOrNull()?.values?.map {
                    loader.loadAllElements(it.stringValueOrNull ?: error("Unexpected null encountered"))
                } ?: error("Unexpected null encountered")

                documents.forEach { i: List<AnyElement> ->
                    documents.forEach { n: List<AnyElement> ->
                        assertEquals(i, n, "$i and $n must be equivalent")
                        assertEquals(i.hashCode(), n.hashCode(), "Equivalent values must have equal hash codes")
                    }
                }
            } else {
                val seqEquivalenceGroup = equivalenceGroup.asSeqOrNull()?.values ?: error("Unexpected null encountered")
                seqEquivalenceGroup.forEach { i ->
                    seqEquivalenceGroup.forEach { n ->
                        assertEquivalence(i, n)
                    }
                }
            }
        }
    }

    private fun assertEquivalence(i: AnyElement, n: AnyElement) {
        assertEquals(i, n, "$i and $n must be equivalent")
        assertEquals(i.hashCode(), n.hashCode(), "Equivalent values must have equal hash codes")
    }

    @ParameterizedTest
    @MethodSource("parametersForNonEquivTests")
    fun nonEquivTests(testFile: Path) {
        println(testFile)
        // read each value in the file as IonElement
        val immutableIonValues = readFileAsIonElements(testFile)

        immutableIonValues.forEachIndexed { i, iIndex ->
            immutableIonValues.forEachIndexed { n, nIndex ->
                if(iIndex != nIndex) {
                    assertNotEquals(i, n, "$i and $n must *not* be equivalent")
                }
            }
        }
    }

    private fun readFileAsIonElements(path: Path): List<AnyElement> =
        Files.newInputStream(path).use { stream ->
            IonReaderBuilder.standard().build(stream).use { reader ->
                loader.loadAllElements(reader)
            }
        }

    private fun readFileAsIonValues(path: Path): List<IonValue> =
        Files.newInputStream(path).use { stream ->
            readStreamAsIonValues(stream)
        }

    private val ion = IonSystemBuilder.standard().build()

    private fun readStreamAsIonValues(stream: InputStream): List<IonValue> =
        IonReaderBuilder.standard().build(stream).use { reader ->
            reader.next()
            ArrayList<IonValue>().apply {
                while (reader.type != null) {
                    this.add(ion.newValue(reader))
                    reader.next()
                }
            }
        }

    private fun readStringAsIonValues(ionText: String) =
        readStreamAsIonValues(ByteArrayInputStream(ionText.toByteArray()))

    private fun writeIonElementToString(immutableIonValue: List<AnyElement>): String =
        ByteArrayOutputStream().let { stream ->
            IonTextWriterBuilder.standard().build(stream).use { writer ->
                immutableIonValue.forEach { it.writeTo(writer) }
                String(stream.toByteArray())
            }
        }


}