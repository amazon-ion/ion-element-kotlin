package com.amazon.ionelement

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.*
import java.math.BigInteger
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ArgumentsSources
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

@OptIn(IonOperators::class)
class IonOperatorsTests {

    @Test
    fun boolElementNotOperatorTests() {
        assertEquals(ionBool(true), !ionBool(false))
        assertEquals(ionBool(false), !ionBool(true))
    }

    @Test
    fun boolElementAndOperatorTests() {
        assertEquals(ionBool(true), ionBool(true) and true)
        assertEquals(ionBool(false), ionBool(true) and false)
        assertEquals(ionBool(false), ionBool(false) and true)
        assertEquals(ionBool(false), ionBool(false) and false)

        assertEquals(ionBool(true), true and ionBool(true))
        assertEquals(ionBool(false), true and ionBool(false))
        assertEquals(ionBool(false), false and ionBool(true))
        assertEquals(ionBool(false), false and ionBool(false))
    }

    @Test
    fun boolElementOrOperatorTests() {
        assertEquals(ionBool(true), ionBool(true) or true)
        assertEquals(ionBool(true), ionBool(true) or false)
        assertEquals(ionBool(true), ionBool(false) or true)
        assertEquals(ionBool(false), ionBool(false) or false)

        assertEquals(ionBool(true), true or ionBool(true))
        assertEquals(ionBool(true), true or ionBool(false))
        assertEquals(ionBool(true), false or ionBool(true))
        assertEquals(ionBool(false), false or ionBool(false))
    }

    @Test
    fun longIntElementIncOverflowOperatorTest() {
        assertEquals(ionInt(Long.MAX_VALUE.toBigInteger().inc()), ionInt(Long.MAX_VALUE).inc())
    }

    @Test
    fun longIntElementDecOverflowOperatorTest() {
        assertEquals(ionInt(Long.MIN_VALUE.toBigInteger().dec()), ionInt(Long.MIN_VALUE).dec())
    }

    @Test
    fun longIntElementWithLongPlusOverflowTests() {
        assertEquals(ionInt(LONG_MAX.toBigInteger() + LONG_MAX.toBigInteger()), ionInt(LONG_MAX) + LONG_MAX)
        assertEquals(ionInt(LONG_MIN.toBigInteger() + LONG_MIN.toBigInteger()), ionInt(LONG_MIN) + LONG_MIN)
        assertEquals(ionInt(LONG_MAX.toBigInteger() + LONG_MAX.toBigInteger()), LONG_MAX + ionInt(LONG_MAX))
        assertEquals(ionInt(LONG_MIN.toBigInteger() + LONG_MIN.toBigInteger()), LONG_MIN + ionInt(LONG_MIN))
    }

    @Test
    fun longIntElementWithIntPlusOverflowTests() {
        assertEquals(ionInt(LONG_MAX.toBigInteger() + INT_MAX.toBigInteger()), ionInt(LONG_MAX) + INT_MAX)
        assertEquals(ionInt(INT_MAX.toBigInteger() + LONG_MAX.toBigInteger()), INT_MAX + ionInt(LONG_MAX))
    }

    @Test
    fun longIntElementWithLongMinusOverflowTests() {
        assertEquals(ionInt(LONG_MIN.toBigInteger() - LONG_MAX.toBigInteger()), ionInt(LONG_MIN) - LONG_MAX)
        assertEquals(ionInt(LONG_MIN.toBigInteger() - LONG_MAX.toBigInteger()), LONG_MIN - ionInt(LONG_MAX))
    }

    @Test
    fun longIntElementWithIntMinusOverflowTests() {
        assertEquals(ionInt(LONG_MIN.toBigInteger() - INT_MAX.toBigInteger()), ionInt(LONG_MIN) - INT_MAX)
        assertEquals(ionInt(LONG_MIN.toBigInteger() - INT_MAX.toBigInteger()), -INT_MAX + ionInt(LONG_MIN))
    }


