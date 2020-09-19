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

## Building

Pull in the `ion-tests` git submodule:

```
git submodule update --init
```

Then run the gradle wrapper:

```
./gradlew build
```

## License

This project is licensed under the Apache-2.0 License.

