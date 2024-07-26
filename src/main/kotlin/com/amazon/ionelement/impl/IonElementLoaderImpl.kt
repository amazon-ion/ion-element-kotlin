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
import com.amazon.ionelement.api.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap

internal class IonElementLoaderImpl(private val options: IonElementLoaderOptions) : IonElementLoader {

    /**
     * Catches an [IonException] occurring in [block] and throws an [IonElementLoaderException] with
     * the current [IonLocation] of the fault, if one is available.  Note that depending on the state of the
     * [IonReader], a location may in fact not be available.
     */
    private inline fun <T> handleReaderException(ionReader: IonReader, crossinline block: () -> T): T {
        try {
            return block()
        } catch (e: IonException) {
            throw IonElementException(
                location = ionReader.currentLocation(),
                description = "IonException occurred, likely due to malformed Ion data (see cause)",
                cause = e
            )
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

    override fun loadSingleElement(ionText: String): AnyElement =
        IonReaderBuilder.standard().build(ionText).use(::loadSingleElement)

    override fun loadSingleElement(ionReader: IonReader): AnyElement {
        return handleReaderException(ionReader) {
            ionReader.next()
            loadCurrentElement(ionReader).also {
                ionReader.next()
                require(ionReader.type == null) { "More than a single value was present in the specified IonReader." }
            }
        }
    }

    override fun loadAllElements(ionReader: IonReader): List<AnyElement> {
        return handleReaderException(ionReader) {
            val elements = mutableListOf<AnyElement>()
            while (ionReader.next() != null) {
                elements.add(loadCurrentElement(ionReader))
            }
            elements
        }
    }

    override fun loadAllElements(ionText: String): List<AnyElement> =
        IonReaderBuilder.standard().build(ionText).use(::loadAllElements)

    override fun loadCurrentElement(ionReader: IonReader): AnyElement {
        return handleReaderException(ionReader) {
            val valueType = requireNotNull(ionReader.type) { "The IonReader was not positioned at an element." }

            val annotations = ionReader.typeAnnotations!!.asList().toEmptyOrPersistentList()

            val metas = when {
                options.includeLocationMeta -> {
                    val location = ionReader.currentLocation()
                    when {
                        location != null -> metaContainerOf(ION_LOCATION_META_TAG to location)
                        else -> emptyMetaContainer()
                    }
                }
                else -> emptyMetaContainer()
            }.toPersistentMap()

            if (ionReader.isNullValue) {
                ionNull(valueType.toElementType(), annotations, metas)
            } else {
                when (valueType) {
                    IonType.BOOL -> BoolElementImpl(ionReader.booleanValue(), annotations, metas)
                    IonType.INT -> when (ionReader.integerSize!!) {
                        IntegerSize.BIG_INTEGER -> {
                            val bigIntValue = ionReader.bigIntegerValue()
                            // Ion java's IonReader appears to determine integerSize based on number of bits,
                            // not on the actual value, which means if we have a padded int that is > 63 bits,
                            // but whose value only uses <= 63 bits then integerSize is still BIG_INTEGER.
                            // Compensate for that here...
                            if (bigIntValue !in RANGE_OF_LONG)
                                BigIntIntElementImpl(bigIntValue, annotations, metas)
                            else {
                                LongIntElementImpl(ionReader.longValue(), annotations, metas)
                            }
                        }
                        IntegerSize.LONG,
                        IntegerSize.INT -> LongIntElementImpl(ionReader.longValue(), annotations, metas)
                    }

                    IonType.FLOAT -> FloatElementImpl(ionReader.doubleValue(), annotations, metas)
                    IonType.DECIMAL -> DecimalElementImpl(ionReader.decimalValue(), annotations, metas)
                    IonType.TIMESTAMP -> TimestampElementImpl(ionReader.timestampValue(), annotations, metas)
                    IonType.STRING -> StringElementImpl(ionReader.stringValue(), annotations, metas)
                    IonType.SYMBOL -> SymbolElementImpl(ionReader.stringValue(), annotations, metas)
                    IonType.CLOB -> ClobElementImpl(ionReader.newBytes(), annotations, metas)
                    IonType.BLOB -> BlobElementImpl(ionReader.newBytes(), annotations, metas)
                    IonType.LIST -> {
                        ionReader.stepIn()
                        val list = ListElementImpl(loadAllElements(ionReader).toEmptyOrPersistentList(), annotations, metas)
                        ionReader.stepOut()
                        list
                    }
                    IonType.SEXP -> {
                        ionReader.stepIn()
                        val sexp = SexpElementImpl(loadAllElements(ionReader).toEmptyOrPersistentList(), annotations, metas)
                        ionReader.stepOut()
                        sexp
                    }
                    IonType.STRUCT -> {
                        val fields = mutableListOf<StructField>()
                        ionReader.stepIn()
                        while (ionReader.next() != null) {
                            fields.add(
                                StructFieldImpl(
                                    ionReader.fieldName,
                                    loadCurrentElement(ionReader)
                                )
                            )
                        }
                        ionReader.stepOut()
                        StructElementImpl(fields.toEmptyOrPersistentList(), annotations, metas)
                    }
                    IonType.DATAGRAM -> error("IonElementLoaderImpl does not know what to do with IonType.DATAGRAM")
                    IonType.NULL -> error("IonType.NULL branch should be unreachable")
                }
            }.asAnyElement()
        }
    }
}
