package com.amazon.ionelement.demos.kotlin

import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.loadSingleElement
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class MutableStructFieldsDemo {
    @Test
    fun `create updated struct from existing struct`() {
        val original = loadSingleElement(
            """
            {
                name: "Alice",               
                scores: {
                    darts: 100,
                    billiards: 15,
                }
            }
            """.trimIndent()
        ).asStruct()

        val expected = loadSingleElement(
            """
                {
                    name: "Alice",               
                    scores: {
                        darts: 100,
                        billiards: 30,
                        pingPong: 200,
                    }
                }
            """.trimIndent()
        ).asStruct()

        val updated = original.update {
            set(
                "scores",
                original["scores"].asStruct().update {
                    set("pingPong", ionInt(200))
                    set("billiards", ionInt(30))
                }
            )
        }

        assertEquals(expected, updated)
    }
}
