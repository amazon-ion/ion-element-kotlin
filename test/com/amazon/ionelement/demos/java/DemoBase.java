package com.amazon.ionelement.demos.java;

import com.amazon.ionelement.api.AnyElement;
import com.amazon.ionelement.api.ElementLoader;
import com.amazon.ionelement.api.IonElement;
import org.junit.jupiter.api.Assertions;

public class DemoBase extends Assertions {
    protected void assertIonEquiv(String expectedIonText, IonElement value) {
        // Load the expected ion Text
        AnyElement expectedElem = ElementLoader.loadSingleElement(expectedIonText);
        assertEquals(expectedElem, value);
    }
}
