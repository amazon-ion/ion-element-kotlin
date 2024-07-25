package com.amazon.ionelement.demos;

import com.amazon.ionelement.api.AnyElement;
import com.amazon.ionelement.api.ElementLoader;
import com.amazon.ionelement.api.IonElement;
import org.junit.jupiter.api.Assertions;

/** Base class for Java-based unit test classes that we use to demonstrate proper use of IonElement from Java. */
public class DemoBase extends Assertions {

    /** Asserts that [value] is equivalent to the [expectedIonText]. */
    protected void assertIonEquiv(String expectedIonText, IonElement value) {
        // Load the expected ion Text
        AnyElement expectedElem = ElementLoader.loadSingleElement(expectedIonText);
        assertEquals(expectedElem, value);
    }
}
