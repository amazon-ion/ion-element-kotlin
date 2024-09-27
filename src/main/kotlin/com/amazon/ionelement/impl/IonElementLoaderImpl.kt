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
import com.amazon.ionelement.impl.collections.*
import java.util.ArrayDeque
import java.util.ArrayList
import kotlinx.collections.immutable.adapters.ImmutableListAdapter

internal class IonElementLoaderImpl(private val options: IonElementLoaderOptions) : IonElementLoader {

    companion object {
        private const val DEFAULT_MAX_RECURSION_DEPTH: Int = 100
    }

    private var maxRecursionDepth = if (options.useRecursiveLoad) DEFAULT_MAX_RECURSION_DEPTH else 0

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
        return loadCurrentElementRecursively(ionReader)
    }

    private fun loadCurrentElementRecursively(ionReader: IonReader): AnyElement {
        return handleReaderException(ionReader) {
            val valueType = requireNotNull(ionReader.type) { "The IonReader was not positioned at an element." }

            val annotations = ionReader.typeAnnotations!!.toImmutableListUnsafe()

            var metas = EMPTY_METAS
            if (options.includeLocationMeta) {
                val location = ionReader.currentLocation()
                if (location != null) metas = location.toMetaContainer()
            }

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
                        val listContent = ArrayList<AnyElement>()
                        if (ionReader.depth < maxRecursionDepth) {
                            while (ionReader.next() != null) {
                                listContent.add(loadCurrentElementRecursively(ionReader))
                            }
                        } else {
                            loadAllElementsIteratively(ionReader, listContent as MutableList<Any>)
                        }
                        ionReader.stepOut()
                        ListElementImpl(listContent.toImmutableListUnsafe(), annotations, metas)
                    }
                    IonType.SEXP -> {
                        ionReader.stepIn()
                        val sexpContent = ArrayList<AnyElement>()
                        if (ionReader.depth < maxRecursionDepth) {
                            while (ionReader.next() != null) {
                                sexpContent.add(loadCurrentElementRecursively(ionReader))
                            }
                        } else {
                            loadAllElementsIteratively(ionReader, sexpContent as MutableList<Any>)
                        }
                        ionReader.stepOut()
                        SexpElementImpl(sexpContent.toImmutableListUnsafe(), annotations, metas)
                    }
                    IonType.STRUCT -> {
                        val fields = ArrayList<StructField>()
                        ionReader.stepIn()
                        if (ionReader.depth < maxRecursionDepth) {
                            while (ionReader.next() != null) {
                                val fieldName = ionReader.fieldName
                                val element = loadCurrentElementRecursively(ionReader)
                                fields.add(StructFieldImpl(fieldName, element))
                            }
                        } else {
                            loadAllElementsIteratively(ionReader, fields as MutableList<Any>)
                        }
                        ionReader.stepOut()
                        StructElementImpl(fields.toImmutableListUnsafe(), annotations, metas)
                    }
                    IonType.DATAGRAM -> error("IonElementLoaderImpl does not know what to do with IonType.DATAGRAM")
                    IonType.NULL -> error("IonType.NULL branch should be unreachable")
                }
            }.asAnyElement()
        }
    }

    private fun loadAllElementsIteratively(ionReader: IonReader, into: MutableList<Any>) {
        // Intentionally not using a "recycling" stack because we have mutable lists that we are going to wrap as
        // ImmutableLists and then forget about the reference to the mutable list.
        val openContainerStack = ArrayDeque<MutableList<Any>>()
        var elements: MutableList<Any> = into

        while (true) {
            val valueType = ionReader.next()

            // End of container or input
            if (valueType == null) {
                // We're at the top (relative to where we started)
                if (elements === into) {
                    return
                } else {
                    ionReader.stepOut()
                    elements = openContainerStack.pop()
                    continue
                }
            }

            // Read a value
            val annotations = ionReader.typeAnnotations!!.toImmutableListUnsafe()

            var metas = EMPTY_METAS
            if (options.includeLocationMeta) {
                val location = ionReader.currentLocation()
                if (location != null) metas = location.toMetaContainer()
            }

            if (ionReader.isNullValue) {
                elements.addContainerElement(ionReader, ionNull(valueType.toElementType(), annotations, metas).asAnyElement())
                continue
            } else when (valueType) {
                IonType.BOOL -> elements.addContainerElement(ionReader, BoolElementImpl(ionReader.booleanValue(), annotations, metas))
                IonType.INT -> {
                    val intValue = when (ionReader.integerSize!!) {
                        IntegerSize.BIG_INTEGER -> {
                            val bigIntValue = ionReader.bigIntegerValue()
                            if (bigIntValue !in RANGE_OF_LONG)
                                BigIntIntElementImpl(bigIntValue, annotations, metas)
                            else {
                                LongIntElementImpl(ionReader.longValue(), annotations, metas)
                            }
                        }
                        IntegerSize.LONG,
                        IntegerSize.INT -> LongIntElementImpl(ionReader.longValue(), annotations, metas)
                    }
                    elements.addContainerElement(ionReader, intValue)
                }
                IonType.FLOAT -> elements.addContainerElement(ionReader, FloatElementImpl(ionReader.doubleValue(), annotations, metas))
                IonType.DECIMAL -> elements.addContainerElement(ionReader, DecimalElementImpl(ionReader.decimalValue(), annotations, metas))
                IonType.TIMESTAMP -> elements.addContainerElement(ionReader, TimestampElementImpl(ionReader.timestampValue(), annotations, metas))
                IonType.STRING -> elements.addContainerElement(ionReader, StringElementImpl(ionReader.stringValue(), annotations, metas))
                IonType.SYMBOL -> elements.addContainerElement(ionReader, SymbolElementImpl(ionReader.stringValue(), annotations, metas))
                IonType.CLOB -> elements.addContainerElement(ionReader, ClobElementImpl(ionReader.newBytes(), annotations, metas))
                IonType.BLOB -> elements.addContainerElement(ionReader, BlobElementImpl(ionReader.newBytes(), annotations, metas))
                IonType.LIST -> {
                    val listContent = ArrayList<AnyElement>()
                    // `listContent` gets wrapped in an `ImmutableListWrapper` so that we can create a ListElementImpl
                    // right away. Then, we add child elements to `ListContent`. Technically, this is a violation of the
                    // contract for `ImmutableListAdapter`, but it is safe to do so here because no reads will occur
                    // after we are done modifying the backing list.
                    // Same thing applies for `sexpContent` and `structContent` in their respective branches.
                    elements.addContainerElement(ionReader, ListElementImpl(ImmutableListAdapter(listContent), annotations, metas))
                    ionReader.stepIn()
                    openContainerStack.push(elements)
                    elements = listContent as MutableList<Any>
                }
                IonType.SEXP -> {
                    val sexpContent = ArrayList<AnyElement>()
                    elements.addContainerElement(ionReader, SexpElementImpl(ImmutableListAdapter(sexpContent), annotations, metas))
                    ionReader.stepIn()
                    openContainerStack.push(elements)
                    elements = sexpContent as MutableList<Any>
                }
                IonType.STRUCT -> {
                    val structContent = ArrayList<StructField>()
                    elements.addContainerElement(
                        ionReader,
                        StructElementImpl(
                            ImmutableListAdapter(structContent),
                            annotations,
                            metas
                        )
                    )
                    ionReader.stepIn()
                    openContainerStack.push(elements)
                    elements = structContent as MutableList<Any>
                }
                IonType.DATAGRAM -> error("IonElementLoaderImpl does not know what to do with IonType.DATAGRAM")
                IonType.NULL -> error("IonType.NULL branch should be unreachable")
            }
        }
    }

    private fun MutableList<Any>.addContainerElement(ionReader: IonReader, value: AnyElement) {
        val fieldName = ionReader.fieldName
        if (fieldName != null) {
            add(StructFieldImpl(fieldName, value))
        } else {
            add(value)
        }
    }
}
