package com.amazon.ionelement.demos.java;

import com.amazon.ion.Decimal;
import com.amazon.ion.Timestamp;
import com.amazon.ionelement.api.AnyElement;
import com.amazon.ionelement.api.ElementLoader;
import com.amazon.ionelement.api.ElementType;
import com.amazon.ionelement.api.IntElement;
import com.amazon.ionelement.api.IonElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// These static imports greatly reduce the syntactic overhead of the constructor functions.

import static com.amazon.ionelement.api.Ion.emptyIonList;
import static com.amazon.ionelement.api.Ion.emptyIonStruct;
import static com.amazon.ionelement.api.Ion.field;
import static com.amazon.ionelement.api.Ion.ionBlob;
import static com.amazon.ionelement.api.Ion.ionBool;
import static com.amazon.ionelement.api.Ion.ionClob;
import static com.amazon.ionelement.api.Ion.ionDecimal;
import static com.amazon.ionelement.api.Ion.ionFloat;
import static com.amazon.ionelement.api.Ion.ionInt;
import static com.amazon.ionelement.api.Ion.ionListOf;
import static com.amazon.ionelement.api.Ion.ionNull;
import static com.amazon.ionelement.api.Ion.ionSexpOf;
import static com.amazon.ionelement.api.Ion.ionString;
import static com.amazon.ionelement.api.Ion.ionStructOf;
import static com.amazon.ionelement.api.Ion.ionSymbol;
import static com.amazon.ionelement.api.Ion.ionTimestamp;


/**
 * Demonstrates usage of the element constructor functions.
 *
 * These methods appear as top-level functions in the {@link com.amazon.ionelement.api} package to Kotlin,
 * but as static members of the {@link com.amazon.ionelement.api.Ion} class to Java.
 *
 * This class serves as documentation to show proper usage of the {@code IonElement} API from Java and to allow
 * developers of this library to verify that usage from Java is in fact idiomatic to Java, which is sometimes not
 * otherwise apparent if we're working only in Kotlin.
 */
public class ConstructionDemo extends Assertions {

    void assertEquiv(String expectedIonText, IonElement value) {
        // Load the expected ion Text
        AnyElement expectedElem = ElementLoader.loadSingleElement(expectedIonText);
        assertEquals(expectedElem, value);
    }

    @Test
    void alksjdfr() {
        IntElement ie = ionInt(42);
        ionSexpOf(ie);
    }
    @Test
    void scalarElements() {
        // boolean
        assertEquiv("true", ionBool(true));
        assertEquiv("false", ionBool(false));

        // integer
        assertEquiv("42", ionInt(42L));

        // string
        assertEquiv("\"make it so\"", ionString("make it so"));

        // symbol
        assertEquiv("'Aye aye, captain!'", ionSymbol("Aye aye, captain!"));

        // float
        assertEquiv("314e-2", ionFloat(3.14));

        // decimal (note: this is com.amazon.ion.Decimal)
        assertEquiv("98.6", ionDecimal(Decimal.valueOf("98.6")));

        // timestamp (note: this is com.amazon.ion.Timestamp)
        assertEquiv("1969-07-20T", ionTimestamp(Timestamp.valueOf("1969-07-20T")));
    }

    @Test
    void listElement() {
        assertEquiv("[]", emptyIonList());

        assertEquiv("[42]", ionListOf(Arrays.asList(ionInt(42))));
        assertEquiv("[1, 2, 3]", ionListOf(ionInt(1), ionInt(2), ionInt(3)));

        assertEquiv("foo::[]", ionListOf(Collections.emptyList(), Collections.singletonList("foo")));
        assertEquiv("foo::[42]", ionListOf(Collections.singletonList(ionInt(42)), Collections.singletonList("foo")));

        assertEquiv("foo::[]", ionListOf(Collections.emptyList(), Collections.singletonList("foo")));
        assertEquiv("foo::[42]", ionListOf(Collections.singletonList(ionInt(42)), Collections.singletonList("foo")));
    }

    @Test
    void annotations() {
        IntElement elem1 = ionInt(42L, Collections.singletonList("meaning_of_life"));
        assertEquiv("meaning_of_life::42", elem1);

        // TODO:  create an issue to expose `withAnnotations` and other `with*` functions to Java.
    }

