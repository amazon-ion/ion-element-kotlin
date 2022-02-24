![Build Pipeline](https://github.com/amzn/ion-element-kotlin/workflows/Build%20Pipeline/badge.svg)

# `ion-element`

`ion-element` is an immutable in-memory representation of [Amazon Ion](http://amzn.github.io/ion-docs/) which has an API
that is idiomatic to Kotlin and is also usable from Java. It is meant as an alternative to `IonValue` from
[Ion-java](https://github.com/amzn/ion-java) but is not a complete implementation of Ion as it relies on `Ion-java`'s
`IonReader` and `IonWriter` interfaces for reading and writing Ion data.

## Status

This library is complete and mature, however the public API should be considered unstable.

## Why is `IonElement` Needed?

### `IonValue.clone()`

While manipulating the `IonValue` instances of `ion-java`, they often need to be deeply copied, which can be extremely
expensive for large data structures. For instance, a single `IonValue` instance cannot reside in multiple `IonContainer`
instances. If this is desired, one must call `IonValue.clone()`. Depending on how the size of `IonValue` instances and
how they are manipulated, the cost of calling `IonValue.clone()` can quickly add up causing severe performance
degradation.

`IonValue` must be cloned before being added to a second container because they...

- ... are mutable. (Mutating a value contained within multiple containers would be very confusing and lead to countless
  bugs.)
- ... keep a reference to the parent container that they reside in.
- ... may contain symbol ids, which can vary depending on the current Ion symbol table.
- ... know which `IonSystem` they originated from.

None of the above is true about instances of `IonElement`.  `IonElement` instances never need to be cloned.
`IonElement` intentionally does not provide any method of performing a deep copy.

This example works perfectly fine with `IonElement` instances:

```Kotlin
val listElement = loadSingleElement("[{ some_potentially_big_container: 42}]").asList();
val newListElement = ionListOf(listElement.values[0]) // <--  no need to call .clone().
println(newListElement)
// Output is [{ some_potentially_big_container: 42}]
```

But the equivalent with `IonValue` requires a call to `.clone()`:

```Kotlin
val ion = IonSystemBuilder.standard().build()
val ionList = ion.singleValue("[{ some_potentially_big_container: 42}]") as IonList
// Removal of .clone() below results in com.amazon.ion.ContainedValueException.
val newList = ion.newList(ionList[0].clone())
println(newList)
// Output is [{ some_potentially_big_container: 42}]
```

### `IonValue` Down-Casting

In order to access the value of an `IonValue`, one must first check to make sure it is safe to down-cast, and then
down-cast it to the appropriate type, but `ion-element` provides the interface `AnyElement` which conveniently performs
the type before returning the down-casted value. Example:

```Kotlin
val elem: AnyElement = loadSingleElement("\"What are you doing, Dave?\"")
println("The string was: ${elem.stringValue}")
```

This is contrast to the equivalent code using `IonValue` here:

```Kotlin
val ion = IonSystemBuilder.standard().build()
val ionValue = ion.singleValue("\"What are you doing, Dave?\"")

// Check type of IonValue first to ensure it's safe to cast to `IonString`
if (ionValue.type == IonType.STRING) {
    val ionStr = ionValue as IonString // <-- Down cast here
    println("The string was: ${ionStr.stringValue()}")
} else {
    error("Expected an Ion string, got an IonType.${ionValue.type}");
}
```

## Anyone may implement the `IonElement` interface and its sub-interfaces

Even though it is an interface, it is not possible to implement `ion-java`'s `IonValue` yourself because `ion-java`
explicitly forbids it.

This library places no such constraints on the implementations of `IonElement`. As long as the behavior is implemented
correctly (see the documentation of each interface), the implementations of `IonElement` provided by this library are
fully interoperable with user supplied implementations.

## Inheritance Hierarchy of `IonElement` Sub-Interfaces

The inheritance hierarchy of `IonElement` and its sub-interfaces closely mirrors that of `IonValue` except for the
addition of `AnyElement`, which provides value accessors and safe down-casting functions. Also, there is no
sub-interface specifically for Ion null values because they require no member functions (such as value getters) in
addition to those which are already defined on `IonElement`. A sub-interface for null values would therefore be
redundant.

The `IonElement` and `AnyElement` interfaces are described as "widened" while the type specific interfaces are
"narrowed". This is in reference to the range of possible data types representable by these interfaces.

- `IonElement`
    - `AnyElement`
    - `BoolElement`
    - `IntElement`
    - `FloatElement`
    - `DecimalElement`
    - `TimestampElement`
    - `TextElement`
        - `StringElement`
        - `SymbolElement`
    - `LobElement`
        - `BlobElement`
        - `ClobElement`
    - `ContainerElement`
        - `SeqElement`
            - `ListElement`
            - `SexpElement`
        - `StructElement`

## `AnyElement`

`AnyElement` is a special sub-interface of `IonElement` that can be used by consumers of this library to reduce the
overhead of accessing JVM native versions of the values represented by an `IonElement`. This is accomplished with two
categories of accessor functions that perform type checking before returning a down-casted `IonElement` instance or
Kotlin native value. As shown below, `AnyElement.as*[OrNull]()` functions perform type checking, throw an exception in
the event of a type mismatch, and down-cast to the narrowed `IonElement` sub-interface.  `AnyElement.*Value[OrNull]`
properties are similar but return JVM native values directly.

The `IonElement.asAnyElement()` interface exists to convert to `AnyElement`. Although in `IonElement` implementations
this is implemented as a simple downcast (i.e. `this as AnyElement`), implementations of `IonElement` are free to
implement this however they see fit.

An example using these functions is shown below:

```Kotlin
val stockItemIonText = """
    stock_item::{
        name: "Fantastic Widget",
        price: 12.34,
        countInStock: 2,
        orders: [
            { customerId: 123, state: WA },
            { customerId: 456, state: "HI" }
        ]
    }
"""

val stockItem: StockItem = try {
    val stockElement: StructElement = loadSingleElement(stockItemIonText).asStruct()
    StockItem(
        stockElement["name"].textValue,
        stockElement["price"].decimalValue,
        stockElement["countInStock"].longValue,
        stockElement["orders"].listValues
            .map { order -> order.asStruct() }
            .map { order ->
                Order(
                    order["customerId"].longValue,
                    order["state"].textValue
                )
            }
    )
} catch (ex: IonElementConstraintException) {
    error("Malformed StockItem: ${ex.message}");
}
```

More details about these functions are included below.

## `AnyElement`'s `*Value[OrNull]` Properties

In order to reduce the need to downcast to the appropriate sub-interface of `IonElement`, `AnyElement` provides two
read-only properties per type of Ion data: `<kotlinType>Value` and `<kotilnType>ValueOrNull`, where `<jvmType>` is the
JVM Kotlin equivalent of the corresponding Ion type. Each of these properties will check to if the Ion type is the
correct for the property called, and if not, will throw `IonElementConstraintException` to indicate that the element is
of an unexpected type.

Here is an example using `longValue` and `stringValue`

```Kotlin
val anInteger: AnyElement = loadSingleElement("42")

// Prints 42
println(anInteger.longValue)

// throws IonElementConstraintException
anInteger.stringValue
```

If the `AnyElement` instance contains an Ion null value, an `IonElementConstraintException` is also thrown. These should
be used when the application wishes to require elements to be non-null.

```Kotlin
val nullElement: AnyElement = loadSingleElement("null")
val typedNullElement: AnyElement = loadSingleElement("null.int")

// Both throw IonElementConstraintException
nullElement.longValue
typedNullElement.longValue
```

Each of `*Value` properties has a corresponding `*ValueOrNull`, which converts Ion null values to a Kotlin `null`. These
should be used when the application wishes to allow elements to be non-null.

```Kotlin
val nullElement: AnyElement = loadSingleElement("null")
println(nullElement.longValueOrNull)
val typedNullElement: AnyElement = loadSingleElement("null.int")
println(typedNullElement.longValueOrNull)
```

The `*Value` functions include a null-check so the application doesn't have to, additionally this takes advantage
of [Kotlin's null safety](https://kotlinlang.org/docs/null-safety.html), since the values returned from these functions
are guaranteed to be non-null. For values that *might* legitimately be null,

### `AnyElement`'s `as*[OrNull]()` Functions

`ion-element` greatly reduces the need for down-casting, but as shown above it does not does not completely

## `IonElement` API Guidelines

The following guidelines are applied to the API of this package and are good practices to follow for any API accepting
or returning `IonElement` types or its sub-types.

- Any function arguments accepting...
    - ... any type of Ion data should be of type `IonElement` and not of type `AnyElement` to avoid requiring the use
      of `IonElement.asAnyElement()` at the call-site.
    - ... a specific type of Ion data should use the narrowest possible `IonElement` sub-interface, for instance:
        - A function argument requiring an Ion s-expression should be of type `SexpElement`.
        - A function argument allowing either a list *or* an s-expression should be of type `ContainerElement`, which is
          the narrowest type that can represent either.
- Any functions that may return...
    - ... any type of Ion data should have the `AnyElement` return type to avoid forcing the caller to down cast to a
      narrower sub-interface or `AnyElement`.
    - ... more than one type of Ion data should have the narrowest possible return type, for instance:
        - A function that will only ever return s-expressions should have the `SexpElement` return type.
        - A function that will return lists or s-expressions should have the `ContainerElement` return type.

## `IonElement` Constructors

`IonElement` instances are created without any `IonSystem` or other factory object. Instead, `IonElement` instances are
created by a series of top-level Kotlin functions (`Ion.*` static functions from Java). Not all `IonElement`
constructor functions are shown here. Please see [Ion.kt](src/com/amazon/ionelement/api/Ion.kt) for a complete list.

Some examples are below:

```kotlin
val greeting = ionString("Hello world!")
println(greeting) // "Hello world!"

val ultimateAnswer = ionInt(42)
println(ultimateAnswer) // 42

val listOfInts = ionListOf((1L..10L).map { ionInt(it) })
println(listOfInts) // [1,2,3,4,5,6,7,8,9,10]

val anotherList = ionListOf(
    greeting,
    ultimateAnswer,
    listOfInts,
    annotations = listOf("yolo", "i_love_ion")
)
println(anotherList) // yolo::i_love_ion::["Hello world!",42,[1,2,3,4,5,6,7,8,9,10]]

val person = ionStructOf(
    "firstName" to ionString("Frédéric"),
    "lastName" to ionString("Chopin"),
    "birthdate" to ionTimestamp("1810-02-22")
)
println(person) // {firstName:"Frédéric",lastName:"Chopin",birthdate:1810-02-22})
```

## `IonElement` and Annotations

Each `IonElement` constructor has an optional `annotations: List<String>` which may be used to specify annotations.
Annotations may also be added to an existing `IonElement` with the `IonElement.withAnnotations()` and annotations
removed with the `IonElement.withoutAnnotations()` extensions functions. Both functions treat the
`IonElement` as a persistent data structure; that is: they return a modified shallow copy of the original.

```Kotlin
// Creating a new `IonElement` instance with a list annotations:
val ionInt = ionInt(42, annotations = listOf("meaning_of_life"))

// Prints meaning_of_life::42
println(ionInt)

// Adding values to an existing instance
val ionInt2 = ionInt(42)
val ionInt3 = ionInt2.withAnnotations("meaning", "of", "life")

// Prints meaning::of::life::42
println(ionInt3)

// Removing annotations from an existing instance
val ionInt4 = ionInt3.withoutAnnotations()

// Prints 42
println(ionInt4) 
```

## Loading Ion Data into `IonElement`

Several ways exist to load Ion data from an `com.amazon.ion.IonReader` instance or from a `String`.

#### `loadSingleElement`

Accepts either a `String` or `IonReader`, and loads the first value found into an `AnyElement` instance.  
Throws an exception if there is more than one top level value, which is useful when a guarantee that there is only one
value present is needed.

Signatures:

```Kotlin
fun loadSingleElement(ionText: String, options: IonElementLoaderOptions = IonElementLoaderOptions())
fun loadSingleElement(ionReader: IonReader, options: IonElementLoaderOptions = IonElementLoaderOptions())
```

Example:

```Kotlin
val element: AnyElement = loadSingleElement("{ some_field: 42 }")
```

#### `loadCurrentElement`

Loads the entire current top-level Ion value from an `IonReader`. Unlike `loadSingleElement`, does not throw an 
exception if another top-level value remains in the `IonReader`.  Steps in to and out as needed for container values, 
but does *not* advance the reader to the next top level value. 

Signatures:

```Kotlin
fun loadCurrentElement(ionReader: IonReader, options: IonElementLoaderOptions = IonElementLoaderOptions()): AnyElement
```

Example:

```Kotlin
IonReaderBuilder.standard().build("1 2").use { reader ->
    while(reader.next() != null) {
        println(loadCurrentElement(reader))
    }
}
```

#### `loadAllElements`

This is an alternative to `loadCurrentElement`.  Returns an iterator that reads one top-level value at a time.

Signatures:

```Kotlin
fun loadAllElements(ionText: String, options: IonElementLoaderOptions = IonElementLoaderOptions()): Iterable<AnyElement>
fun loadAllElements(ionReader: IonReader, options: IonElementLoaderOptions = IonElementLoaderOptions()): Iterable<AnyElement>
```

```Kotlin
val allElementsIterator: Iterable<AnyElement> = loadAllElements("1 2")
allElementsIterator.forEach { elem: AnyElement ->
    println(elem)
}
```

#### `IonElementLoader` and Dependency Injection

All the above functions are also present on the `IonElementLoader` interface, which can easily be mocked for the
purposes of isolated unit testing. The primary difference is that the `IonElementLoaderOptions` instance is passed to
the `createIonElementLoader` function, which creates a real (non-mock) implementation of
`IonElementLoader`.

```Kotlin
val loader: IonElementLoader = createIonElementLoader(IonElementLoaderOptions(includeLocationMeta = true));
val element: AnyElement = loader.loadSingleElement("{ some_field: 42 }")
```

## Writing Ion Data

### `IonElement.toString()`

Whereas `IonValue.toString()` is not guaranteed to produce valid Ion text, `IonElement.toString()` is. The string
representation is produced with a standard `IonTextWriter` from `ion-java`.

As a rule of thumb, if performance or space considerations are paramount, this should be avoided. It is generally more
performant to write an `IonElement` directly to an `IonWriter`, shown below.

### `IonElement.writeTo(IonWriter)`

`IonElement.writeTo(IonWriter)` exists for writing Ion data to an Ion binary or text stream.

```Kotlin
val meaningOfLife = ionInt(42)
val stringBuilder = StringBuilder()

// Construct an `IonWriter` in the usual way:
val ion = IonSystemBuilder.standard().build()
ion.newTextWriter(stringBuilder).use { writer ->
    meaningOfLife.writeTo(writer)
}

// Prints "42"
println(stringBuilder.toString())
```

## `IonElement.hashCode` and `IonElement.equals`

`IonElement.hashCode` and `IonElement.equals` behave identically to `IonValue`'s--the definition of equivalence is the
same.

## Metas

Every instance of `IonElement` contains a `metas: HashSet<String, Any>` that is useful for storing arbitrary metadata
with each node. This metadata does not affect `IonElement.equals` or `IonElement.hashCode`, and it is currently up to
the application to take care of persisting any metas.
[Future work: this library should assist with (de)serializing metas](https://github.com/amzn/ion-element-kotlin/issues/65)
.

- Metas not considered during equivalence and computation of hash codes.
- not currently represented when written out as text or binary Ion.  (Provide link)

## Loading Data With Location Metas

In certain contexts, for example, when Ion text is used as the syntax of a DSL (domain specific language) or 
as a configuration file, it is helpful to know the line & column number of the Ion value within a file so that 
semantic validation errors may be reported to the end user.  This is accomplished through the use of the previously
described metas.

The inclusion of location metas comes at a additional CPU cost which is non-trivial for large files, so by default is
disabled. It is enabled by passing `IonElementLoaderOptions(includeLocationMeta = true)` along to the corresponding 
loader function.

```Kotlin
val ionText = "{\n  some_field: 42 }"

val structElem: StructElement =
    loadSingleElement(ionText, IonElementLoaderOptions(includeLocationMeta = true)).asStruct()

// Struct is positioned on line 1 at character 1.
assertEquals(IonTextLocation(1, 1), structElem.metas.location)
// some_field begins on line 2 at character 3.
assertEquals(IonTextLocation(2, 3), structElem["some_field"].metas.location)
```

Note that `.location` above is an extension property provided for convenience.

Finally, this works for both Ion text and Ion binary files, however for binary files, this the meta includes a byte
offset instead of a line number and character offset.

## Converting between `IonElement` and `IonValue` Instances

In order to support better interoperability with existing code that uses `IonValue`, it is easy to convert between
`IonElement` and `IonValue` instances. The `IonValue.toIonElement()` and `IonElement.toIonValue(ValueFactory)`
extension functions are provided for this purpose.

```Kotlin
val messageString = "What are you doing, Dave?"
val ion = IonSystemBuilder.standard().build()

// Converting from IonValue to IonElement
val ionValue = ion.newString(messageString)

val ionElement = ionValue.toIonElement().asString()

assertEquals(ionString(messageString), ionElement)

// Converting from IonElement to IonValue
val ionElement2 = ionString(messageString)

val ionValue2 = ionElement.toIonValue(ion)

assertEquals(ion.newString(messageString), ionValue2)
```

## Future of `IonElement`

Currently, there are two possible areas of future development for `IonElement`. 

- [Path copying](https://en.wikipedia.org/wiki/Persistent_data_structure#Path_copying) can be employed to aid in the
transformation of nested deeply nested elements.  Today, clients must provide their own solutions for transformation 
of deeply nested `IonElement` values.  Ideally, this library provides such a facility out of the box. 
- Lazy loading of `IonElement` values.  An alternative to `ion-java`'s skip-scanning abilities, this would avoid parsing
the Ion text or binary values *until their values area actually requested*, and would provide all the benefits of 
skip-scanning, while also providing a rich Ion object model.  By contrast, today `IonElement` (and also`IonValue`)
eagerly read the entire top-level value and populate an entire deeply nested data structure).   

## Development

This repository contains the [ion-tests](https://github.com/amzn/ion-tests) repository as
a [git submodule](https://git-scm.com/docs/git-submodule). The easiest way to clone the `ion-element-kotlin` repository
and initialize its submodule is to run the following command:

```
$ git clone --recursive https://github.com/amzn/ion-element-kotlin.git ion-element-kotlin
```

Alternatively, the submodule may be initialized independently of the clone by running the following commands:

```
$ git submodule init
$ git submodule update
```

`ion-element-kotlin` may now be built with the following command:

```
$ ./gradlew build
```

### Pulling in Upstream Changes

To pull upstream changes into `ion-element-kotlin`, start with a simple `git pull`. This will pull in any changes
to `ion-element-kotlin` itself (including any changes to its `.gitmodules` file), but not any changes to the submodules.
To make sure the submodules are up-to-date, use the following command:

```
$ git submodule update --remote
```

For detailed walkthroughs of git submodule usage, see the
[Git Tools documentation](https://git-scm.com/book/en/v2/Git-Tools-Submodules).

## License

This project is licensed under the Apache-2.0 License.

