package com.amazon.ionelement.demos;

import com.amazon.ion.IonReader;
import com.amazon.ionelement.api.AnyElement;
import com.amazon.ionelement.api.ElementLoader;
import com.amazon.ionelement.api.IonElementLoaderOptions;
import com.amazon.ionelement.util.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

/**
 * Demonstrates correct usage of the element loader API from Java and helps developers of IonElement
 * verify that usage from Java is idiomatic.
 */
public class ElementLoaderDemo extends DemoBase {
    private static final String ION_TEXT_STRUCT = "{ a_field: \"hello!\"}";
    private static final String ION_TEXT_MULTIPLE_VALUES = "1 2 3";

    @Test
    void createIonElementLoaderOptions() {
        IonElementLoaderOptions opts = IonElementLoaderOptions.builder()
            .withIncludeLocationMeta(true)
            .withUseRecursiveLoad(false)
            .build();

        assertTrue(opts.getIncludeLocationMeta());
        assertFalse(opts.getUseRecursiveLoad());

        IonElementLoaderOptions copy = opts.toBuilder()
            .withUseRecursiveLoad(true)
            .build();

        // Unchanged
        assertTrue(copy.getIncludeLocationMeta());
        // Changed
        assertTrue(copy.getUseRecursiveLoad());
    }

    @Test
    void loadSingleElement_text() {
        AnyElement value = ElementLoader.loadSingleElement(ION_TEXT_STRUCT);
        assertIonEquiv(ION_TEXT_STRUCT, value);
    }

    @Test
    void loadSingleElement_ion_reader() throws IOException {
        try (IonReader reader = TestUtils.ION.newReader(ION_TEXT_STRUCT)) {
            AnyElement value = ElementLoader.loadSingleElement(reader);
            assertIonEquiv("{ a_field: \"hello!\"}", value);
        }
    }

    @Test
    void loadAllElements_text() {
        Iterable<AnyElement> values = ElementLoader.loadAllElements(ION_TEXT_MULTIPLE_VALUES);
        Iterator<AnyElement> itr = values.iterator();
        assertIonEquiv("1", itr.next());
        assertIonEquiv("2", itr.next());
        assertIonEquiv("3", itr.next());
        assertFalse(itr.hasNext());
    }

    @Test
    void loadAllElements_reader() throws IOException {
        try (IonReader reader = TestUtils.ION.newReader(ION_TEXT_MULTIPLE_VALUES)) {
            Iterable<AnyElement> values = ElementLoader.loadAllElements(reader);
            Iterator<AnyElement> itr = values.iterator();
            assertIonEquiv("1", itr.next());
            assertIonEquiv("2", itr.next());
            assertIonEquiv("3", itr.next());
            assertFalse(itr.hasNext());
        }
    }
    @Test
    void loadCurrentElement() throws IOException {
        try (IonReader reader = TestUtils.ION.newReader(ION_TEXT_MULTIPLE_VALUES)) {
            reader.next();
            reader.next();
            AnyElement value = ElementLoader.loadCurrentElement(reader);
            assertIonEquiv("2", value);
        }
    }
}