    @Test
    void metas() {
        IntElement elem3 = ionInt(42L, Collections.singletonMap("so long", "thanks for all the fish"));

        assertEquals("thanks for all the fish", elem3.getMetas().get("so long"));
    }


    static class TestCase {
        String expectedIonText;
        IonElement element;

        public TestCase(String expectedIonText, IonElement element) {
            this.expectedIonText = expectedIonText;
            this.element = element;
        }

        @Override
        public String toString() {
            return "TestCase{" +
                    "expectedIonText='" + expectedIonText + '\'' +
                    ", element=" + element +
                    '}';
        }
    }

    @ParameterizedTest
    @MethodSource("parametersForValuesTest")
    void valuesTest(TestCase tc) {
        assertEquiv(tc.expectedIonText, tc.element);
    }

    static List<TestCase> parametersForValuesTest() {
        List<String> testAnnotations = Collections.singletonList("foo");
        return Arrays.asList(

                // null
                new TestCase("null", ionNull()),
                new TestCase("foo::null", ionNull(ElementType.NULL, testAnnotations)),

                // typed null
                new TestCase("null.int", ionNull(ElementType.INT)),
                new TestCase("foo::null.int", ionNull(ElementType.INT, testAnnotations)),

                // boolean
                new TestCase("true", ionBool(true)),
                new TestCase("foo::true", ionBool(true, testAnnotations)),

                // int
                new TestCase("1", ionInt(1)),
                new TestCase("foo::2", ionInt(2, testAnnotations)),

                // float
                new TestCase("314e-2", ionFloat(3.14)),
                new TestCase("foo::314e-2", ionFloat(3.14, testAnnotations)),

                // decimal
                new TestCase("1.23", ionDecimal(Decimal.valueOf("1.23"))),
                new TestCase("foo::1.23", ionDecimal(Decimal.valueOf("1.23"), testAnnotations)),

                // symbol
                new TestCase("some_symbol", ionSymbol("some_symbol")),
                new TestCase("foo::some_symbol", ionSymbol("some_symbol", testAnnotations)),

                // string
                new TestCase("\"some string\"", ionString("some string")),
                new TestCase("foo::\"some string\"", ionString("some string", testAnnotations)),

                // blob
                new TestCase("{{ VG8gaW5maW5pdHkuLi4gYW5kIGJleW9uZCE= }}", ionBlob("To infinity... and beyond!".getBytes(StandardCharsets.UTF_8))),
                new TestCase("foo::{{ VG8gaW5maW5pdHkuLi4gYW5kIGJleW9uZCE= }}", ionBlob("To infinity... and beyond!".getBytes(StandardCharsets.UTF_8), testAnnotations)),

                // clob
                new TestCase("{{ \"This is a CLOB of text.\" }}", ionClob("This is a CLOB of text.".getBytes(StandardCharsets.UTF_8))),
                new TestCase("foo::{{ \"This is a CLOB of text.\" }}", ionClob("This is a CLOB of text.".getBytes(StandardCharsets.UTF_8), testAnnotations)),

                //
                // Collection types
                //

                // list
                new TestCase("[1, 2]", ionListOf(ionInt(1), ionInt(2))),
                new TestCase("foo::[1, 2]", ionListOf(Arrays.asList(ionInt(1), ionInt(2)), testAnnotations)),

                // sexp
                new TestCase("(1 2)", ionSexpOf(ionInt(1), ionInt(2))),
                new TestCase("foo::(1 2)", ionSexpOf(Arrays.asList(ionInt(1), ionInt(2)), testAnnotations)),

                // struct
                new TestCase("{ meaning_of_life: 42, days_until_im_a_millionaire: 999999 }",
                        ionStructOf(
                                field("meaning_of_life", ionInt(42)),
                                field("days_until_im_a_millionaire", ionInt(999999)))),

                new TestCase("foo::{ meaning_of_life: 42, days_until_im_a_millionaire: 999999 }",
                        ionStructOf(
                                Arrays.asList(
                                        field("meaning_of_life", ionInt(42)),
                                        field("days_until_im_a_millionaire", ionInt(999999))),
                                testAnnotations)),


                new TestCase("true", ionBool(true))
        );
    }
}
