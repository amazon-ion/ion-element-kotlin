package com.amazon.ionelement

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.*
import java.math.BigInteger
import kotlin.random.Random
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class IonOperatorsTests {
    @Test
    fun boolElementOperatorsTest() {
        assertEquals(ionBool(true), !ionBool(false))
        assertEquals(ionBool(false), !ionBool(true))

        assertEquals(ionBool(true), ionBool(true) and ionBool(true))
        assertEquals(ionBool(false), ionBool(true) and ionBool(false))
        assertEquals(ionBool(false), ionBool(false) and ionBool(true))
        assertEquals(ionBool(false), ionBool(false) and ionBool(false))

        assertEquals(ionBool(true), ionBool(true) or ionBool(true))
        assertEquals(ionBool(true), ionBool(true) or ionBool(false))
        assertEquals(ionBool(true), ionBool(false) or ionBool(true))
        assertEquals(ionBool(false), ionBool(false) or ionBool(false))

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
        assertEquals(ionInt(x.toBigInteger() + x.toBigInteger()), ionInt(x) + ionInt(x))
        assertEquals(ionInt(y.toBigInteger() + y.toBigInteger()), ionInt(y) + ionInt(y))
        assertEquals(ionInt(y.toBigInteger() - x.toBigInteger()), ionInt(y) - ionInt(x))
        assertEquals(ionInt(x.toBigInteger() * x.toBigInteger()), ionInt(x) * ionInt(x))
        assertEquals(ionInt(y.toBigInteger() * y.toBigInteger()), ionInt(y) * ionInt(y))
        assertEquals(ionInt(x.toBigInteger() * y.toBigInteger()), ionInt(x) * ionInt(y))

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
        assertEquals(ionInt(x + y), ionInt(x) + ionInt(y))
        assertEquals(ionInt(x - y), ionInt(x) - ionInt(y))
        assertEquals(ionInt(x * y), ionInt(x) * ionInt(y))
        assertEquals(ionInt(x / y), ionInt(x) / ionInt(y))
        assertEquals(ionInt(x % y), ionInt(x) % ionInt(y))

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

            assertEquals(exactLongOp(x, y, Math::addExact, BigInteger::add), ionInt(x) + ionInt(y))
            assertEquals(exactLongOp(x, y, Math::subtractExact, BigInteger::minus), ionInt(x) - ionInt(y))
            assertEquals(exactLongOp(x, y, Math::multiplyExact, BigInteger::times), ionInt(x) * ionInt(y))
            assertEquals(ionInt(x / y), ionInt(x) / ionInt(y))
            assertEquals(ionInt(x % y), ionInt(x) % ionInt(y))

            assertEquals(x < y, ionInt(x) < ionInt(y))
            assertEquals(x > y, ionInt(x) > ionInt(y))
            assertEquals(x <= y, ionInt(x) <= ionInt(y))
            assertEquals(x >= y, ionInt(x) >= ionInt(y))

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
            assertEquals(x < y, ionInt(x) < y)
            assertEquals(x > y, ionInt(x) > y)
            assertEquals(x <= y, ionInt(x) <= y)
            assertEquals(x >= y, ionInt(x) >= y)

            assertEquals(x < y, x < ionInt(y))
            assertEquals(x > y, x > ionInt(y))
            assertEquals(x <= y, x <= ionInt(y))
            assertEquals(x >= y, x >= ionInt(y))

            // Mixed-type Int
            assertEquals(x < z, ionInt(x) < z)
            assertEquals(x > z, ionInt(x) > z)
            assertEquals(x <= z, ionInt(x) <= z)
            assertEquals(x >= z, ionInt(x) >= z)

            assertEquals(z < y, z < ionInt(y))
            assertEquals(z > y, z > ionInt(y))
            assertEquals(z <= y, z <= ionInt(y))
            assertEquals(z >= y, z >= ionInt(y))
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

            assertEquals(ionInt(x + y), ionInt(x) + ionInt(y))
            assertEquals(ionInt(x - y), ionInt(x) - ionInt(y))
            assertEquals(ionInt(x * y), ionInt(x) * ionInt(y))
            assertEquals(ionInt(x / y), ionInt(x) / ionInt(y))
            assertEquals(ionInt(x % y), ionInt(x) % ionInt(y))

            assertEquals(x < y, ionInt(x) < ionInt(y))
            assertEquals(x > y, ionInt(x) > ionInt(y))
            assertEquals(x <= y, ionInt(x) <= ionInt(y))
            assertEquals(x >= y, ionInt(x) >= ionInt(y))

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

            assertEquals(x < y, x < ionInt(y))
            assertEquals(x > y, x > ionInt(y))
            assertEquals(x <= y, x <= ionInt(y))
            assertEquals(x >= y, x >= ionInt(y))

            assertEquals(x < y, ionInt(x) < y)
            assertEquals(x > y, ionInt(x) > y)
            assertEquals(x <= y, ionInt(x) <= y)
            assertEquals(x >= y, ionInt(x) >= y)
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

            assertEquals(ionDecimal(Decimal.valueOf(x + y)), ionDecimal(x) + ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x - y)), ionDecimal(x) - ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x * y)), ionDecimal(x) * ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x / y)), ionDecimal(x) / ionDecimal(y))
            assertEquals(ionDecimal(Decimal.valueOf(x % y)), ionDecimal(x) % ionDecimal(y))

            assertEquals(x < y, ionDecimal(x) < ionDecimal(y))
            assertEquals(x > y, ionDecimal(x) > ionDecimal(y))
            assertEquals(x <= y, ionDecimal(x) <= ionDecimal(y))
            assertEquals(x >= y, ionDecimal(x) >= ionDecimal(y))

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

            assertEquals(x < y, x < ionDecimal(y))
            assertEquals(x > y, x > ionDecimal(y))
            assertEquals(x <= y, x <= ionDecimal(y))
            assertEquals(x >= y, x >= ionDecimal(y))

            assertEquals(x < y, ionDecimal(x) < y)
            assertEquals(x > y, ionDecimal(x) > y)
            assertEquals(x <= y, ionDecimal(x) <= y)
            assertEquals(x >= y, ionDecimal(x) >= y)
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

            assertEquals(ionFloat(x + y), ionFloat(x) + ionFloat(y))
            assertEquals(ionFloat(x - y), ionFloat(x) - ionFloat(y))
            assertEquals(ionFloat(x * y), ionFloat(x) * ionFloat(y))
            assertEquals(ionFloat(x / y), ionFloat(x) / ionFloat(y))
            assertEquals(ionFloat(x % y), ionFloat(x) % ionFloat(y))

            assertEquals(x < y, ionFloat(x) < ionFloat(y))
            assertEquals(x > y, ionFloat(x) > ionFloat(y))
            assertEquals(x <= y, ionFloat(x) <= ionFloat(y))
            assertEquals(x >= y, ionFloat(x) >= ionFloat(y))

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

            assertEquals(x < y, x < ionFloat(y))
            assertEquals(x > y, x > ionFloat(y))
            assertEquals(x <= y, x <= ionFloat(y))
            assertEquals(x >= y, x >= ionFloat(y))

            assertEquals(x < y, ionFloat(x) < y)
            assertEquals(x > y, ionFloat(x) > y)
            assertEquals(x <= y, ionFloat(x) <= y)
            assertEquals(x >= y, ionFloat(x) >= y)
        }
    }
}

private fun exactLongOp(x: Long, y: Long, longOp: LongOp, bigOp: BigOp): IntElement =
    try {
        ionInt(longOp(x, y))
    } catch (e: ArithmeticException) {
        ionInt(bigOp(x.toBigInteger(), y.toBigInteger()))
    }

private typealias LongOp = (Long, Long) -> Long
private typealias BigOp = (BigInteger, BigInteger) -> BigInteger
