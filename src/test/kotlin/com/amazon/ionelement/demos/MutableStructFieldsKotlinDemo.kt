package com.amazon.ionelement.demos

import com.amazon.ionelement.api.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class MutableStructFieldsKotlinDemo {
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

        val expected = ionStructOf {
            add("name", ionString("Alice"))
            add(
                "scores",
                ionStructOf {
                    add("darts", ionInt(100))
                    add("billiards", ionInt(30))
                    add("pingPong", ionInt(200))
                }
            )
        }

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
