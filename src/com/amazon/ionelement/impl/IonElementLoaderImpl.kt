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

package com.amazon.ionelement.impl

import com.amazon.ion.IntegerSize
import com.amazon.ion.IonException
import com.amazon.ion.IonReader
import com.amazon.ion.IonType
import com.amazon.ion.OffsetSpan
import com.amazon.ion.SpanProvider
import com.amazon.ion.TextSpan
import com.amazon.ion.system.IonReaderBuilder
import com.amazon.ionelement.api.ION_LOCATION_META_TAG
import com.amazon.ionelement.api.IonBinaryLocation
import com.amazon.ionelement.api.IonElectrolyteException
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.IonElementLoader
import com.amazon.ionelement.api.IonLocation
import com.amazon.ionelement.api.IonStructField
import com.amazon.ionelement.api.IonTextLocation
import com.amazon.ionelement.api.emptyMetaContainer
import com.amazon.ionelement.api.ionBlob
import com.amazon.ionelement.api.ionBool
import com.amazon.ionelement.api.ionClob
import com.amazon.ionelement.api.ionDecimal
import com.amazon.ionelement.api.ionFloat
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionListOf
import com.amazon.ionelement.api.ionNull
import com.amazon.ionelement.api.ionSexpOf
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionelement.api.ionTimestamp
import com.amazon.ionelement.api.metaContainerOf
import com.amazon.ionelement.api.toElementType
import com.amazon.ionelement.api.withAnnotations
import com.amazon.ionelement.api.withMetas

class IonElementLoaderImpl(private val includeLocations: Boolean) : IonElementLoader {
    private inline fun <T> handleReaderException(ionReader: IonReader, crossinline block: () -> T): T {
        try {
            return block()
        } catch(e: IonException) {
            throw IonElectrolyteException(
                location = ionReader.currentLocation(),
                description = "IonException occurred, likely due to malformed Ion data (see cause)",
                cause = e)
        }
    }

    private fun IonReader.currentLocation(): IonLocation? =
        when {
            // Can't attempt to get a SpanProvider unless we're on a value
            this.type == null -> null
            else -> {
                val spanFacet = this.asFacet(SpanProvider::class.java)
                when (val currentSpan = spanFacet.currentSpan()) {
                    is TextSpan -> IonTextLocation(currentSpan.startLine, currentSpan.startColumn)
                    is OffsetSpan -> IonBinaryLocation(currentSpan.startOffset)
                    else -> null
                }
            }
        }

    override fun loadSingleElement(ionText: String): IonElement =
        IonReaderBuilder.standard().build(ionText).use { ionReader ->
            loadSingleElement(ionReader)
        }

    override fun loadSingleElement(ionReader: IonReader): IonElement {
        return handleReaderException(ionReader) {
            ionReader.next()
            loadCurrentElement(ionReader).also {
                ionReader.next()
                require(ionReader.type == null) { "More than a single value was present in the specified IonReader." }
            }
        }
    }

    override fun loadAllElements(ionReader: IonReader): List<IonElement> {
        return handleReaderException(ionReader) {
            mutableListOf<IonElement>().also { fields ->
                ionReader.forEachValue { fields.add(loadCurrentElement(ionReader)) }
            }
        }
    }

    override fun loadAllElements(ionText: String): List<IonElement> =
        IonReaderBuilder.standard().build(ionText).use { ionReader ->
            return ArrayList<IonElement>().also { list ->
                ionReader.forEachValue {
                    list.add(loadCurrentElement(ionReader))
                }
            }.toList()
        }

    override fun loadCurrentElement(ionReader: IonReader): IonElement {
        return handleReaderException(ionReader) {
            require(ionReader.type != null) { "The IonReader was not positioned at an element." }

            val valueType = ionReader.type

            val annotations = ionReader.typeAnnotations!!

            val metas = when {
                includeLocations -> {
                    val location = ionReader.currentLocation()
                    when {
                        location != null -> metaContainerOf(ION_LOCATION_META_TAG to location)
                        else -> emptyMetaContainer()
                    }
                }
                else -> emptyMetaContainer()
            }

            var element: IonElement = when {
                ionReader.type == IonType.DATAGRAM -> error("IonElementLoaderImpl does not know what to do with IonType.DATAGRAM")
                ionReader.isNullValue -> ionNull(valueType.toElementType())
                else -> {
                    when {
                        !IonType.isContainer(valueType) -> {
                            when (valueType) {
                                IonType.BOOL -> ionBool(ionReader.booleanValue())
                                IonType.INT -> when (ionReader.integerSize!!) {
                                    IntegerSize.BIG_INTEGER -> {
                                        val bigIntValue = ionReader.bigIntegerValue()
                                        // Ion java's IonReader appears to determine integerSize based on number of bits,
                                        // not on the actual value, which means if we have a padded int that is > 63 bits,
                                        // but who's value only uses <= 63 bits then integerSize is still BIG_INTEGER.
                                        // Compensate for that here...
                                        if (bigIntValue > MAX_LONG_AS_BIG_INT || bigIntValue < MIN_LONG_AS_BIG_INT)
                                            ionInt(bigIntValue)
                                        else {
                                            ionInt(ionReader.longValue())
                                        }
                                    }
                                    IntegerSize.LONG, IntegerSize.INT -> ionInt(ionReader.longValue())
                                }
                                IonType.FLOAT -> ionFloat(ionReader.doubleValue())
                                IonType.DECIMAL -> ionDecimal(ionReader.decimalValue())
                                IonType.TIMESTAMP -> ionTimestamp(ionReader.timestampValue())
                                IonType.STRING -> ionString(ionReader.stringValue())
                                IonType.SYMBOL -> ionSymbol(ionReader.stringValue())
                                IonType.CLOB -> ionClob(ionReader.newBytes())
                                IonType.BLOB -> ionBlob(ionReader.newBytes())
                                else ->
                                    error("Unexpected Ion type for scalar Ion data type ${ionReader.type}.")
                            }
                        }
                        else -> {
                            ionReader.stepIn()
                            when (valueType) {
                                IonType.LIST -> {
                                    ionListOf(loadAllElements(ionReader))
                                }
                                IonType.SEXP -> {
                                    ionSexpOf(loadAllElements(ionReader))
                                }
                                IonType.STRUCT -> {
                                    val fields = mutableListOf<IonStructField>()
                                    ionReader.forEachValue { fields.add(IonStructFieldImpl(ionReader.fieldName, loadCurrentElement(ionReader))) }
                                    ionStructOf(fields)
                                }
                                else -> error("Unexpected Ion type for container Ion data type ${ionReader.type}.")
                            }.also {
                                ionReader.stepOut()
                            }
                        }
                    }
                }
            }.asIonElement()

            if (annotations.any()) {
                element = element.withAnnotations(*annotations)
            }
            if (metas.any()) {
                element = element.withMetas(metas)
            }

            element
        }
    }
}

/**
 * Calls [IonReader.next] and invokes [block] until all values at the current level in the [IonReader]
 * have been exhausted.
 * */
private fun <T> IonReader.forEachValue(block: () -> T): Unit {
    while (this.next() != null) {
        block()
    }
}