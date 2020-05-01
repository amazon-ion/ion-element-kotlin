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

import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.field
import com.amazon.ionelement.api.ionBlob
import com.amazon.ionelement.api.ionBool
import com.amazon.ionelement.api.ionClob
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionListOf
import com.amazon.ionelement.api.ionNull
import com.amazon.ionelement.api.ionSexpOf
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionelement.api.ionTimestamp
import com.amazon.ion.IonType
import com.amazon.ion.Timestamp
import java.util.*

val randomSeed = Random().nextLong()
private val random = Random(randomSeed)

private const val MAX_DEPTH = 5
private const val MAX_ELEMENTS = 10
private const val COLLECTION_CHANCE = 0.6
private const val ANNOTATION_CHANCE = 0.1
private const val MAX_ANNOTATIONS = 5
private val MAX_TIMESTAMP_MILLIS = Timestamp.forSecond(1999, 12, 31, 23, 59, 59, null).millis
private const val MAX_RANDOM_STRING_LENGTH = 25
private const val CHARACTERS = "_____abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val MAX_LOB_SIZE = 64

private val ION_TYPES = IonType.values().filter { it != IonType.DATAGRAM }

fun randomIonElement(): IonElement {

    fun innerRandomElement(depth: Int): IonElement {
        val thisDepth = depth + 1
        return when {
            // If we haven't exceeded MAX_DEPTH, we have a COLLECTION_CHANCE% chance of returning a collection.
            thisDepth < MAX_DEPTH && random.nextDouble() < COLLECTION_CHANCE -> {
                val elementCount = random.nextInt(MAX_ELEMENTS + 1)
                when (random.nextInt(3)) {
                    0 -> ionSexpOf((1..elementCount).map { innerRandomElement(thisDepth) })
                    1 -> ionListOf((1..elementCount).map { innerRandomElement(thisDepth) })
                    2 -> ionStructOf((1..elementCount).map { field(randomString(), innerRandomElement(thisDepth)) })
                    else -> error("shouldn't happen")
                }
            }
            // Generate a scalar value
            else -> {
                when(random.nextInt(8)) {
                    0 -> ionNull(ION_TYPES[random.nextInt(ION_TYPES.size)])
                    1 -> ionBool(random.nextBoolean())
                    2 -> ionInt(random.nextLong())
                    3 -> ionSymbol(randomString())
                    4 -> ionString(randomString())
                    // We don't do anything complex with Timestamp here because we largely rely on
                    // com.amazon.ion.Timestamp for equality.
                    5 -> ionTimestamp(Timestamp.forMillis((random.nextLong() + 1) % MAX_TIMESTAMP_MILLIS, null))
                    6 -> ionBlob(randomLob())
                    7 -> ionClob(randomLob())
                    else -> error("shouldn't happen")
                }
            }
        }.let { element ->
            if(random.nextDouble() < ANNOTATION_CHANCE) {
                element.withAnnotations((1..random.nextInt(MAX_ANNOTATIONS)).map { randomString() })
            }
            else {
                element
            }
        }
    }

    return innerRandomElement(0)
}

private fun randomString() = StringBuilder().let { sb ->
    repeat(random.nextInt(MAX_RANDOM_STRING_LENGTH)) {
        sb.append(CHARACTERS[random.nextInt(CHARACTERS.length)])
    }
    sb.toString()
}

private fun randomLob() = ByteArray(random.nextInt(MAX_LOB_SIZE + 1)).also { random.nextBytes(it) }