    @Test
    fun longIntElementWithLongTimesOverflowTests() {
        assertEquals(ionInt(LONG_MAX.toBigInteger() * LONG_MAX.toBigInteger()), ionInt(LONG_MAX) * LONG_MAX)
        assertEquals(ionInt(LONG_MIN.toBigInteger() * LONG_MIN.toBigInteger()), ionInt(LONG_MIN) * LONG_MIN)
        assertEquals(ionInt(LONG_MAX.toBigInteger() * LONG_MIN.toBigInteger()), ionInt(LONG_MAX) * LONG_MIN)

        assertEquals(ionInt(LONG_MAX.toBigInteger() * LONG_MAX.toBigInteger()), LONG_MAX * ionInt(LONG_MAX))
        assertEquals(ionInt(LONG_MIN.toBigInteger() * LONG_MIN.toBigInteger()), LONG_MIN * ionInt(LONG_MIN))
        assertEquals(ionInt(LONG_MAX.toBigInteger() * LONG_MIN.toBigInteger()), LONG_MAX * ionInt(LONG_MIN))
    }

    @Test
    fun longIntElementWithIntTimesOverflowTests() {
        assertEquals(ionInt(LONG_MAX.toBigInteger() * INT_MAX.toBigInteger()), ionInt(LONG_MAX) * INT_MAX)
        assertEquals(ionInt(INT_MAX.toBigInteger() * LONG_MAX.toBigInteger()), INT_MAX * ionInt(LONG_MAX))
    }

    @Test
    fun longIntElementWithLongPlusNoOverflowTests() {
        assertEquals(ionInt(1 + 2), ionInt(1) + 2)
        assertEquals(ionInt(1 + 2), 1 + ionInt(2))
    }

    @Test
    fun longIntElementWithLongMinusNoOverflowTests() {
        assertEquals(ionInt(1 - 2), ionInt(1) - 2)
        assertEquals(ionInt(1 - 2), 1 - ionInt(2))
    }

    @Test
    fun longIntElementWithLongTimesNoOverflowTests() {
        assertEquals(ionInt(1 * 2), ionInt(1) * 2)
        assertEquals(ionInt(1 * 2), 1 * ionInt(2)
    }

    @Test
    fun longIntElementWithLongDivideByZeroTest() {
        assertThrows<ArithmeticException> {
            ionInt(1) / 0
        }
    }

    @Test
    fun longIntElementWithLongPlusRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = Random.nextLong()
            assertEquals(ionInt(x + y), x + ionInt(y), "failed on arguments: $x and $y")
            assertEquals(ionInt(x + y), ionInt(x) + y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongMinusRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = Random.nextLong()
            assertEquals(ionInt(x - y), x - ionInt(y), "failed on arguments: $x and $y")
            assertEquals(ionInt(x - y), ionInt(x) - y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongTimesRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = Random.nextLong()
            assertEquals(ionInt(x * y), x * ionInt(y), "failed on arguments: $x and $y")
            assertEquals(ionInt(x * y), ionInt(x) * y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongDivideRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = with(Random.nextLong()) {if (this == 0L) 1L else this}
            assertEquals(ionInt(x / y), x / ionInt(y), "failed on arguments: $x and $y")
            assertEquals(ionInt(x / y), ionInt(x) / y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongRemainderRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = with(Random.nextLong()) {if (this == 0L) 1L else this}
            assertEquals(ionInt(x % y), x % ionInt(y), "failed on arguments: $x and $y")
            assertEquals(ionInt(x % y), ionInt(x) % y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementNegationRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            assertEquals(ionInt(-x), -ionInt(x), "failed on argument: $x")
        }
    }

    @Test
    fun longIntElementIncRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            assertEquals(ionInt(x.inc()), ionInt(x).inc(), "failed on argument: $x")
        }
    }

    @Test
    fun longIntElementDecRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            assertEquals(ionInt(x.dec()), ionInt(x).dec(), "failed on argument: $x")
        }
    }

