package com.amazon.ionelement;

import com.amazon.ion.Decimal;
import com.amazon.ion.Timestamp;
import com.amazon.ionelement.api.*;
import kotlin.Pair;
import kotlinx.collections.immutable.PersistentList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.amazon.ionelement.api.Ion.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This particular test class is written in Java because the {@link List} interface is immutable in Kotlin,
 * and immutability is enforced in the type system (rather than at runtime). In Java, we have no such
 * limitation and can attempt to modify any {@link List} without getting any complaint from the compiler.
 */
public class ImmutabilityTests {


    @SafeVarargs
    private static <E> List<E> listOf(E... elements) {
        ArrayList<E> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, elements);
        return arrayList;
    }

    private static Map<String, Object> dummyMetas() {
        return IonMeta.metaContainerOf(listOf(new Pair<>("foo_meta", 1)));
    }

    private static List<String> dummyAnnotations() {
        return listOf("some_annotation");
    }

    static List<Object> parametersForMetasAndAnnotationsTests() {
        return listOf(
                ionNull(ElementType.NULL, dummyAnnotations(), dummyMetas()),
                ionBool(true, dummyAnnotations(), dummyMetas()),
                ionInt(123L, dummyAnnotations(), dummyMetas()),
                ionInt(BigInteger.ONE, dummyAnnotations(), dummyMetas()),
                ionTimestamp("2001T", dummyAnnotations(), dummyMetas()),
                ionTimestamp(Timestamp.valueOf("2001T"), dummyAnnotations(), dummyMetas()),
                ionFloat(1.0, dummyAnnotations(), dummyMetas()),
                ionDecimal(Decimal.ZERO, dummyAnnotations(), dummyMetas()),
                ionString("foo", dummyAnnotations(), dummyMetas()),
                ionSymbol("foo", dummyAnnotations(), dummyMetas()),
                ionClob(new byte[]{1}, dummyAnnotations(), dummyMetas()),
                ionBlob(new byte[]{1}, dummyAnnotations(), dummyMetas()),
                ionListOf(listOf(ionInt(1)), dummyAnnotations(), dummyMetas()),
                ionSexpOf(listOf(ionInt(1)), dummyAnnotations(), dummyMetas()),
                ionStructOf(listOf(field("foo", ionInt(1))), dummyAnnotations(), dummyMetas())
        );
    }

    @ParameterizedTest(name = "annotationsAreImmutable: {0}")
    @MethodSource("parametersForMetasAndAnnotationsTests")
    public void annotationsAreImmutable(IonElement elem) {
        // Assert that attempting to modify the annotations throws an UnsupportedOperationException
        try {
            elem.getAnnotations().add("foo");
            fail("Expected an exception to be thrown when trying to modify the annotations.");
        } catch (UnsupportedOperationException e) {
            // Pass
        }
    }

    @ParameterizedTest(name = "metasAreImmutable: {0}")
    @MethodSource("parametersForMetasAndAnnotationsTests")
    public void metasAreImmutable(IonElement elem) {
        // Assert that attempting to modify the metas throws an UnsupportedOperationException
        try {
            elem.getMetas().put("foo", "bar");
            fail("Expected an exception to be thrown when trying to modify the metas.");
        } catch (UnsupportedOperationException e) {
            // Pass
        }
    }
    
    @Test
    public void listElementValuesAreImmutable() {
        StringElement foo = ionString("foo");
        StringElement bar = ionString("bar");
        StringElement baz = ionString("baz");

        List<IonElement> listValues = listOf(foo, bar);

        ListElement ionList = ionListOf(listValues);

        // Assert that modifying the original collection does not modify the values in the ListElement
        listValues.add(baz);
        assertEquals(listOf(foo, bar), ionList.getValues());

        // Assert that attempting to modify the values in the ListElement throws an UnsupportedOperationException
        try {
            ionList.getValues().add(baz.asAnyElement());
            fail("Expected an exception to be thrown when trying to modify the contents of the ListElement values.");
        } catch (UnsupportedOperationException e) {
            // Pass
        }
    }

    @Test
    public void sexpElementValuesAreImmutable() {
        StringElement foo = ionString("foo");
        StringElement bar = ionString("bar");
        StringElement baz = ionString("baz");

        List<IonElement> sexpValues = listOf(foo, bar);

        SexpElement ionSexp = ionSexpOf(sexpValues);

        // Assert that modifying the original collection does not modify the values in the SexpElement
        sexpValues.add(baz);
        assertEquals(listOf(foo, bar), ionSexp.getValues());

        // Assert that attempting to modify the values in the SexpElement throws an UnsupportedOperationException
        try {
            ionSexp.getValues().add(baz.asAnyElement());
            fail("Expected an exception to be thrown when trying to modify the contents of the SexpElement values.");
        } catch (UnsupportedOperationException e) {
            // Pass
        }
    }

    @Test
    public void structElementValuesAreImmutable() {
        StructField foo = field("foo", ionString("foo"));
        StructField bar = field("bar", ionString("bar"));
        StructField baz = field("baz", ionString("baz"));

        List<StructField> structValues = listOf(foo, bar);

        StructElement ionStruct = ionStructOf(structValues);

        // Assert that modifying the original collection does not modify the fields in the StructElement
        structValues.add(baz);
        assertEquals(listOf(foo, bar), ionStruct.getFields());

        // Assert that attempting to modify the fields in the StructElement throws an UnsupportedOperationException
        try {
            ionStruct.getFields().add(baz);
            fail("Expected an exception to be thrown when trying to modify the contents of the StructElement fields.");
        } catch (UnsupportedOperationException e) {
            // Pass
        }
    }
}
