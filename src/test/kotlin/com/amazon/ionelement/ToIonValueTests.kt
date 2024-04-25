package com.amazon.ionelement

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionelement.api.toIonElement
import com.amazon.ionelement.api.toIonValue
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class ToIonValueTests {

    /**
     * This test is needed due to the way `IonElement.toIonValue(ValueFactory)` uses a temporary container to obtain
     * an `IonWriter` implementation to which the receiver will be written.
     */
    @Test
    fun `IonValue instances converted from IonElement instances can be added to IonContainer instances`() {
        val ion = IonSystemBuilder.standard().build()
        val ionValue = ion.singleValue("1")
        val element = ionValue.toIonElement()
        val ionList = ion.newList(element.toIonValue(ion))

        assertEquals(ion.singleValue("[1]"), ionList)
    }
}
