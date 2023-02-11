package com.amazon.ionelement.api

import com.amazon.ion.Decimal
import com.amazon.ionelement.api.IntElementSize.*
import java.math.BigDecimal
import java.math.BigInteger

/*
 * This file contains operator overloads and associated extension functions for various IonElement subclasses.
 * Infix operators that mix IonElement types with native types are only defined for IonElement on the left-hand side.
 * Augmented assignment operators, e.g., '+=' are provided by Kotlin automatically from the associated binary operator.
 */

/* Type aliases for readability */
private typealias BE = BoolElement
private typealias IE = IntElement
private typealias DE = DecimalElement
private typealias FE = FloatElement

/* BoolElement unary operators */
public operator fun BE.not(): BE = ionBool(!this.booleanValue)

/* BoolElement with BoolElement operators */
public infix fun BE.and(other: BE): BE = ionBool(this.booleanValue && other.booleanValue)
public infix fun BE.or(other: BE): BE = ionBool(this.booleanValue || other.booleanValue)

/* BoolElement with Boolean operators */
public infix fun BE.and(other: Boolean): BE = ionBool(this.booleanValue && other)
public infix fun BE.or(other: Boolean): BE = ionBool(this.booleanValue || other)

/* Boolean with BoolElement operators */
public infix fun Boolean.and(other: BE): BE = ionBool(this && other.booleanValue)
public infix fun Boolean.or(other: BE): BE = ionBool(this || other.booleanValue)

/* IntElement unary operators (unaryPlus omitted as bigInteger does not support it) */
public operator fun IE.unaryMinus(): IE = if (useLong(this)) ionInt(-this.longValue) else ionInt(-this.bigIntegerValue)
public operator fun IE.inc(): IE =
    if (useLong(this)) try {
        ionInt(Math.incrementExact(this.longValue))
    } catch (e: ArithmeticException) {
        ionInt(this.bigIntegerValue.inc())
    }
    else ionInt(this.bigIntegerValue.inc())

public operator fun IE.dec(): IE =
    if (useLong(this)) try {
        ionInt(Math.decrementExact(this.longValue))
    } catch (e: ArithmeticException) {
        ionInt(this.bigIntegerValue.dec())
    }
    else ionInt(this.bigIntegerValue.dec())

/* IntElement with IntElement binary operators */
public operator fun IE.plus(other: IE): IE = intOp(this, other, Math::addExact, BigInteger::plus)
public operator fun IE.minus(other: IE): IE = intOp(this, other, Math::subtractExact, BigInteger::minus)
public operator fun IE.times(other: IE): IE = intOp(this, other, Math::multiplyExact, BigInteger::times)
public operator fun IE.div(other: IE): IE = intOp(this, other, Long::div, BigInteger::div)
public operator fun IE.rem(other: IE): IE = intOp(this, other, Long::rem, BigInteger::rem)
public operator fun IE.compareTo(other: IE): Int = intCmp(this, other)

/* IntElement with Int binary operators */
public operator fun IE.plus(other: Int): IE = intOp(this, other, Math::addExact, BigInteger::plus)
public operator fun IE.minus(other: Int): IE = intOp(this, other, Math::subtractExact, BigInteger::minus)
public operator fun IE.times(other: Int): IE = intOp(this, other, Math::multiplyExact, BigInteger::times)
public operator fun IE.div(other: Int): IE = intOp(this, other, Long::div, BigInteger::div)
public operator fun IE.rem(other: Int): IE = intOp(this, other, Long::rem, BigInteger::rem)
public operator fun IE.compareTo(other: Int): Int = intCmp(this, other.toLong())

/* IntElement with Long binary operators */
public operator fun IE.plus(other: Long): IE = intOp(this, other, Math::addExact, BigInteger::plus)
public operator fun IE.minus(other: Long): IE = intOp(this, other, Math::subtractExact, BigInteger::minus)
public operator fun IE.times(other: Long): IE = intOp(this, other, Math::multiplyExact, BigInteger::times)
public operator fun IE.div(other: Long): IE = intOp(this, other, Long::div, BigInteger::div)
public operator fun IE.rem(other: Long): IE = intOp(this, other, Long::rem, BigInteger::rem)
public operator fun IE.compareTo(other: Long): Int = intCmp(this, other)

/* IntElement with BigInteger binary operators */
public operator fun IE.plus(other: BigInteger): IE = ionInt(this.bigIntegerValue + other)
public operator fun IE.minus(other: BigInteger): IE = ionInt(this.bigIntegerValue - other)
public operator fun IE.times(other: BigInteger): IE = ionInt(this.bigIntegerValue * other)
public operator fun IE.div(other: BigInteger): IE = ionInt(this.bigIntegerValue / other)
public operator fun IE.rem(other: BigInteger): IE = ionInt(this.bigIntegerValue % other)
public operator fun IE.compareTo(other: BigInteger): Int = this.bigIntegerValue.compareTo(other)

