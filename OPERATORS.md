
# `ion-element` Operators

`ion-element` operators is an experimental API that enables the use of some standard operations between Ion values and 
built in types. Using the API requires explicit opt-in. To opt in to the API for a single use, annotate the usage with
`@IonOperatores`. To opt in for an entire module, add the following compiler argument:
```
-opt-in=com.amazon.ionelement.api.IonOperators
```
For example, in Gradle:
```groovy
tasks.withType(KotlinCompile).all {
    kotlinOptions {
        freeCompilerArgs = [
                "-opt-in=com.amazon.ionelement.api.IonOperators"
        ]
    }
}
```

## String Operations
A `SymbolElement` or `StringElement` can be concatenated with a `String` resulting in a new `SymbolElement` or 
`StringElement` with the original's `annotations` and `metas` preserved. For example, each of the following will results
are `true`.
```kotlin
ionString("Hello") + " World!" == ionString("Hello World!")
ionSymbol("Hello", listOf(foo)) + " World!" == ionSymbol("Hello World!", listOf("foo"))
```

## Arithmetic Operations
The arithmetic operators `+`, `-`, `*`, `/` and `%` are defined on the numeric `IonElement` types and their corresponding 
primitive types, each producing an `IonElement` of the same type. The `annotations` and `metas` of the original Ion values
are preserved. For example, the following are all true:
```kotlin
ionInt(1) + 1 == ionInt(2)
ionInt(2) - BigInteger.valueOf(1) == ionInt(BigInteger.valueOf(1))
3.14 * ionFloat(3.14) == ionFloat(9.8596)
ionDecimal(Decimal.valueOf(2.71)) / 3.14 == ionDecimal(Decimal.valueOf(2.71 / 3.14))
``` 

Infix comparison functions `eq`, `lt`, `gt`, `lte`, `gte` are also defined between Ion values of the same type and 
between Ion values and primitive types. For example the following are all true:
```kotlin
ionInt(1) eq ionInt(1)
ionInt(1) lt ionInt(2)
ionFloat(3.14) gt  2.71
ionDecimal(Decimal.valueOf(99)) lte Decimal.valueOf(99)
ionDecimal(Decimal.valueOf(99)) gte ionDecimal(Decimal.valueOf(99))
```
