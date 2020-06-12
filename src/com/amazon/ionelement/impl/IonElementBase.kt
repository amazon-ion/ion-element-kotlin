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

import com.amazon.ion.Decimal
import com.amazon.ion.IntegerSize
import com.amazon.ion.IonWriter
import com.amazon.ion.Timestamp
import com.amazon.ion.system.IonTextWriterBuilder
import com.amazon.ionelement.api.BlobElement
import com.amazon.ionelement.api.BoolElement
import com.amazon.ionelement.api.ClobElement
import com.amazon.ionelement.api.ContainerElement
import com.amazon.ionelement.api.DecimalElement
import com.amazon.ionelement.api.Element
import com.amazon.ionelement.api.ElementType
import com.amazon.ionelement.api.ElementType.BLOB
import com.amazon.ionelement.api.ElementType.BOOL
import com.amazon.ionelement.api.ElementType.CLOB
import com.amazon.ionelement.api.ElementType.DECIMAL
import com.amazon.ionelement.api.ElementType.FLOAT
import com.amazon.ionelement.api.ElementType.INT
import com.amazon.ionelement.api.ElementType.LIST
import com.amazon.ionelement.api.ElementType.NULL
import com.amazon.ionelement.api.ElementType.SEXP
import com.amazon.ionelement.api.ElementType.STRING
import com.amazon.ionelement.api.ElementType.STRUCT
import com.amazon.ionelement.api.ElementType.SYMBOL
import com.amazon.ionelement.api.ElementType.TIMESTAMP
import com.amazon.ionelement.api.FloatElement
import com.amazon.ionelement.api.IntElement
import com.amazon.ionelement.api.IonByteArray
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.ListElement
import com.amazon.ionelement.api.LobElement
import com.amazon.ionelement.api.SeqElement
import com.amazon.ionelement.api.SexpElement
import com.amazon.ionelement.api.StringElement
import com.amazon.ionelement.api.StructElement
import com.amazon.ionelement.api.SymbolElement
import com.amazon.ionelement.api.TextElement
import com.amazon.ionelement.api.TimestampElement
import com.amazon.ionelement.api.ionError
import java.math.BigInteger

private val TEXT_WRITER_BUILDER = IonTextWriterBuilder.standard()

/**
 * TODO: explain how to implement this class.
 */
internal abstract class IonElementBase : IonElement {

    override fun asIonElement(): IonElement = this

    override val isNull: Boolean get() = false
    protected abstract fun writeContentTo(writer: IonWriter)

    override fun writeTo(writer: IonWriter) {
        if(this.annotations.any()) {
            writer.setTypeAnnotations(*this.annotations.toTypedArray())
        }
        this.writeContentTo(writer)
    }

    override fun toString() = StringBuilder().also { buf ->
        TEXT_WRITER_BUILDER.build(buf).use { writeTo(it) }
    }.toString()

    override val integerSize: IntegerSize get() = ionError(this, "integerSize not valid for this Element")

    private inline fun <reified T: Element> requireTypeAndCastOrNull(allowedType: ElementType): T? {
        if(this.type == NULL) {
            return null
        }

        if(this.type != allowedType)
            expectedType(allowedType)

        return when {
            // this could still be a typed null
            this.isNull -> null
            else -> this as T
        }
    }

    private fun expectedType(allowedType: ElementType): Nothing {
        ionError(this, "Expected an element of type $allowedType but found an element of type ${this.type}")
    }

    private fun expectedType(allowedType: ElementType, allowedType2: ElementType): Nothing  {
        ionError(this, "Expected an element of type $allowedType or $allowedType2 but found an element of type ${this.type}")
    }

    private inline fun <reified T: Element> requireTypeAndCastOrNull(allowedType: ElementType, allowedType2: ElementType): T? {
        if(this.type == NULL) {
            return null
        }

        if(this.type != allowedType && this.type != allowedType2)
            expectedType(allowedType, allowedType2)

        return when {
            // this could still be a typed null
            this.isNull -> null
            else -> this as T
        }
    }

    private inline fun <reified T: Element> requireTypeAndCastOrNull(allowedType: ElementType, allowedType2: ElementType, allowedType3: ElementType): T? {
        if(this.type == NULL) {
            return null
        }

        if(this.type != allowedType && this.type != allowedType2 && this.type != allowedType3)
            ionError(this, "Expected an element of type $allowedType, $allowedType2 or $allowedType3 but found an element of type ${this.type}")

        return when {
            // this could still be a typed null
            this.isNull -> null
            else -> this as T
        }
    }

    private inline fun <reified T: Element> requireTypeAndCast(allowedType: ElementType): T {
        requireTypeAndCastOrNull<T>(allowedType) ?: ionError(this, "Required non-null value of type $allowedType but found a $this")
        return this as T
    }

    private inline fun <reified T: Element> requireTypeAndCast(allowedType: ElementType, allowedType2: ElementType): T {
        requireTypeAndCastOrNull<T>(allowedType, allowedType2) ?: ionError(this, "Required non-null value of type $allowedType or $allowedType2 but found a $this")
        return this as T
    }

    private inline fun <reified T: Element> requireTypeAndCast(allowedType: ElementType, allowedType2: ElementType, allowedType3: ElementType): T {
        requireTypeAndCastOrNull<T>(allowedType, allowedType2, allowedType3) ?: ionError(this, "Required non-null value of type $allowedType, $allowedType2 or $allowedType3 but found a $this")
        return this as T
    }