/* Int with IntElement binary operators */
public operator fun Int.plus(other: IE): IE = intOp(this, other, Math::addExact, BigInteger::plus)
public operator fun Int.minus(other: IE): IE = intOp(this, other, Math::subtractExact, BigInteger::minus)
public operator fun Int.times(other: IE): IE = intOp(this, other, Math::multiplyExact, BigInteger::times)
public operator fun Int.div(other: IE): IE = intOp(this, other, Long::div, BigInteger::div)
public operator fun Int.rem(other: IE): IE = intOp(this, other, Long::rem, BigInteger::rem)
public operator fun Int.compareTo(other: IntElement): Int = intCmp(this.toLong(), other)

/* Long with IntElement binary operators */
public operator fun Long.plus(other: IE): IE = intOp(this, other, Math::addExact, BigInteger::plus)
public operator fun Long.minus(other: IE): IE = intOp(this, other, Math::subtractExact, BigInteger::minus)
public operator fun Long.times(other: IE): IE = intOp(this, other, Math::multiplyExact, BigInteger::times)
public operator fun Long.div(other: IE): IE = intOp(this, other, Long::div, BigInteger::div)
public operator fun Long.rem(other: IE): IE = intOp(this, other, Long::rem, BigInteger::rem)
public operator fun Long.compareTo(other: IntElement): Int = intCmp(this, other)

/* BigInteger with IntElement binary operators */
public operator fun BigInteger.plus(other: IE): IE = ionInt(this + other.bigIntegerValue)
public operator fun BigInteger.minus(other: IE): IE = ionInt(this - other.bigIntegerValue)
public operator fun BigInteger.times(other: IE): IE = ionInt(this * other.bigIntegerValue)
public operator fun BigInteger.div(other: IE): IE = ionInt(this / other.bigIntegerValue)
public operator fun BigInteger.rem(other: IE): IE = ionInt(this % other.bigIntegerValue)
public operator fun BigInteger.compareTo(other: IE): Int = this.compareTo(other.bigIntegerValue)

/* DecimalElement unary operators (unaryPlus() omitted as BigDecimal does not support it) */
public operator fun DE.unaryMinus(): DE = ionDecimal(Decimal.valueOf(-this.decimalValue))
public operator fun DE.inc(): DE = ionDecimal(Decimal.valueOf(this.decimalValue.inc()))
public operator fun DE.dec(): DE = ionDecimal(Decimal.valueOf(this.decimalValue.dec()))

/* DecimalElement with DecimalElement operators */
public operator fun DE.plus(other: DE): DE = ionDecimal(Decimal.valueOf(this.decimalValue + other.decimalValue))
public operator fun DE.minus(other: DE): DE = ionDecimal(Decimal.valueOf(this.decimalValue - other.decimalValue))
public operator fun DE.times(other: DE): DE = ionDecimal(Decimal.valueOf(this.decimalValue * other.decimalValue))
public operator fun DE.div(other: DE): DE = ionDecimal(Decimal.valueOf(this.decimalValue / other.decimalValue))
public operator fun DE.rem(other: DE): DE = ionDecimal(Decimal.valueOf(this.decimalValue % other.decimalValue))
public operator fun DE.compareTo(other: DE): Int = this.decimalValue.compareTo(other.decimalValue)

/* DecimalElement with BigDecimal operators */
public operator fun DE.plus(other: BigDecimal): DE = ionDecimal(Decimal.valueOf(this.decimalValue + other))
public operator fun DE.minus(other: BigDecimal): DE = ionDecimal(Decimal.valueOf(this.decimalValue - other))
public operator fun DE.times(other: BigDecimal): DE = ionDecimal(Decimal.valueOf(this.decimalValue * other))
public operator fun DE.div(other: BigDecimal): DE = ionDecimal(Decimal.valueOf(this.decimalValue / other))
public operator fun DE.rem(other: BigDecimal): DE = ionDecimal(Decimal.valueOf(this.decimalValue % other))
public operator fun DE.compareTo(other: BigDecimal): Int = this.decimalValue.compareTo(other)

/* BigDecimal with DecimalElement operators */
public operator fun BigDecimal.plus(other: DE): DE = ionDecimal(Decimal.valueOf(this + other.decimalValue))
public operator fun BigDecimal.minus(other: DE): DE = ionDecimal(Decimal.valueOf(this - other.decimalValue))
public operator fun BigDecimal.times(other: DE): DE = ionDecimal(Decimal.valueOf(this * other.decimalValue))
public operator fun BigDecimal.div(other: DE): DE = ionDecimal(Decimal.valueOf(this / other.decimalValue))
public operator fun BigDecimal.rem(other: DE): DE = ionDecimal(Decimal.valueOf(this % other.decimalValue))
public operator fun BigDecimal.compareTo(other: DE): Int = this.compareTo(other.decimalValue)

