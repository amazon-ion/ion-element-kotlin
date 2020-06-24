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

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.createIonElementLoader
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.withAnnotations
import com.amazon.ionelement.api.withMeta
import org.junit.jupiter.api.Assertions

data class EquivTestCase(val left: String, val right: String, val isEquiv: Boolean) {
    fun checkEquivalence() {
        val loader = createIonElementLoader()

        // Note: metas are not relevant to equivalence tests.
        val leftElement = loader.loadSingleElement(left).withMeta("leftMeta", 1)
        val rightElement = loader.loadSingleElement(right).withMeta("rightMeta", 2)

        // It seems unlikely that we should ever return zero--if we do it probably is a bug.
        // (this may need to be revisited in the future)
        Assertions.assertNotEquals(0, leftElement.hashCode())
        Assertions.assertNotEquals(0, rightElement.hashCode())

        // Equivalence should be reflexive
        checkEquivalence(true, leftElement, leftElement)
        checkEquivalence(true, rightElement, rightElement)

        // Equivalence should be symmetric
        checkEquivalence(isEquiv, leftElement, rightElement)

        // Check reflexivity again, this time bypassing the reference equality check that happens first .equals calls
        checkEquivalence(true, leftElement, loader.loadSingleElement(left))
        checkEquivalence(true, rightElement, loader.loadSingleElement(right))

        // Adding annotations should not change the result
        val leftElementWithAnnotation = leftElement.withAnnotations("some_annotation")
        val rightElementWithAnnotation = rightElement.withAnnotations("some_annotation")
        checkEquivalence(
            isEquiv,
            leftElementWithAnnotation,
            rightElementWithAnnotation)

        // Adding an annotation to only one side will force them to be not equivalent
        checkEquivalence(false, leftElement.withAnnotations("some_annotation"), rightElement)

        // Adding metas has no effect
        checkEquivalence(isEquiv, leftElement.withMeta("foo", 1), rightElement)

        // Nesting the values within a struct should not change the result
        fun nest(ie: AnyElement) = ionStructOf("nested" to ie)
        checkEquivalence(isEquiv, nest(leftElement), nest(rightElement))
    }

    private fun checkEquivalence(equiv: Boolean, leftElement: IonElement, rightElement: IonElement) {
        fun checkIt(first: IonElement, second: IonElement) {
            if (equiv) {
                Assertions.assertEquals(first, second, "Elements should be equivalent")
                Assertions.assertEquals(first.hashCode(), second.hashCode(), "Elements should not be equivalent")
            } else {
                Assertions.assertNotEquals(first, second, "Elements should not be equal")
                // Note that two different [IonElement]s *can* have the same hash code and this might one day
                // break the build and may necessitate removing the assertion below. However, if it does happen we
                // should evaluate if the hashing algorithm is sufficient or not since that seems unlikely
                Assertions.assertNotEquals(first.hashCode(), second.hashCode(), "Elements' hash codes should not be equal")
            }
        }

        // Equivalence is symmetric
        checkIt(leftElement, rightElement)
        checkIt(rightElement, leftElement)

        // Adding metas to one side should not have any effect
        checkIt(leftElement, rightElement.withMeta("foo", 123))
        checkIt(rightElement, leftElement.withMeta("bar", 456))
    }
}