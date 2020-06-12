///*
// * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License").
// * You may not use this file except in compliance with the License.
// * A copy of the License is located at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * or in the "license" file accompanying this file. This file is distributed
// * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// * express or implied. See the License for the specific language governing
// *  permissions and limitations under the License.
// */
//
//package com.amazon.ionelement
//
//import com.amazon.ionelement.api.IonElement
//import com.amazon.ionelement.api.ListElement
//import com.amazon.ionelement.api.SeqElement
//import com.amazon.ionelement.api.emptyIonList
//import com.amazon.ionelement.api.emptyIonSexp
//import com.amazon.ionelement.api.ionInt
//import com.amazon.ionelement.api.ionListOf
//import com.amazon.ionelement.api.ionSexpOf
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertFalse
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//
//// TODO:  do these tests make sense anymore?  I think they are defunct now that we don't implement List<T> directly
//
///**
// * Test for [IonElementContainer]'s SEXP and LIST List<T> implementations.
// *
// * TODO:  revise documentation
// * These are perhaps more thorough than they need to be since right now [OrderedIonElementArray] simply delegates
// * to the default Kotlin implementation of [List<>], but at least we will have them if we need to do something
// * different in the future.
// */
//class IonElementContainerTests {
//
//    @Test
//    fun listTest() = checkListImpl(ionListOf(ionInt(1), ionInt(2), ionInt(3), ionInt(3)))
//
//    @Test
//    fun sexpTest() = checkListImpl(ionSexpOf(ionInt(1), ionInt(2), ionInt(3), ionInt(3)))
//
//    @Test
//    fun isEmptyTest() {
//        assertTrue(emptyIonSexp().values.isEmpty())
//        assertTrue(emptyIonList().values.isEmpty())
//        // Non-emptiness is tested in [checkListImpl].
//    }
//
//    @Test
//    fun listListIteratorTest() =
//        checkListIterator(ionListOf(ionInt(1), ionInt(2), ionInt(3), ionInt(4)))
//
//    @Test
//    fun sexpListIteratorTest() =
//        checkListIterator(ionSexpOf(ionInt(1), ionInt(2), ionInt(3), ionInt(4)))
//
//    private fun checkListImpl(element: SeqElement) {
//        // size
//        assertEquals(4, element.values.size)
//
//        // get
//        assertEquals(1L, element.values[0].longValueOrNull)
//        assertEquals(2L, element.values[1].longValueOrNull)
//        assertEquals(3L, element.values[2].longValueOrNull)
//        assertEquals(3L, element.values[3].longValueOrNull)
//        assertThrows<IndexOutOfBoundsException> { element.values[4] }
//
//        // iterator
//        val expectedList = listOf(ionInt(1), ionInt(2), ionInt(3), ionInt(3))
//        val actualList = mutableListOf<IonElement>()
//        element.values.iterator().forEachRemaining { actualList.add(it) }
//        assertEquals(expectedList, actualList)
//
//        // contains
//        assertTrue(element.values.contains(ionInt(1)))
//        assertTrue(element.values.contains(ionInt(2)))
//        assertTrue(element.values.contains(ionInt(3)))
//        assertFalse(element.values.contains(ionInt(4)))
//
//        // containsAll
//        assertTrue(element.values.containsAll(listOf(ionInt(1))))
//        assertTrue(element.values.containsAll(listOf(ionInt(1), ionInt(2))))
//        assertTrue(element.values.containsAll(listOf(ionInt(1), ionInt(2), ionInt(3))))
//        assertFalse(element.values.containsAll(listOf(ionInt(1), ionInt(2), ionInt(3), ionInt(4))))
//
//        // isEmpty
//        assertFalse(element.values.isEmpty())
//
//        // indexOf
//        assertEquals(0, element.values.indexOf(ionInt(1)))
//        assertEquals(1, element.values.indexOf(ionInt(2)))
//        assertEquals(2, element.values.indexOf(ionInt(3)))
//        assertEquals(-1, element.values.indexOf(ionInt(4)))
//
//        // lastIndexOf
//        assertEquals(0, element.values.lastIndexOf(ionInt(1)))
//        assertEquals(1, element.values.lastIndexOf(ionInt(2)))
//        assertEquals(3, element.values.lastIndexOf(ionInt(3)))
//        assertEquals(-1, element.values.lastIndexOf(ionInt(4)))
//
//        // subList
//        val subList = element.values.subList(1, 3)
//        assertEquals(subList, listOf(ionInt(2), ionInt(3)))
//    }
//
//    private fun checkListIterator(element: ListElement) {
//        // listIterator
//        val listIterator = element.listIterator()
//
//        // Position: before fist element.
//
//        assertEquals(-1, listIterator.previousIndex())
//        assertFalse(listIterator.hasPrevious())
//        assertTrue(listIterator.hasNext())
//        assertEquals(0, listIterator.nextIndex())
//
//        // Remaining positions
//        repeat(4) {
//            println("$it")
//            val idx = it + 1
//            assertEquals(ionInt(1L + it), listIterator.next())
//
//            assertEquals(idx > 0, listIterator.hasPrevious())
//            assertEquals(-1 + idx, listIterator.previousIndex())
//
//            assertEquals(idx <= 3, listIterator.hasNext())
//
//            // Oddly, when listIterator.hasNext() == false, listIterator.nextIndex() points to element after last.
//            assertEquals(idx, listIterator.nextIndex())
//        }
//    }
//}