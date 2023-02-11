package com.amazon.ionelement

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.*
import java.math.BigInteger
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

@OptIn(IonOperators::class)
class IonOperatorsTests {
    @Test
    fun boolElementOperatorsTest() {
        assertEquals(ionBool(true), !ionBool(false))
        assertEquals(ionBool(false), !ionBool(true))

        // Mixed-type cases
        assertEquals(ionBool(true), ionBool(true) and true)
        assertEquals(ionBool(false), ionBool(true) and false)
        assertEquals(ionBool(false), ionBool(false) and true)
        assertEquals(ionBool(false), ionBool(false) and false)

        assertEquals(ionBool(true), true and ionBool(true))
        assertEquals(ionBool(false), true and ionBool(false))
        assertEquals(ionBool(false), false and ionBool(true))
        assertEquals(ionBool(false), false and ionBool(false))

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
    fun intElementLongOperatorsTests() {
        // Check explicitly for overflow cases
        var x = Long.MAX_VALUE
        var y = Long.MIN_VALUE
        var z = Int.MAX_VALUE
        assertEquals(ionInt(x.toBigInteger().inc()), ionInt(x).inc())
        assertEquals(ionInt(y.toBigInteger().dec()), ionInt(y).dec())

        // Mixed Long cases
        assertEquals(ionInt(x.toBigInteger() + x.toBigInteger()), ionInt(x) + x)
        assertEquals(ionInt(y.toBigInteger() + y.toBigInteger()), ionInt(y) + y)
        assertEquals(ionInt(y.toBigInteger() - x.toBigInteger()), ionInt(y) - x)
        assertEquals(ionInt(x.toBigInteger() * x.toBigInteger()), ionInt(x) * x)
        assertEquals(ionInt(y.toBigInteger() * y.toBigInteger()), ionInt(y) * y)
        assertEquals(ionInt(x.toBigInteger() * y.toBigInteger()), ionInt(x) * y)
        assertEquals(ionInt(x.toBigInteger() + x.toBigInteger()), x + ionInt(x))
        assertEquals(ionInt(y.toBigInteger() + y.toBigInteger()), y + ionInt(y))
        assertEquals(ionInt(y.toBigInteger() - x.toBigInteger()), y - ionInt(x))
        assertEquals(ionInt(x.toBigInteger() * x.toBigInteger()), x * ionInt(x))
        assertEquals(ionInt(y.toBigInteger() * y.toBigInteger()), y * ionInt(y))
        assertEquals(ionInt(x.toBigInteger() * y.toBigInteger()), x * ionInt(y))

        // Mixed Int cases
        assertEquals(ionInt(x.toBigInteger() + z.toBigInteger()), ionInt(x) + z)
        assertEquals(ionInt(y.toBigInteger() - z.toBigInteger()), ionInt(y) - z)
        assertEquals(ionInt(x.toBigInteger() * z.toBigInteger()), ionInt(x) * z)

        assertEquals(ionInt(z.toBigInteger() + x.toBigInteger()), z + ionInt(x))
        assertEquals(ionInt(y.toBigInteger() - z.toBigInteger()), -z + ionInt(y))
        assertEquals(ionInt(z.toBigInteger() * x.toBigInteger()), z * ionInt(x))

        // Check explicitly for non-overflow cases
        x = Random.nextInt().toLong()
        y = Random.nextInt().toLong()

        assertEquals(ionInt(x + y), ionInt(x) + y)
        assertEquals(ionInt(x - y), ionInt(x) - y)
        assertEquals(ionInt(x * y), ionInt(x) * y)
        assertEquals(ionInt(x / y), ionInt(x) / y)
        assertEquals(ionInt(x % y), ionInt(x) % y)

        assertEquals(ionInt(x + y), x + ionInt(y))
        assertEquals(ionInt(x - y), x - ionInt(y))
        assertEquals(ionInt(x * y), x * ionInt(y))
        assertEquals(ionInt(x / y), x / ionInt(y))
        assertEquals(ionInt(x % y), x % ionInt(y))

        // Additional randomized tests
        repeat(1000) {
            x = Random.nextLong()
            y = Random.nextLong()
            assertEquals(ionInt(-x), -ionInt(x))
            assertEquals(ionInt(x.inc()), ionInt(x).inc())
            assertEquals(ionInt(x.dec()), ionInt(x).dec())

            assertEquals(x == y, ionInt(x) eq ionInt(y))
            assertTrue(ionInt(x) eq ionInt(x))
            assertEquals(x < y, ionInt(x) lt ionInt(y))
            assertEquals(x > y, ionInt(x) gt ionInt(y))
            assertEquals(x <= y, ionInt(x) lte ionInt(y))
            assertEquals(x >= y, ionInt(x) gte ionInt(y))

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
            assertEquals(ionFloat(+x), +ionFloat(x))
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
        assertEquals(ionSymbol(x + y), ionSymbol(x) + y)
    }
}

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
