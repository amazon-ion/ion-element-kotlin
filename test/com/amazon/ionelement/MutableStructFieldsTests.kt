package com.amazon.ionelement

import com.amazon.ionelement.api.buildStruct
import com.amazon.ionelement.api.field
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionListOf
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionelement.api.loadSingleElement
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MutableStructFieldsTests {
    @Test
    fun `StructElement to mutable fields back to StructElement`() {
        assertEquals(testStruct, ionStructOf(testStruct.mutableFields()))
    }

    @Test
    fun `StructElement to mutable fields back to StructElement with annotations and metas`() {
        assertEquals(
            testStruct
                .withAnnotations("foo", "bar")
                .withMeta("meta", 1),

            ionStructOf(testStruct.mutableFields(), listOf("foo", "bar"), mapOf("meta" to 1))
        )
    }

    @Test
    fun get() {
        val mutableFields = testStruct.mutableFields()
        assertEquals(ionString("123-456-789"), mutableFields["isbn"].asString())
        assertThrows<IllegalArgumentException> {
            mutableFields["nothing"]
        }
    }

    @Test
    fun getOptional() {
        val mutableFields = testStruct.mutableFields()
        assertNull(mutableFields.getOptional("nothing"))
        assertEquals(ionString("123-456-789"), mutableFields.getOptional("isbn")?.asString())
    }

    @Test
    fun getAll() {
        val mutableFields = testStruct.mutableFields()
        assertEquals(2, mutableFields.getAll("author").count())
        assertEquals(
            setOf(
                ionStructOf("lastname" to ionString("Doe"), "firstname" to ionString("Jane")),
                ionStructOf("lastname" to ionString("Smith"), "firstname" to ionString("Jane"))
            ),
            mutableFields.getAll("author").map { it.asStruct() }.toSet()
        )
        assertEquals(0, mutableFields.getAll("nothing").count())
    }

    @Test
    fun containsField() {
        val mutableFields = testStruct.mutableFields()
        assertTrue(mutableFields.containsField("author"))
        assertTrue(mutableFields.containsField("isbn"))
        assertFalse(mutableFields.containsField("nothing"))
    }

    @Test
    fun set() {
        val mutableFields = testStruct.mutableFields()
        mutableFields["nothing"] = ionInt(0)
        mutableFields["isbn"] = ionInt(1)
        mutableFields["author"] = ionInt(2)

        assertEquals(ionInt(0), mutableFields["nothing"].asInt())
        assertEquals(ionInt(1), mutableFields["isbn"].asInt())
        assertEquals(1, mutableFields.getAll("author").count())
        assertEquals(ionInt(2), mutableFields.getAll("author").first().asInt())
    }

    @Test
    fun setAll() {
        val mutableFields = testStruct.mutableFields()
        mutableFields.setAll(
            listOf(
                field("year", ionInt(2000)),
                field("year", ionInt(2012))
            )
        )
        mutableFields.setAll(
            listOf(
                field("author", ionString("Alice")),
                field("author", ionString("Bob"))
            )
        )
        assertEquals(
            setOf(ionInt(2000).asAnyElement(), ionInt(2012).asAnyElement()),
            mutableFields.getAll("year").toSet()
        )

        assertEquals(
            setOf(ionString("Alice").asAnyElement(), ionString("Bob").asAnyElement()),
            mutableFields.getAll("author").toSet()
        )
    }

    @Test
    fun add() {
        val mutableFields = testStruct.mutableFields()
        mutableFields.add(field("year", ionInt(2000)))
        mutableFields.add("year", ionInt(2012))
        mutableFields.add("author", ionString("Alice"))

        assertEquals(
            setOf(ionInt(2000).asAnyElement(), ionInt(2012).asAnyElement()),
            mutableFields.getAll("year").toSet()
        )

        assertEquals(
            setOf(ionString("Alice").asAnyElement()).plus(testStruct.getAll("author")),
            mutableFields.getAll("author").toSet()
        )
    }

    @Test
    fun plusAssign() {
        val mutableFields = testStruct.mutableFields()
        mutableFields += field("year", ionInt(2000))
        mutableFields += listOf(field("year", ionInt(2012)), field("year", ionInt(2021)))

        assertEquals(
            setOf(ionInt(2000).asAnyElement(), ionInt(2012).asAnyElement(), ionInt(2021).asAnyElement()),
            mutableFields.getAll("year").toSet()
        )
    }

    @Test
    fun remove() {
        val mutableFields = testStruct.mutableFields()
        mutableFields.remove(field("author", loadSingleElement("{lastname: \"Doe\", firstname: \"Jane\" }")))
        mutableFields.remove(field("author", loadSingleElement("{lastname: \"Smith\", firstname: \"Jane\" }")))

        assertNull(mutableFields.getOptional("author"))
    }

    @Test
    fun removeAll() {
        val mutableFields = testStruct.mutableFields()
        mutableFields.removeAll("author")

        assertNull(mutableFields.getOptional("author"))
    }

    @Test
    fun buildStructTest() {
        val actual = buildStruct {
            add("isbn", ionString("123-456-789"))
            add(
                "author",
                buildStruct {
                    add("lastname", ionString("Doe"))
                    add("firstname", ionString("Jane"))
                }
            )
            add(
                "author",
                buildStruct {
                    add("lastname", ionString("Smith"))
                    add("firstname", ionString("Jane"))
                }
            )
            add("title", ionString("AWS User Guide"))
            add("category", ionListOf(ionSymbol("Non-Fiction"), ionSymbol("Technology")))
        }

        assertEquals(testStruct, actual)
    }

    companion object {
        val testStruct = loadSingleElement(
            """
            {
              isbn: "123-456-789",
              author: {
                lastname: "Doe",
                firstname: "Jane"
              },
              author: {
                lastname: "Smith",
                firstname: "Jane"
              },
              title: "AWS User Guide",
              category: [
                'Non-Fiction',
                'Technology'
              ]
            }
        """
        ).asStruct()
    }
}
