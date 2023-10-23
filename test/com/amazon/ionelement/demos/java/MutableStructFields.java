package com.amazon.ionelement.demos.java;

import com.amazon.ionelement.api.Ion;
import com.amazon.ionelement.api.StructElement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.amazon.ionelement.api.ElementLoader.loadSingleElement;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableStructFields {

    @Test
    void CreateUpdatedStructFromExistingStruct() {
        StructElement original = loadSingleElement("{name:\"Alice\",scores:{darts:100,billiards:15,}}").asStruct();

        StructElement expected = loadSingleElement("{name:\"Alice\",scores:{darts:100,billiards:30,pingPong:200,}}").asStruct();

        StructElement updated = original.copy(Arrays.asList(Ion.field("scores", original.get("scores").asStruct().getMutableFields()
                .add("pingPong", Ion.ionInt(200))
                .set("billiards", Ion.ionInt(30))
                .toStruct())));

        assertEquals(expected, updated);
    }
}
