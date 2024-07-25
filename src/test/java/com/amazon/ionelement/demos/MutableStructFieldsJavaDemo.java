package com.amazon.ionelement.demos;

import com.amazon.ionelement.api.Ion;
import com.amazon.ionelement.api.StructElement;
import org.junit.jupiter.api.Test;

import static com.amazon.ionelement.api.ElementLoader.loadSingleElement;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableStructFieldsJavaDemo {

    @Test
    void createUpdatedStructFromExistingStruct() {
        StructElement original = loadSingleElement(
                "{name:\"Alice\",scores:{darts:100,billiards:15,}}").asStruct();

        StructElement expected = Ion.ionStructOf(a -> {
            a.add("name", Ion.ionString("Alice"));
            a.add("scores", Ion.ionStructOf(b -> {
                    b.add("darts", Ion.ionInt(100));
                    b.add("billiards", Ion.ionInt(30));
                    b.add("pingPong", Ion.ionInt(200));
                }
            ));
        });

        StructElement updated = original.update(fields ->
            fields.set("scores",
                    fields.get("scores").asStruct().update(scoreFields -> {
                        scoreFields.add("pingPong", Ion.ionInt(200));
                        scoreFields.set("billiards", Ion.ionInt(30));
                    })
        ));

        assertEquals(expected, updated);
    }
}