/* DecimalElement unary operators */
public operator fun FE.unaryMinus(): FE = ionFloat(-this.doubleValue)
public operator fun FE.unaryPlus(): FE = ionFloat(+this.doubleValue)
public operator fun FE.inc(): FE = ionFloat(this.doubleValue.inc())
public operator fun FE.dec(): FE = ionFloat(this.doubleValue.dec())

/* FloatElement with FloatElement operators */
public operator fun FE.plus(other: FE): FE = ionFloat(this.doubleValue + other.doubleValue)
public operator fun FE.minus(other: FE): FE = ionFloat(this.doubleValue - other.doubleValue)
public operator fun FE.times(other: FE): FE = ionFloat(this.doubleValue * other.doubleValue)
public operator fun FE.div(other: FE): FE = ionFloat(this.doubleValue / other.doubleValue)
public operator fun FE.rem(other: FE): FE = ionFloat(this.doubleValue % other.doubleValue)
public operator fun FE.compareTo(other: FE): Int = this.doubleValue.compareTo(other.doubleValue)

/* FloatElement with Double operators */
public operator fun FE.plus(other: Double): FE = ionFloat(this.doubleValue + other)
public operator fun FE.minus(other: Double): FE = ionFloat(this.doubleValue - other)
public operator fun FE.times(other: Double): FE = ionFloat(this.doubleValue * other)
public operator fun FE.div(other: Double): FE = ionFloat(this.doubleValue / other)
public operator fun FE.rem(other: Double): FE = ionFloat(this.doubleValue % other)
public operator fun FE.compareTo(other: Double): Int = this.doubleValue.compareTo(other)

/* Double with FloatElement operators */
public operator fun Double.plus(other: FloatElement): FE = ionFloat(this + other.doubleValue)
public operator fun Double.minus(other: FloatElement): FE = ionFloat(this - other.doubleValue)
public operator fun Double.times(other: FloatElement): FE = ionFloat(this * other.doubleValue)
public operator fun Double.div(other: FloatElement): FE = ionFloat(this / other.doubleValue)
public operator fun Double.rem(other: FloatElement): FE = ionFloat(this % other.doubleValue)
public operator fun Double.compareTo(other: FloatElement): Int = this.compareTo(other.doubleValue)

/* Integer operator helpers */
private fun useLong(x: IE): Boolean = x.integerSize == LONG
private fun useLong(x: IE, y: IE): Boolean = x.integerSize == LONG && y.integerSize == LONG
private fun intOp(x: IE, y: IE, longOp: (Long, Long) -> Long, bigOp: (BigInteger, BigInteger) -> BigInteger): IE =
    if (useLong(x, y)) try {
        ionInt(longOp(x.longValue, y.longValue))
    } catch (e: ArithmeticException) {
        ionInt(bigOp(x.bigIntegerValue, y.bigIntegerValue))
    }
    else ionInt(bigOp(x.bigIntegerValue, y.bigIntegerValue))

private fun intOp(x: IE, y: Long, longOp: (Long, Long) -> Long, bigOp: (BigInteger, BigInteger) -> BigInteger): IE {
    return if (useLong(x)) {
        try {
            ionInt(longOp(x.longValue, y))
        } catch (e: ArithmeticException) {
            ionInt(bigOp(x.bigIntegerValue, y.toBigInteger()))
        }
    } else {
        ionInt(bigOp(x.bigIntegerValue, y.toBigInteger()))
    }
}

private fun intOp(x: Long, y: IE, longOp: (Long, Long) -> Long, bigOp: (BigInteger, BigInteger) -> BigInteger): IE {
    return if (useLong(y)) {
        try {
            ionInt(longOp(x, y.longValue))
        } catch (e: ArithmeticException) {
            ionInt(bigOp(x.toBigInteger(), y.bigIntegerValue))
        }
    } else {
        ionInt(bigOp(x.toBigInteger(), y.bigIntegerValue))
    }
}

private fun intOp(x: IE, y: Int, longOp: (Long, Long) -> Long, bigOp: (BigInteger, BigInteger) -> BigInteger): IE {
    return intOp(x, y.toLong(), longOp, bigOp)
}

private fun intOp(x: Int, y: IE, longOp: (Long, Long) -> Long, bigOp: (BigInteger, BigInteger) -> BigInteger): IE {
    return intOp(x.toLong(), y, longOp, bigOp)
}

private fun intCmp(x: IE, y: IE): Int {
    return if (useLong(x, y)) {
        x.longValue.compareTo(y.longValue)
    } else {
        x.bigIntegerValue.compareTo(y.bigIntegerValue)
    }
}

private fun intCmp(x: IE, y: Long): Int {
    return if (useLong(x)) {
        x.longValue.compareTo(y)
    } else {
        x.bigIntegerValue.compareTo(y.toBigInteger())
    }
}

private fun intCmp(x: Long, y: IE): Int {
    return if (useLong(y)) {
        x.compareTo(y.longValue)
    } else {
        x.toBigInteger().compareTo(y.bigIntegerValue)
    }
}
