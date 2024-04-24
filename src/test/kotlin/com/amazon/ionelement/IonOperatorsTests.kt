package com.amazon.ionelement

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.*
import com.amazon.ionelement.util.*
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

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
    fun textPlusOperatorTests() {
        val x = "first test string"
        val y = "second test string"
        assertEquals(ionString(x + y), ionString(x) + y)
        assertEquals(ionString(x + y), ionString(x) + buildString { append(y) })
        assertEquals(ionSymbol(x + y), ionSymbol(x) + y)
        assertEquals(ionSymbol(x + y), ionSymbol(x) + buildString { append(y) })
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
        assertEquals(ionInt(LONG_MAX_BIGINT + LONG_MAX_BIGINT), ionInt(LONG_MAX) + LONG_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT + LONG_MIN_BIGINT), ionInt(LONG_MIN) + LONG_MIN)
        assertEquals(ionInt(LONG_MAX_BIGINT + LONG_MAX_BIGINT), LONG_MAX + ionInt(LONG_MAX))
        assertEquals(ionInt(LONG_MIN_BIGINT + LONG_MIN_BIGINT), LONG_MIN + ionInt(LONG_MIN))
    }

    @Test
    fun longIntElementWithIntPlusOverflowTests() {
        assertEquals(ionInt(LONG_MAX_BIGINT + INT_MAX.toBigInteger()), ionInt(LONG_MAX) + INT_MAX)
        assertEquals(ionInt(INT_MAX.toBigInteger() + LONG_MAX_BIGINT), INT_MAX + ionInt(LONG_MAX))
    }

    @Test
    fun longIntElementWithLongMinusOverflowTests() {
        assertEquals(ionInt(LONG_MIN_BIGINT - LONG_MAX_BIGINT), ionInt(LONG_MIN) - LONG_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT - LONG_MAX_BIGINT), LONG_MIN - ionInt(LONG_MAX))
    }

    @Test
    fun longIntElementWithIntMinusOverflowTests() {
        assertEquals(ionInt(LONG_MIN_BIGINT - INT_MAX.toBigInteger()), ionInt(LONG_MIN) - INT_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT - INT_MAX.toBigInteger()), -INT_MAX + ionInt(LONG_MIN))
    }

    @Test
    fun longIntElementWithLongTimesOverflowTests() {
        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MAX_BIGINT), ionInt(LONG_MAX) * LONG_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT * LONG_MIN_BIGINT), ionInt(LONG_MIN) * LONG_MIN)
        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MIN_BIGINT), ionInt(LONG_MAX) * LONG_MIN)

        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MAX_BIGINT), LONG_MAX * ionInt(LONG_MAX))
        assertEquals(ionInt(LONG_MIN_BIGINT * LONG_MIN_BIGINT), LONG_MIN * ionInt(LONG_MIN))
        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MIN_BIGINT), LONG_MAX * ionInt(LONG_MIN))
    }

    @Test
    fun longIntElementWithIntTimesOverflowTests() {
        assertEquals(ionInt(LONG_MAX_BIGINT * INT_MAX.toBigInteger()), ionInt(LONG_MAX) * INT_MAX)
        assertEquals(ionInt(INT_MAX.toBigInteger() * LONG_MAX_BIGINT), INT_MAX * ionInt(LONG_MAX))
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
        assertEquals(ionInt(1 * 2), 1 * ionInt(2))
    }

    @Test
    fun longIntElementWithLongDivideByZeroTest() {
        assertThrows<ArithmeticException> {
            ionInt(1) / 0
        }
    }

    @Test
    fun bigIntegerIntElementIncOverflowOperatorTest() {
        assertEquals(ionInt(LONG_MAX_BIGINT.inc()), ionInt(LONG_MAX_BIGINT.inc()))
    }

    @Test
    fun bigIntegerIntElementDecOverflowOperatorTest() {
        assertEquals(ionInt(LONG_MIN_BIGINT.dec()), ionInt(LONG_MIN_BIGINT).dec())
    }

    @Test
    fun bigIntegerIntElementWithLongPlusOverflowTests() {
        assertEquals(ionInt(LONG_MAX_BIGINT + LONG_MAX_BIGINT), ionInt(LONG_MAX_BIGINT) + LONG_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT + LONG_MIN_BIGINT), ionInt(LONG_MIN_BIGINT) + LONG_MIN)
        assertEquals(ionInt(LONG_MAX_BIGINT + LONG_MAX_BIGINT), LONG_MAX_BIGINT + ionInt(LONG_MAX))
        assertEquals(ionInt(LONG_MIN_BIGINT + LONG_MIN_BIGINT), LONG_MIN_BIGINT + ionInt(LONG_MIN))
    }

    @Test
    fun bigIntegerIntElementWithIntPlusOverflowTests() {
        assertEquals(ionInt(LONG_MAX_BIGINT + INT_MAX.toBigInteger()), ionInt(LONG_MAX_BIGINT) + INT_MAX)
        assertEquals(ionInt(INT_MAX.toBigInteger() + LONG_MAX_BIGINT), INT_MAX + ionInt(LONG_MAX_BIGINT))
    }

    @Test
    fun bigIntegerIntElementWithLongMinusOverflowTests() {
        assertEquals(ionInt(LONG_MIN_BIGINT - LONG_MAX_BIGINT), ionInt(LONG_MIN) - LONG_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT - LONG_MAX_BIGINT), LONG_MIN - ionInt(LONG_MAX))
    }

    @Test
    fun bigIntegerIntElementWithIntMinusOverflowTests() {
        assertEquals(ionInt(LONG_MIN_BIGINT - INT_MAX.toBigInteger()), ionInt(LONG_MIN) - INT_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT - INT_MAX.toBigInteger()), -INT_MAX + ionInt(LONG_MIN))
    }

    @Test
    fun bigIntegerIntElementWithLongTimesOverflowTests() {
        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MAX_BIGINT), ionInt(LONG_MAX) * LONG_MAX)
        assertEquals(ionInt(LONG_MIN_BIGINT * LONG_MIN_BIGINT), ionInt(LONG_MIN) * LONG_MIN)
        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MIN_BIGINT), ionInt(LONG_MAX) * LONG_MIN)

        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MAX_BIGINT), LONG_MAX * ionInt(LONG_MAX))
        assertEquals(ionInt(LONG_MIN_BIGINT * LONG_MIN_BIGINT), LONG_MIN * ionInt(LONG_MIN))
        assertEquals(ionInt(LONG_MAX_BIGINT * LONG_MIN_BIGINT), LONG_MAX * ionInt(LONG_MIN))
    }

    @Test
    fun bigIntegerIntElementWithIntTimesOverflowTests() {
        assertEquals(ionInt(LONG_MAX_BIGINT * INT_MAX.toBigInteger()), ionInt(LONG_MAX) * INT_MAX)
        assertEquals(ionInt(INT_MAX.toBigInteger() * LONG_MAX_BIGINT), INT_MAX * ionInt(LONG_MAX))
    }

    @Test
    fun bigIntegerIntElementWithLongPlusNoOverflowTests() {
        assertEquals(ionInt(BigInteger.ONE + BigInteger.ONE), ionInt(BigInteger.ONE) + 1L)
        assertEquals(ionInt(BigInteger.ONE + BigInteger.ONE), 1L + ionInt(BigInteger.ONE))
    }

    @Test
    fun bigIntegerIntElementWithLongMinusNoOverflowTests() {
        assertEquals(ionInt(BigInteger.ONE - BigInteger.ONE), ionInt(BigInteger.ONE) - 1L)
        assertEquals(ionInt(BigInteger.ONE - BigInteger.ONE), 1L - ionInt(BigInteger.ONE))
    }

    @Test
    fun bigIntegerIntElementWithLongTimesNoOverflowTests() {
        assertEquals(ionInt(BigInteger.ONE * BigInteger.ONE), ionInt(BigInteger.ONE) * 1L)
        assertEquals(ionInt(BigInteger.ONE * BigInteger.ONE), 1L * ionInt(BigInteger.ONE))
    }

    @Test
    fun bigIntegerIntElementWithLongDivideByZeroTest() {
        assertThrows<ArithmeticException> {
            ionInt(BigInteger.ONE) / 0
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SimpleOperatorTestsArgumentsProvider::class)
    fun simpleOperatorTests(testCase: OperatorTestCase) = testCase.executeTestCase()
    class SimpleOperatorTestsArgumentsProvider : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> = listOf(
            // FloatElement
            UnaryOpTestCase(Random::nextDouble, ::ionFloat, Double::unaryMinus, FE::unaryMinus),
            UnaryOpTestCase(Random::nextDouble, ::ionFloat, Double::inc, FE::inc),
            UnaryOpTestCase(Random::nextDouble, ::ionFloat, Double::dec, FE::dec),
            BinaryOpTestCase(Random::nextDouble, ::ionFloat, Double::plus, FE::plus, Double::plus),
            BinaryOpTestCase(Random::nextDouble, ::ionFloat, Double::minus, FE::minus, Double::minus),
            BinaryOpTestCase(Random::nextDouble, ::ionFloat, Double::times, FE::times, Double::times),
            BinaryOpTestCase(Random::nextDouble, ::ionFloat, Double::div, FE::div, Double::div),
            BinaryOpTestCase(Random::nextDouble, ::ionFloat, Double::rem, FE::rem, Double::rem),
            CmpTestCase(Random::nextDouble, ::ionFloat, Double::equals, FE::eq, Double::eq, FE::eq),
            CmpTestCase(Random::nextDouble, ::ionFloat, { x, y -> x < y }, FE::lt, Double::lt, FE::lt),
            CmpTestCase(Random::nextDouble, ::ionFloat, { x, y -> x > y }, FE::gt, Double::gt, FE::gt),
            CmpTestCase(Random::nextDouble, ::ionFloat, { x, y -> x <= y }, FE::lte, Double::lte, FE::lte),
            CmpTestCase(Random::nextDouble, ::ionFloat, { x, y -> x >= y }, FE::gte, Double::gte, FE::gte),

            // DecimalElement
            UnaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x -> decimal(-x) }, DE::unaryMinus),
            UnaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x -> decimal(x.inc()) }, DE::inc),
            UnaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x -> decimal(x.dec()) }, DE::dec),
            BinaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> decimal(x + y) }, DE::plus, Decimal::plus),
            BinaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> decimal(x - y) }, DE::minus, Decimal::minus),
            BinaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> decimal(x * y) }, DE::times, Decimal::times),
            BinaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> decimal(x / y) }, DE::div, Decimal::div),
            BinaryOpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> decimal(x % y) }, DE::rem, Decimal::rem),
            CmpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> x == y }, DE::eq, Decimal::eq, DE::eq),
            CmpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> x < y }, DE::lt, Decimal::lt, DE::lt),
            CmpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> x > y }, DE::gt, Decimal::gt, DE::gt),
            CmpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> x <= y }, DE::lte, Decimal::lte, DE::lte),
            CmpTestCase(Random::nextDecimal, ::ionDecimal, { x, y -> x >= y }, DE::gte, Decimal::gte, DE::gte),

            // IntElement<Long>
            UnaryOpTestCase(Random::nextLong, ::ionInt, Long::unaryMinus, IE::unaryMinus),
            UnaryOpTestCase(Random::nextLong, ::ionInt, Long::inc, IE::inc),
            UnaryOpTestCase(Random::nextLong, ::ionInt, Long::dec, IE::dec),
            BinaryOpOverflowTestCase(Random::nextLong, ::ionInt, ::plusExact, IE::plus, Long::plus),
            BinaryOpOverflowTestCase(Random::nextLong, ::ionInt, ::minusExact, IE::minus, Long::minus),
            BinaryOpOverflowTestCase(Random::nextLong, ::ionInt, ::timesExact, IE::times, Long::times),
            BinaryOpTestCase(Random::nextLong, ::ionInt, Long::div, IE::div, Long::div),
            BinaryOpTestCase(Random::nextLong, ::ionInt, Long::rem, IE::rem, Long::rem),
            CmpTestCase(Random::nextLong, ::ionInt, Long::equals, IE::eq, Long::eq, IE::eq),
            CmpTestCase(Random::nextLong, ::ionInt, { x, y -> x < y }, IE::lt, Long::lt, IE::lt),
            CmpTestCase(Random::nextLong, ::ionInt, { x, y -> x > y }, IE::gt, Long::gt, IE::gt),
            CmpTestCase(Random::nextLong, ::ionInt, { x, y -> x <= y }, IE::lte, Long::lte, IE::lte),
            CmpTestCase(Random::nextLong, ::ionInt, { x, y -> x >= y }, IE::gte, Long::gte, IE::gte),

            // IntElement<BigInteger>
            UnaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::unaryMinus, IE::unaryMinus),
            UnaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::inc, IE::inc),
            UnaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::dec, IE::dec),
            BinaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::plus, IE::plus, BigInteger::plus),
            BinaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::minus, IE::minus, BigInteger::minus),
            BinaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::times, IE::times, BigInteger::times),
            BinaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::div, IE::div, BigInteger::div),
            BinaryOpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::rem, IE::rem, BigInteger::rem),
            CmpTestCase(Random::nextBigInteger, ::ionInt, BigInteger::equals, IE::eq, BigInteger::eq, IE::eq),
            CmpTestCase(Random::nextBigInteger, ::ionInt, { x, y -> x < y }, IE::lt, BigInteger::lt, IE::lt),
            CmpTestCase(Random::nextBigInteger, ::ionInt, { x, y -> x > y }, IE::gt, BigInteger::gt, IE::gt),
            CmpTestCase(Random::nextBigInteger, ::ionInt, { x, y -> x <= y }, IE::lte, BigInteger::lte, IE::lte),
            CmpTestCase(Random::nextBigInteger, ::ionInt, { x, y -> x >= y }, IE::gte, BigInteger::gte, IE::gte),

            // IntElement<BigInteger> with Int
            BigIntElementMixedOpTestCase(Random::nextInt, BigInteger::plus, Int::plus, IE::plus),
            BigIntElementMixedOpTestCase(Random::nextInt, BigInteger::minus, Int::minus, IE::minus),
            BigIntElementMixedOpTestCase(Random::nextInt, BigInteger::times, Int::times, IE::times),
            BigIntElementMixedOpTestCase(Random::nextInt, BigInteger::div, Int::div, IE::div),
            BigIntElementMixedOpTestCase(Random::nextInt, BigInteger::rem, Int::rem, IE::rem),
            BigIntElementMixedCmpTestCase(Random::nextInt, Int::equals, Int::eq, IE::eq),
            BigIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x < y }, Int::lt, IE::lt),
            BigIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x > y }, Int::gt, IE::gt),
            BigIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x <= y }, Int::lte, IE::lte),
            BigIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x >= y }, Int::gte, IE::gte),

            // IntElement<BigInteger> with Long
            BigIntElementMixedOpTestCase(Random::nextLong, BigInteger::plus, Long::plus, IE::plus),
            BigIntElementMixedOpTestCase(Random::nextLong, BigInteger::minus, Long::minus, IE::minus),
            BigIntElementMixedOpTestCase(Random::nextLong, BigInteger::times, Long::times, IE::times),
            BigIntElementMixedOpTestCase(Random::nextLong, BigInteger::div, Long::div, IE::div),
            BigIntElementMixedOpTestCase(Random::nextLong, BigInteger::rem, Long::rem, IE::rem),
            BigIntElementMixedCmpTestCase(Random::nextLong, Long::equals, Long::eq, IE::eq),
            BigIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x < y }, Long::lt, IE::lt),
            BigIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x > y }, Long::gt, IE::gt),
            BigIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x <= y }, Long::lte, IE::lte),
            BigIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x >= y }, Long::gte, IE::gte),

            // IntElement<Long> with Int
            LongIntElementMixedOpTestCase(Random::nextInt, ::plusExact, Int::plus, IE::plus),
            LongIntElementMixedOpTestCase(Random::nextInt, ::minusExact, Int::minus, IE::minus),
            LongIntElementMixedOpTestCase(Random::nextInt, ::timesExact, Int::times, IE::times),
            LongIntElementMixedOpTestCase(Random::nextInt, ::divExact, Int::div, IE::div),
            LongIntElementMixedOpTestCase(Random::nextInt, ::remExact, Int::rem, IE::rem),
            LongIntElementMixedCmpTestCase(Random::nextInt, Int::equals, Int::eq, IE::eq),
            LongIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x < y }, Int::lt, IE::lt),
            LongIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x > y }, Int::gt, IE::gt),
            LongIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x <= y }, Int::lte, IE::lte),
            LongIntElementMixedCmpTestCase(Random::nextInt, { x, y -> x >= y }, Int::gte, IE::gte),

            // IntElement<Long> with Long
            LongIntElementMixedOpTestCase(Random::nextLong, ::plusExact, Long::plus, IE::plus),
            LongIntElementMixedOpTestCase(Random::nextLong, ::minusExact, Long::minus, IE::minus),
            LongIntElementMixedOpTestCase(Random::nextLong, ::timesExact, Long::times, IE::times),
            LongIntElementMixedOpTestCase(Random::nextLong, ::divExact, Long::div, IE::div),
            LongIntElementMixedOpTestCase(Random::nextLong, ::remExact, Long::rem, IE::rem),
            LongIntElementMixedCmpTestCase(Random::nextLong, Long::equals, Long::eq, IE::eq),
            LongIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x < y }, Long::lt, IE::lt),
            LongIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x > y }, Long::gt, IE::gt),
            LongIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x <= y }, Long::lte, IE::lte),
            LongIntElementMixedCmpTestCase(Random::nextLong, { x, y -> x >= y }, Long::gte, IE::gte),

            // IntElement<BigInteger> with IntElement<Long>
            MixedIntElementCmpTestCase(BigInteger::equals, IE::eq),
            MixedIntElementCmpTestCase({ x, y -> x < y }, IE::lt),
            MixedIntElementCmpTestCase({ x, y -> x > y }, IE::gt),
            MixedIntElementCmpTestCase({ x, y -> x <= y }, IE::lte),
            MixedIntElementCmpTestCase({ x, y -> x >= y }, IE::gte),
        )
    }

    interface OperatorTestCase {
        fun executeTestCase()
    }

    data class UnaryOpTestCase<BaseType, IonType>(
        val argGenerator: () -> BaseType,
        val ionConstructor: (BaseType) -> IonType,
        val baseOp: (BaseType) -> BaseType,
        val unaryOp: (IonType) -> IonType
    ) : OperatorTestCase {
        override fun executeTestCase() {
            repeat(100) {
                val x = argGenerator()
                assertEquals(ionConstructor(baseOp(x)), unaryOp(ionConstructor(x)))
            }
        }
    }

    data class BinaryOpTestCase<BaseType, IonType>(
        val argGenerator: () -> BaseType,
        val ionConstructor: (BaseType) -> IonType,
        val baseOp: (BaseType, BaseType) -> BaseType,
        val leftOp: (IonType, BaseType) -> IonType,
        val rightOp: (BaseType, IonType) -> IonType,
    ) : OperatorTestCase {
        override fun executeTestCase() {
            repeat(100) {
                val x = argGenerator()
                val y = argGenerator()
                val expected = ionConstructor(baseOp(x, y))
                assertEquals(expected, leftOp(ionConstructor(x), y), "failed on arguments: $x and $y")
                assertEquals(expected, rightOp(x, ionConstructor(y)), "failed on arguments: $x and $y")
            }
        }
    }

    data class BinaryOpOverflowTestCase<BaseType, IonType>(
        val argGenerator: () -> BaseType,
        val ionConstructor: (BaseType) -> IonType,
        val exactOp: (BaseType, BaseType) -> IonType,
        val leftOp: (IonType, BaseType) -> IonType,
        val rightOp: (BaseType, IonType) -> IonType,
    ) : OperatorTestCase {
        override fun executeTestCase() {
            repeat(100) {
                val x = argGenerator()
                val y = argGenerator()
                val expected = exactOp(x, y)
                assertEquals(expected, leftOp(ionConstructor(x), y), "failed on arguments: $x and $y")
                assertEquals(expected, rightOp(x, ionConstructor(y)), "failed on arguments: $x and $y")
            }
        }
    }

    data class BigIntElementMixedOpTestCase<BaseType>(
        val argGenerator: () -> BaseType,
        val baseOp: (BigInteger, BigInteger) -> BigInteger,
        val leftOp: (BaseType, IntElement) -> IntElement,
        val rightOp: (IntElement, BaseType) -> IntElement
    ) : OperatorTestCase {
        override fun executeTestCase() {
            repeat(100) {
                val x = argGenerator()
                val xb = (x as? Int)?.toBigInteger() ?: (x as Long).toBigInteger()
                val y = argGenerator()
                val yb = (y as? Int)?.toBigInteger() ?: (y as Long).toBigInteger()
                val actual = ionInt(baseOp(xb, yb))
                assertEquals(actual, leftOp(x, ionInt(yb)), "failed on arguments: $x and $y")
                assertEquals(actual, rightOp(ionInt(xb), y), "failed on arguments: $x and $y")
            }
        }
    }

    data class LongIntElementMixedOpTestCase<BaseType>(
        val argGenerator: () -> BaseType,
        val exactOp: (Long, Long) -> IntElement,
        val leftOp: (BaseType, IntElement) -> IntElement,
        val rightOp: (IntElement, BaseType) -> IntElement
    ) : OperatorTestCase {
        override fun executeTestCase() {
            repeat(100) {
                val x = argGenerator()
                val xl = (x as? Int)?.toLong() ?: x as Long
                val y = argGenerator()
                val yl = (y as? Int)?.toLong() ?: y as Long
                val actual = exactOp(xl, yl)
                assertEquals(actual, leftOp(x, ionInt(yl)), "failed on arguments: $x and $y")
                assertEquals(actual, rightOp(ionInt(xl), y), "failed on arguments: $x and $y")
            }
        }
    }

    data class CmpTestCase<BaseType, IonType>(
        val argGenerator: () -> BaseType,
        val ionConstructor: (BaseType) -> IonType,
        val baseOp: (BaseType, BaseType) -> Boolean,
        val leftOp: (IonType, BaseType) -> Boolean,
        val rightOp: (BaseType, IonType) -> Boolean,
        val bothOp: (IonType, IonType) -> Boolean
    ) : OperatorTestCase {
        override fun executeTestCase() {
            repeat(100) {
                val x = argGenerator()
                val y = with(Random.nextBoolean()) { if (this) x else argGenerator() }
                val expected = baseOp(x, y)
                assertEquals(expected, leftOp(ionConstructor(x), y), "failed on arguments: $x and $y")
                assertEquals(expected, rightOp(x, ionConstructor(y)), "failed on arguments: $x and $y")
                assertEquals(expected, bothOp(ionConstructor(x), ionConstructor(y)), "failed on arguments: $x and $y")
            }
        }
    }

    data class BigIntElementMixedCmpTestCase<BaseType>(
        val argGenerator: () -> BaseType,
        val baseOp: (BaseType, BaseType) -> Boolean,
        val leftOp: (BaseType, IntElement) -> Boolean,
        val rightOp: (IntElement, BaseType) -> Boolean,
    ) : OperatorTestCase {
        override fun executeTestCase() {
            val x = argGenerator()
            val y = with(Random.nextBoolean()) { if (this) x else argGenerator() }
            val xb = (x as? Int)?.toBigInteger() ?: (x as Long).toBigInteger()
            val yb = (y as? Int)?.toBigInteger() ?: (y as Long).toBigInteger()
            assertEquals(baseOp(x, y), leftOp(x, ionInt(yb)), "failed on arguments: $x and $y")
            assertEquals(baseOp(x, y), rightOp(ionInt(xb), y), "failed on arguments: $x and $y")
        }
    }

    data class LongIntElementMixedCmpTestCase<BaseType>(
        val argGenerator: () -> BaseType,
        val baseOp: (BaseType, BaseType) -> Boolean,
        val leftOp: (BaseType, IntElement) -> Boolean,
        val rightOp: (IntElement, BaseType) -> Boolean,
    ) : OperatorTestCase {
        override fun executeTestCase() {
            val x = argGenerator()
            val y = with(Random.nextBoolean()) { if (this) x else argGenerator() }
            val xl = (x as? Int)?.toLong() ?: (x as Long)
            val yl = (y as? Int)?.toLong() ?: (y as Long)
            assertEquals(baseOp(x, y), leftOp(x, ionInt(yl)), "failed on arguments: $x and $y")
            assertEquals(baseOp(x, y), rightOp(ionInt(xl), y), "failed on arguments: $x and $y")
        }
    }

    data class MixedIntElementCmpTestCase(
        val baseOp: (BigInteger, BigInteger) -> Boolean,
        val ionOp: (IntElement, IntElement) -> Boolean,
    ) : OperatorTestCase {
        override fun executeTestCase() {
            val x = Random.nextLong().toBigInteger()
            val y = with(Random.nextBoolean()) { if (this) x.toLong() else Random.nextLong() }
            assertEquals(baseOp(x, y.toBigInteger()), ionOp(ionInt(x), ionInt(y)), "failed on arguments: $x and $y")
            assertEquals(baseOp(y.toBigInteger(), x), ionOp(ionInt(y), ionInt(x)), "failed on arguments: $x and $y")
        }
    }
}

