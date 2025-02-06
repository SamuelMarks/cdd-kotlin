package com.example.parsing.lexer.scripts

fun main() {
    // Keywords
    val whenKeyword = "when"

    // Identifiers
    val identifier2 = "anotherVariable"

    // Numbers and Arithmetic Operators
    val number = 123
    val decimal = 3.14
    val sum = number + 10
    val product = sum * 2

    // Strings and String Interpolation
    val stringLiteral = "Hello, Kotlin!"
    val interpolatedString = "The sum is $sum and the product is $product."

    // Characters
    val charLiteral = 'A'

    // Booleans and Logical Operators
    val isKotlinFun = true
    val isJavaFun = false
    val comparison = isKotlinFun && !isJavaFun

    // Collections
    val list = listOf(1, 2, 3)
    val map = mapOf("key1" to "value1", "key2" to "value2")

    // Nullability
    val nullableString: String? = null
    val nonNullableString: String = "Not null"

    // Functions
    fun sayHello(name: String): String {
        return "Hello, $name!"
    }

    val greeting = sayHello("World")

    // Control Structures
    if (isKotlinFun) {
        println("Kotlin is fun!")
    } else {
        println("Kotlin is not fun.")
    }

    when (number % 2) {
        0 -> println("$number is even.")
        1 -> println("$number is odd.")
        else -> println("Unexpected result.")
    }

    // Loops
    for (i in 1..5) {
        println("Iteration: $i")
    }

    var counter = 0
    while (counter < 3) {
        println("Counter: $counter")
        counter++
    }

    // Lambda Expressions
    val multiply = { x: Int, y: Int -> x * y }
    println("5 * 6 = ${multiply(5, 6)}")

    // Classes and Objects
    class Person(val name: String, val age: Int)

    val person = Person("John Doe", 30)
    println("Person: ${person.name}, Age: ${person.age}")

    // Data Classes
    data class Point(val x: Int, val y: Int)

    val point = Point(1, 2)
    println("Point: (${point.x}, ${point.y})")

    // Exception Handling
    try {
        val result = 10 / 0
    } catch (e: ArithmeticException) {
        println("Cannot divide by zero.")
    }
}