    @Test
    fun longIntElementWithLongEqRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = with(Random.nextBoolean()) { if(this) x else Random.nextLong() }
            assertEquals(x == y, ionInt(x) eq ionInt(y), "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongLtRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = with(Random.nextBoolean()) { if(this) x else Random.nextLong() }
            assertEquals(x < y, ionInt(x) lt ionInt(y), "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongGtRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = with(Random.nextBoolean()) { if(this) x else Random.nextLong() }
            assertEquals(x > y, ionInt(x) gt ionInt(y), "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongLteRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = with(Random.nextBoolean()) { if(this) x else Random.nextLong() }
            assertEquals(x <= y, ionInt(x) lte ionInt(y), "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithLongGteRandomTests() {
        repeat(500) {
            val x = Random.nextLong()
            val y = with(Random.nextBoolean()) { if(this) x else Random.nextLong() }
            assertEquals(x >= y, ionInt(x) gte ionInt(y), "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithIntPlusRandomTest() {
        repeat(500) {
            val x = Random.nextInt()
            val y = Random.nextInt()
            assertEquals(ionInt((x + y).toLong()), x + ionInt(y.toLong()), "failed on arguments: $x and $y")
            assertEquals(ionInt((x + y).toLong()), ionInt(x.toLong()) + y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithIntMinusRandomTest() {
        repeat(500) {
            val x = Random.nextInt()
            val y = Random.nextInt()
            assertEquals(ionInt((x - y).toLong()), x - ionInt(y.toLong()), "failed on arguments: $x and $y")
            assertEquals(ionInt((x - y).toLong()), ionInt(x.toLong()) - y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithIntTimesRandomTest() {
        repeat(500) {
            val x = Random.nextInt()
            val y = Random.nextInt()
            assertEquals(ionInt((x * y).toLong()), x * ionInt(y.toLong()), "failed on arguments: $x and $y")
            assertEquals(ionInt((x * y).toLong()), ionInt(x.toLong()) * y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithIntDivideRandomTest() {
        repeat(500) {
            val x = Random.nextInt()
            val y = with(Random.nextInt()) {if (this == 0) 1 else this}
            assertEquals(ionInt((x / y).toLong()), x / ionInt(y.toLong()), "failed on arguments: $x and $y")
            assertEquals(ionInt((x / y).toLong()), ionInt(x.toLong()) / y, "failed on arguments: $x and $y")
        }
    }

    @Test
    fun longIntElementWithIntRemainderRandomTest() {
        repeat(500) {
            val x = Random.nextInt()
            val y = with(Random.nextInt()) {if (this == 0) 1 else this}
            assertEquals(ionInt((x % y).toLong()), x % ionInt(y.toLong()), "failed on arguments: $x and $y")
            assertEquals(ionInt((x % y).toLong()), ionInt(x.toLong()) % y, "failed on arguments: $x and $y")
        }
    }



    @Test
    fun testRemainderNoOverflowTests() {

        // Additional randomized tests
        repeat(1000) {
            x = Random.nextLong()
            y = Random.nextLong()


            // Mixed-type cases
            z = Random.nextInt()
            assertEquals(exactLongOp(x, z.toLong(), Math::addExact, BigInteger::add), ionInt(x) + z)
            assertEquals(exactLongOp(x, z.toLong(), Math::subtractExact, BigInteger::minus), ionInt(x) - z)
            assertEquals(exactLongOp(x, z.toLong(), Math::multiplyExact, BigInteger::times), ionInt(x) * z)
            assertEquals(ionInt(x / z), ionInt(x) / z)
            assertEquals(ionInt(x % z), ionInt(x) % z)

            assertEquals(exactLongOp(z.toLong(), y, Math::addExact, BigInteger::add), z + ionInt(y))
            assertEquals(exactLongOp(z.toLong(), y, Math::subtractExact, BigInteger::minus), z - ionInt(y))
            assertEquals(exactLongOp(z.toLong(), y, Math::multiplyExact, BigInteger::times), z * ionInt(y))
            assertEquals(ionInt(z / y), z / ionInt(x))
            assertEquals(ionInt(z % y), z % ionInt(x))

            // Mixed-type Long
            assertEquals(x == y, ionInt(x) eq y)
            assertTrue(ionInt(x) eq x)
            assertEquals(x != y, !(ionInt(x) eq y))
            assertEquals(x < y, ionInt(x) lt y)
            assertEquals(x > y, ionInt(x) gt y)
            assertEquals(x <= y, ionInt(x) lte y)
            assertEquals(x >= y, ionInt(x) gte y)

            assertEquals(x == y, x eq ionInt(y))
            assertTrue(x eq ionInt(x))
            assertEquals(x != y, !(x eq ionInt(y)))
            assertEquals(x < y, x lt ionInt(y))
            assertEquals(x > y, x gt ionInt(y))
            assertEquals(x <= y, x lte ionInt(y))
            assertEquals(x >= y, x gte ionInt(y))

            // Mixed-type Int
            assertEquals(x == z.toLong(), ionInt(x) eq z)
            assertTrue(ionInt(z.toLong()) eq z)
            assertEquals(x < z, ionInt(x) lt z)
            assertEquals(x > z, ionInt(x) gt z)
            assertEquals(x <= z, ionInt(x) lte z)
            assertEquals(x >= z, ionInt(x) gte z)

            assertEquals(z.toLong() == y, z eq ionInt(y))
            assertEquals(z.toLong() != y, !(z eq ionInt(y)))
            assertEquals(z < y, z lt ionInt(y))
            assertEquals(z > y, z gt ionInt(y))
            assertEquals(z <= y, z lte ionInt(y))
            assertEquals(z >= y, z gte ionInt(y))
        }
    }

    @Test
    fun intElementBigIntegerOperatorsTests() {
        repeat(1000) {
            val x = Random.nextLong().toBigInteger()
            val y = Random.nextLong().toBigInteger() * Random.nextLong().toBigInteger()
            assertEquals(ionInt(-x), -ionInt(x))
            assertEquals(ionInt(x.inc()), ionInt(x).inc())
            assertEquals(ionInt(x.dec()), ionInt(x).dec())

            assertEquals(x == y, ionInt(x) eq ionInt(y))
            assertEquals(x < y, ionInt(x) lt ionInt(y))
            assertEquals(x > y, ionInt(x) gt ionInt(y))
            assertEquals(x <= y, ionInt(x) lte ionInt(y))
            assertEquals(x >= y, ionInt(x) gte ionInt(y))

            // Mixed-type cases
            assertEquals(ionInt(x + y), x + ionInt(y))
            assertEquals(ionInt(x - y), x - ionInt(y))
            assertEquals(ionInt(x * y), x * ionInt(y))
            assertEquals(ionInt(x / y), x / ionInt(y))
            assertEquals(ionInt(x % y), x % ionInt(y))

            assertEquals(ionInt(x + y), ionInt(x) + y)
            assertEquals(ionInt(x - y), ionInt(x) - y)
            assertEquals(ionInt(x * y), ionInt(x) * y)
            assertEquals(ionInt(x / y), ionInt(x) / y)
            assertEquals(ionInt(x % y), ionInt(x) % y)

            assertEquals(x == y, x eq ionInt(y))
            assertEquals(x < y, x lt ionInt(y))
            assertEquals(x > y, x gt ionInt(y))
            assertEquals(x <= y, x lte ionInt(y))
            assertEquals(x >= y, x gte ionInt(y))

            assertEquals(x == y, ionInt(x) eq y)
            assertEquals(x < y, ionInt(x) lt y)
            assertEquals(x > y, ionInt(x) gt y)
            assertEquals(x <= y, ionInt(x) lte y)
            assertEquals(x >= y, ionInt(x) gte y)
        }
    }

    @Test
    fun intElementLongWithBigIntegerOperatorsTests() {
        // Additional randomized tests
        repeat(1000) {
            val long = Random.nextLong()
            val big = Random.nextLong().toBigInteger()
            val ieLong = ionInt(Random.nextLong())
            val ieBig = ionInt(Random.nextLong().toBigInteger())

            // These are the remaining combinations not tested previously
            assertEquals(ionInt(ieLong.bigIntegerValue + big), ieLong + big)
            assertEquals(ionInt(ieBig.bigIntegerValue + long.toBigInteger()), ieBig + long)
            assertEquals(ionInt(long.toBigInteger() + ieBig.bigIntegerValue), long + ieBig)
            assertEquals(ionInt(big + ieLong.bigIntegerValue), big + ieLong)

            assertEquals(ionInt(ieLong.bigIntegerValue - big), ieLong - big)
            assertEquals(ionInt(ieBig.bigIntegerValue - long.toBigInteger()), ieBig - long)
            assertEquals(ionInt(long.toBigInteger() - ieBig.bigIntegerValue), long - ieBig)
            assertEquals(ionInt(big - ieLong.bigIntegerValue), big - ieLong)

            assertEquals(ionInt(ieLong.bigIntegerValue * big), ieLong * big)
            assertEquals(ionInt(ieBig.bigIntegerValue * long.toBigInteger()), ieBig * long)
            assertEquals(ionInt(long.toBigInteger() * ieBig.bigIntegerValue), long * ieBig)
            assertEquals(ionInt(big * ieLong.bigIntegerValue), big * ieLong)

            assertEquals(ionInt(ieLong.bigIntegerValue / big), ieLong / big)
            assertEquals(ionInt(ieBig.bigIntegerValue / long.toBigInteger()), ieBig / long)
            assertEquals(ionInt(long.toBigInteger() / ieBig.bigIntegerValue), long / ieBig)
            assertEquals(ionInt(big / ieLong.bigIntegerValue), big / ieLong)

            assertEquals(ionInt(ieLong.bigIntegerValue % big), ieLong % big)
            assertEquals(ionInt(ieBig.bigIntegerValue % long.toBigInteger()), ieBig % long)
            assertEquals(ionInt(long.toBigInteger() % ieBig.bigIntegerValue), long % ieBig)
            assertEquals(ionInt(big % ieLong.bigIntegerValue), big % ieLong)

            assertEquals(ieLong.bigIntegerValue == ieBig.bigIntegerValue, ieLong eq ieBig)
            assertEquals(ieLong.bigIntegerValue == big, ieLong eq big)
            assertEquals(ieBig.bigIntegerValue == ieLong.bigIntegerValue, ieBig eq ieLong)
            assertEquals(ieBig.bigIntegerValue == long.toBigInteger(), ieBig eq long)
            assertEquals(long.toBigInteger() == ieBig.bigIntegerValue, long eq ieBig)
            assertEquals(big == ieLong.bigIntegerValue, big eq ieLong)

            assertEquals(ieLong.bigIntegerValue < ieBig.bigIntegerValue, ieLong lt ieBig)
            assertEquals(ieLong.bigIntegerValue < big, ieLong lt big)
            assertEquals(ieBig.bigIntegerValue < ieLong.bigIntegerValue, ieBig lt ieLong)
            assertEquals(ieBig.bigIntegerValue < long.toBigInteger(), ieBig lt long)
            assertEquals(long.toBigInteger() < ieBig.bigIntegerValue, long lt ieBig)
            assertEquals(big < ieLong.bigIntegerValue, big lt ieLong)

            assertEquals(ieLong.bigIntegerValue > ieBig.bigIntegerValue, ieLong gt ieBig)
            assertEquals(ieLong.bigIntegerValue > big, ieLong gt big)
            assertEquals(ieBig.bigIntegerValue > ieLong.bigIntegerValue, ieBig gt ieLong)
            assertEquals(ieBig.bigIntegerValue > long.toBigInteger(), ieBig gt long)
            assertEquals(long.toBigInteger() > ieBig.bigIntegerValue, long gt ieBig)
            assertEquals(big > ieLong.bigIntegerValue, big gt ieLong)

            assertEquals(ieLong.bigIntegerValue <= ieBig.bigIntegerValue, ieLong lte ieBig)
            assertEquals(ieLong.bigIntegerValue <= big, ieLong lte big)
            assertEquals(ieBig.bigIntegerValue <= ieLong.bigIntegerValue, ieBig lte ieLong)
            assertEquals(ieBig.bigIntegerValue <= long.toBigInteger(), ieBig lte long)
            assertEquals(long.toBigInteger() <= ieBig.bigIntegerValue, long lte ieBig)
            assertEquals(big <= ieLong.bigIntegerValue, big lte ieLong)

            assertEquals(ieLong.bigIntegerValue >= ieBig.bigIntegerValue, ieLong gte ieBig)
            assertEquals(ieLong.bigIntegerValue >= big, ieLong gte big)
            assertEquals(ieBig.bigIntegerValue >= ieLong.bigIntegerValue, ieBig gte ieLong)
            assertEquals(ieBig.bigIntegerValue >= long.toBigInteger(), ieBig gte long)
            assertEquals(long.toBigInteger() >= ieBig.bigIntegerValue, long gte ieBig)
            assertEquals(big >= ieLong.bigIntegerValue, big gte ieLong)
        }
    }

    @Test
    fun decimalElementOperatorsTests() {
        // Test negative zero
        assertEquals(ionDecimal(Decimal.valueOf(-Decimal.valueOf(0))), -ionDecimal(Decimal.valueOf(0)))
        repeat(1000) {
            val x = Decimal.valueOf(Random.nextDouble().toBigDecimal())
            val y = Decimal.valueOf(Random.nextDouble().toBigDecimal())
            assertEquals(ionDecimal(Decimal.valueOf(-x)), -ionDecimal(x))
            assertEquals(ionDecimal(Decimal.valueOf(x.inc())), ionDecimal(x).inc())
            assertEquals(ionDecimal(Decimal.valueOf(x.dec())), ionDecimal(x).dec())

            assertEquals(x == y, ionDecimal(x) eq ionDecimal(y))
            assertTrue(ionDecimal(x) eq ionDecimal(x))
            assertEquals(x < y, ionDecimal(x) lt ionDecimal(y))
            assertEquals(x > y, ionDecimal(x) gt ionDecimal(y))
            assertEquals(x <= y, ionDecimal(x) lte ionDecimal(y))
            assertEquals(x >= y, ionDecimal(x) gte ionDecimal(y))

            // Mixed-type cases
            assertEquals(ionDecimal(Decimal.valueOf(x + y)), x + ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x - y)), x - ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x * y)), x * ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x / y)), x / ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x % y)), x % ionDecimal(y))

            assertEquals(ionDecimal(Decimal.valueOf(x + y)), ionDecimal(x) + y)
            assertEquals(ionDecimal(Decimal.valueOf(x - y)), ionDecimal(x) - y)
            assertEquals(ionDecimal(Decimal.valueOf(x * y)), ionDecimal(x) * y)
            assertEquals(ionDecimal(Decimal.valueOf(x / y)), ionDecimal(x) / y)
            assertEquals(ionDecimal(Decimal.valueOf(x % y)), ionDecimal(x) % y)

            assertEquals(x == y, x eq ionDecimal(y))
            assertTrue(x eq ionDecimal(x))
            assertEquals(x < y, x lt ionDecimal(y))
            assertEquals(x > y, x gt ionDecimal(y))
            assertEquals(x <= y, x lte ionDecimal(y))
            assertEquals(x >= y, x gte ionDecimal(y))

            assertEquals(x == y, ionDecimal(x) eq y)
            assertTrue(ionDecimal(x) eq x)
            assertEquals(x < y, ionDecimal(x) lt y)
            assertEquals(x > y, ionDecimal(x) gt y)
            assertEquals(x <= y, ionDecimal(x) lte y)
            assertEquals(x >= y, ionDecimal(x) gte y)
        }
    }

    @Test
    fun floatElementOperatorsTests() {
        repeat(1000) {
            val x = Random.nextDouble()
            val y = Random.nextDouble()
            assertEquals(ionFloat(-x), -ionFloat(x))
            assertEquals(ionFloat(x.inc()), ionFloat(x).inc())
            assertEquals(ionFloat(x.dec()), ionFloat(x).dec())

            assertEquals(x == y, ionFloat(x) eq ionFloat(y))
            assertTrue(ionFloat(x) eq ionFloat(x))
            assertEquals(x < y, ionFloat(x) lt ionFloat(y))
            assertEquals(x > y, ionFloat(x) gt ionFloat(y))
            assertEquals(x <= y, ionFloat(x) lte ionFloat(y))
            assertEquals(x >= y, ionFloat(x) gte ionFloat(y))

            // Mixed-type cases
            assertEquals(ionFloat(x + y), x + ionFloat(y))
            assertEquals(ionFloat(x - y), x - ionFloat(y))
            assertEquals(ionFloat(x * y), x * ionFloat(y))
            assertEquals(ionFloat(x / y), x / ionFloat(y))
            assertEquals(ionFloat(x % y), x % ionFloat(y))

            assertEquals(ionFloat(x + y), ionFloat(x) + y)
            assertEquals(ionFloat(x - y), ionFloat(x) - y)
            assertEquals(ionFloat(x * y), ionFloat(x) * y)
            assertEquals(ionFloat(x / y), ionFloat(x) / y)
            assertEquals(ionFloat(x % y), ionFloat(x) % y)

            assertEquals(x == y, x eq ionFloat(y))
            assertTrue(x eq ionFloat(x))
            assertEquals(x < y, x lt ionFloat(y))
            assertEquals(x > y, x gt ionFloat(y))
            assertEquals(x <= y, x lte ionFloat(y))
            assertEquals(x >= y, x gte ionFloat(y))

            assertEquals(x == y, ionFloat(x) eq y)
            assertTrue(ionFloat(x) eq x)
            assertEquals(x < y, ionFloat(x) lt y)
            assertEquals(x > y, ionFloat(x) gt y)
            assertEquals(x <= y, ionFloat(x) lte y)
            assertEquals(x >= y, ionFloat(x) gte y)
        }
    }

    @Test
    fun testTextElementOperators() {
        val x = "first test string"
        val y = "second test string"
        assertEquals(ionString(x + y), ionString(x) + y)
        assertEquals(ionString(x + y), ionString(x) + buildString { append(y) })
        assertEquals(ionSymbol(x + y), ionSymbol(x) + y)
        assertEquals(ionSymbol(x + y), ionSymbol(x) + buildString { append(y) })
    }
}

private const val LONG_MAX = Long.MAX_VALUE
private const val LONG_MIN = Long.MIN_VALUE
private const val INT_MAX = Int.MAX_VALUE

private val annotations = listOf("annotation1", "annotation2")
private val metas = mapOf("key1" to "value1", "key2" to listOf("value2", "value3"), "key3" to 1)

private fun ionBool(x: Boolean) = ionBool(x, annotations, metas)
private fun ionInt(x: Long) = ionInt(x, annotations, metas)
private fun ionInt(x: BigInteger) = ionInt(x, annotations, metas)
private fun ionFloat(x: Double) = ionFloat(x, annotations, metas)
private fun ionDecimal(x: Decimal) = ionDecimal(x, annotations, metas)
private fun ionString(x: String) = ionString(x, annotations, metas)
private fun ionSymbol(x: String) = ionSymbol(x, annotations, metas)

private fun exactLongOp(x: Long, y: Long, longOp: LongOp, bigOp: BigOp): IntElement =
    try {
        ionInt(longOp(x, y))
    } catch (e: ArithmeticException) {
        ionInt(bigOp(x.toBigInteger(), y.toBigInteger()))
    }

private typealias LongOp = (Long, Long) -> Long
private typealias BigOp = (BigInteger, BigInteger) -> BigInteger
