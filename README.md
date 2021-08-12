![Build Pipeline](https://github.com/amzn/ion-element-kotlin/workflows/Build%20Pipeline/badge.svg)

# Ion Element

`IonElement` is an immutable in-memory representation of [Amazon Ion](http://amzn.github.io/ion-docs/) which has an API
that is idiomatic to Kotlin.  `IonElement` is meant as an alternative to `IonValue` from 
[Ion-java](https://github.com/amzn/ion-java) but is not a complete implementation of Ion as it relies on `Ion-java`'s 
`IonReader` and `IonWriter` interfaces for reading and writing Ion data.

### Status

The the library is capable of representing any Ion value, however the public API should be considered 
unstable and in alpha status.

## Example

### Constructing Ion Values

`ion-element` provides a function for creating instances of each of the Ion element types.

```kotlin
import com.amazon.ionelement.api.ionInt
import com.amazon.ionelement.api.ionListOf
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionTimestamp

val greeting = ionString("Hello world!")

val ultimateAnswer = ionInt(42)

val listOfInts = ionListOf((1L..10L).map { ionInt(it) })

val anotherList = ionListOf(
    greeting,
    ultimateAnswer,
    listOfInts,
    annotations = listOf("yolo", "i_love_ion")
)

val person = ionStructOf(
    "firstName" to ionString("Frédéric"),
    "lastName" to ionString("Chopin"),
    "birthdate" to ionTimestamp("1810-02-22")
)
```

### Loading Ion Data using an IonReader
```Kotlin
// Loading Ion Data using an IonReader from Ion-java

val stockItemsIonText = """
stock_item::{
    name: "Fantastic Widget",
    price: 12.34,
    countInStock: 2,
    orders: [
        { customerId: 123, state: WA },
        { customerId: 456, state: "HI" }
    ]
}
stock_item::{ // stock item has no name
    price: 23.45,
    countInStock: 20,
    orders: [
        { customerId: 234, state: "VA" },
        { customerId: 567, state: MI }
    ]
}
"""

val ion = IonSystemBuilder.standard().build
val stockItems = ion.newReader(stockItemsIonText).use { reader ->
   loadAllElements(reader)
        .map { stockItem: AnyElement ->
            stockItem.asStruct().run {
                StockItem(
                    getOptional("name")?.textValue ?: "<unknown name>",
                    get("price").decimalValue,
                    get("countInStock").longValue,
                    get("orders").asList().values.map { order ->
                        order.asStruct().run {
                            Order(
                                get("customerId").longValue,
                                get("state").textValue)
                        }
                    })
            }
        }
}
```

## Development
This repository contains the [ion-tests](https://github.com/amzn/ion-tests) repository as a [git submodule](https://git-scm.com/docs/git-submodule).
The easiest way to clone the `ion-element-kotlin` repository and initialize its submodule
is to run the following command:

```
$ git clone --recursive https://github.com/amzn/ion-element-kotlin.git ion-element-kotlin
```

Alternatively, the submodule may be initialized independently of the clone
by running the following commands:

```
$ git submodule init
$ git submodule update
```

`ion-element-kotlin` may now be built with the following command:

```
$ ./gradlew build
```

### Pulling in Upstream Changes
To pull upstream changes into `ion-element-kotlin`, start with a simple `git pull`.
This will pull in any changes to `ion-element-kotlin` itself (including any changes
to its `.gitmodules` file), but not any changes to the submodules.
To make sure the submodules are up-to-date, use the following
command:

```
$ git submodule update --remote
```

For detailed walkthroughs of git submodule usage, see the
[Git Tools documentation](https://git-scm.com/book/en/v2/Git-Tools-Submodules).

## License

This project is licensed under the Apache-2.0 License.