private const val LONG_MAX = Long.MAX_VALUE
private const val LONG_MIN = Long.MIN_VALUE
private const val INT_MAX = Int.MAX_VALUE
private val LONG_MAX_BIGINT = LONG_MAX.toBigInteger()
private val LONG_MIN_BIGINT = LONG_MIN.toBigInteger()

private fun Random.nextBigInteger(): BigInteger = with(this.nextLong()) { this * this }.toBigInteger()
private fun Random.nextDecimal(): Decimal = Decimal.valueOf(this.nextDouble().toBigDecimal())
private fun decimal(x: BigDecimal) = Decimal.valueOf(x)

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

private fun plusExact(x: Long, y: Long) = exactLongOp(x, y, Math::addExact, BigInteger::plus)
private fun minusExact(x: Long, y: Long) = exactLongOp(x, y, Math::subtractExact, BigInteger::minus)
private fun timesExact(x: Long, y: Long) = exactLongOp(x, y, Math::multiplyExact, BigInteger::times)
private fun divExact(x: Long, y: Long) = exactLongOp(x, y, Long::div, BigInteger::div)
private fun remExact(x: Long, y: Long) = exactLongOp(x, y, Long::rem, BigInteger::rem)

private typealias LongOp = (Long, Long) -> Long
private typealias BigOp = (BigInteger, BigInteger) -> BigInteger