    // The default as*() functions do not need to be overridden by child classes.
    final override fun asBoolean(): BoolElement = requireTypeAndCast(BOOL)
    final override fun asBooleanOrNull(): BoolElement? = requireTypeAndCastOrNull(BOOL)
    final override fun asInt(): IntElement = requireTypeAndCast(INT)
    final override fun asIntOrNull(): IntElement? = requireTypeAndCastOrNull(INT)
    final override fun asDecimal(): DecimalElement = requireTypeAndCast(DECIMAL)
    final override fun asDecimalOrNull(): DecimalElement? = requireTypeAndCastOrNull(DECIMAL)
    final override fun asDouble(): FloatElement = requireTypeAndCast(FLOAT)
    final override fun asDoubleOrNull(): FloatElement? = requireTypeAndCastOrNull(FLOAT)
    final override fun asText(): TextElement = requireTypeAndCast(STRING, SYMBOL)
    final override fun asTextOrNull(): TextElement? = requireTypeAndCastOrNull(STRING, SYMBOL)
    final override fun asString(): StringElement = requireTypeAndCast(STRING)
    final override fun asStringOrNull(): StringElement? = requireTypeAndCastOrNull(STRING)
    final override fun asSymbol(): SymbolElement = requireTypeAndCast(SYMBOL)
    final override fun asSymbolOrNull(): SymbolElement? = requireTypeAndCastOrNull(SYMBOL)
    final override fun asTimestamp(): TimestampElement = requireTypeAndCast(TIMESTAMP)
    final override fun asTimestampOrNull(): TimestampElement? = requireTypeAndCastOrNull(TIMESTAMP)
    final override fun asLob(): LobElement  = requireTypeAndCast(BLOB, CLOB)
    final override fun asLobOrNull(): LobElement? = requireTypeAndCastOrNull(BLOB, CLOB)
    final override fun asBlob(): BlobElement = requireTypeAndCast(BLOB)
    final override fun asBlobOrNull(): BlobElement? = requireTypeAndCastOrNull(BLOB)
    final override fun asClob(): ClobElement = requireTypeAndCast(CLOB)
    final override fun asClobOrNull(): ClobElement? = requireTypeAndCastOrNull(CLOB)
    final override fun asContainer(): ContainerElement = requireTypeAndCast(LIST, STRUCT, SEXP)
    final override fun asContainerOrNull(): ContainerElement? = requireTypeAndCastOrNull(LIST, STRUCT, SEXP)
    final override fun asSeq(): SeqElement = requireTypeAndCast(LIST, SEXP)
    final override fun asSeqOrNull(): SeqElement? = requireTypeAndCastOrNull(LIST, SEXP)
    final override fun asList(): ListElement = requireTypeAndCast(LIST)
    final override fun asListOrNull(): ListElement? = requireTypeAndCastOrNull(LIST)
    final override fun asSexp(): SexpElement = requireTypeAndCast(SEXP)
    final override fun asSexpOrNull(): SexpElement? = requireTypeAndCastOrNull(SEXP)
    final override fun asStruct(): StructElement = requireTypeAndCast(STRUCT)
    final override fun asStructOrNull(): StructElement? = requireTypeAndCastOrNull(STRUCT)

    // These are overridden in the implementation of the type-specific elements.
    // The default implementation throws, complaining about an unexpected type.
    override val booleanValue: Boolean get() = expectedType(BOOL)
    override val longValue: Long get() = expectedType(INT)
    override val bigIntegerValue: BigInteger get() = expectedType(INT)
    override val textValue: String get() = expectedType(STRING, SYMBOL)
    override val stringValue: String get() = expectedType(STRING)
    override val symbolValue: String get() = expectedType(SYMBOL)
    override val decimalValue: Decimal get() = expectedType(DECIMAL)
    override val doubleValue: Double get() = expectedType(FLOAT)
    override val timestampValue: Timestamp get() = expectedType(TIMESTAMP)
    override val bytesValue: IonByteArray get() = expectedType(BLOB, CLOB)
    override val blobValue: IonByteArray get() = expectedType(BLOB)
    override val clobValue: IonByteArray get() = expectedType(CLOB)

    // Default implementations that perform the type check and wrap the corresponding non-nullable version.
    final override val booleanValueOrNull: Boolean? get() = requireTypeAndCastOrNull<BoolElement>(BOOL)?.booleanValue
    final override val longValueOrNull: Long? get() =  requireTypeAndCastOrNull<IntElement>(INT)?.longValue
    final override val bigIntegerValueOrNull: BigInteger? get() = requireTypeAndCastOrNull<IntElement>(INT)?.bigIntegerValue
    final override val textValueOrNull: String? get() = requireTypeAndCastOrNull<TextElement>(STRING, SYMBOL)?.textValue
    final override val stringValueOrNull: String? get() = requireTypeAndCastOrNull<StringElement>(STRING)?.textValue
    final override val symbolValueOrNull: String? get() = requireTypeAndCastOrNull<SymbolElement>(SYMBOL)?.textValue
    final override val decimalValueOrNull: Decimal? get() = requireTypeAndCastOrNull<DecimalElement>(DECIMAL)?.decimalValue
    final override val doubleValueOrNull: Double? get() = requireTypeAndCastOrNull<FloatElement>(FLOAT)?.doubleValue
    final override val timestampValueOrNull: Timestamp? get() = requireTypeAndCastOrNull<TimestampElement>(TIMESTAMP)?.timestampValue
    final override val bytesValueOrNull: IonByteArray? get() = requireTypeAndCastOrNull<LobElement>(BLOB, CLOB)?.bytesValue
    final override val blobValueOrNull: IonByteArray? get() = requireTypeAndCastOrNull<BlobElement>(BLOB)?.bytesValue
    final override val clobValueOrNull: IonByteArray? get() = requireTypeAndCastOrNull<ClobElement>(CLOB)?.bytesValue
}

