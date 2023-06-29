package com.amazon.ionelement.demos.kotlin

import com.amazon.ionelement.api.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class StructUpdateDemo {
    private val testStruct = loadSingleElement(
        """
        {
            a: 0,
            b: {
                c: {
                    d: [1, {a: foo, b: "bar"}],
                    e: 2,
                },
            },
        }
        """.trimIndent()
    ).asStruct()

    @Test
    fun `get from a nested path`() {
        assertEquals(
            ionInt(0),
            testStruct.getPath(IonPath(Field("a")))
        )

        assertEquals(
            ionSymbol("foo"),
            testStruct.getPath(IonPath(Field("b"), Field("c"), Field("d"), Index(1), Field("a")))
        )
    }

    @OptIn(IonOperators::class)
    @Test
    fun `update struct with dsl`() {
        val updatedStruct = testStruct.update {
            this["a"] = ionInt(1)
            this["a"] = this["a"].asInt() + 1
            this["b"] = this["b"].update {
                this["c"] = this["c"].update {
                    this["f"] = ionStructOf("x" to ionString("X"))
                }
            }
            this["x"] = ionStructOf(
                "q" to ionInt(10),
                "u" to this["b"],
                "r" to testStruct["a"],
                "r_" to this["a"]
            )
        }

        val expected = loadSingleElement(
            """
            {
                a:2,
                b:{
                    c:{d:[1,{a:foo,b:"bar"}],e:2,f:{x:"X"}}},
                    x:{q:10,u:{c:{d:[1,{a:foo,b:"bar"}],e:2,f:{x:"X"}}},
                    r:0,
                    r_:2
                }
            }
            """.trimIndent()
        ).asStruct()

        assertEquals(expected, updatedStruct)
    }
}
