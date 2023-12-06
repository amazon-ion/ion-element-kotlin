package com.amazon.ionelement.demos.java;

import com.amazon.ionelement.api.Ion;
import com.amazon.ionelement.api.StructElement;
import kotlin.Unit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.amazon.ionelement.api.ElementLoader.loadSingleElement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class MutableStructFieldsDemo {

    @Test
    void createUpdatedStructFromExistingStruct() {
        StructElement original = loadSingleElement(
                "{name:\"Alice\",scores:{darts:100,billiards:15,}}").asStruct();

        StructElement expected = loadSingleElement(
                "{name:\"Alice\",scores:{darts:100,billiards:30,pingPong:200,}}").asStruct();

        StructElement updated = original.update(fields -> {
            fields.set("scores",
                    original.get("scores").asStruct().update(scoreFields -> {
                        scoreFields.add("pingPong", Ion.ionInt(200));
                        scoreFields.set("billiards", Ion.ionInt(30));
                        return Unit.INSTANCE;
                    }));
            return Unit.INSTANCE;
        });

        assertEquals(expected, updated);
    }
}
