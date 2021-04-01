package com.amazon.ionelement.demos.kotlin

import com.amazon.ionelement.api.IntElement
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.StringElement
import com.amazon.ionelement.api.TextElement
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NarrowingDemo {
    sealed class ValueContainer {
        data class Text(val textValue: String) : ValueContainer()
        data class Integer(val indexValue: Int) : ValueContainer()
    }

    @Test
    fun `branching based on element type`() {
        // Note that we do not get the benefit of sealed classes here, however, Kotlin's implicit casting does make
        // this more concise than the old way.
        fun elementToValueContainer(elem: IonElement) = when (elem) {
            is TextElement -> ValueContainer.Text(elem.textValue)
            is IntElement -> ValueContainer.Integer(elem.longValue.toInt())
            else -> error("Unexpected ion type")
        }

        assertEquals(ValueContainer.Integer(1), elementToValueContainer(ionInt(1)))
        assertEquals(ValueContainer.Text("foo"), elementToValueContainer(ionString("foo")))
    }

    class Env(val elem: IonElement)
    private inline fun <reified T: IonElement> List<Env>.extractNarrowValue() =
        first().let { it.elem as T }

    @Test
    fun `extracting a narrowed value from a list of Env`() {
        val envList = listOf(Env(ionString("foo")))
        val strElem = envList.extractNarrowValue<StringElement>()
        assertEquals("foo", strElem.textValue)
    }

    @Test
    fun `when type is unexpected`() {
        val someInt: IonElement = ionInt(1)
        val someString: IonElement = ionString("two")

        assertNull((someInt as? StringElement)?.textValue)
        assertNotNull((someString as? StringElement)?.textValue)
    }
}
