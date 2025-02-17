package com.example.parsing.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Cat entity.
 *
 * @param name The name of the cat.
 * @param age The age of the cat in years.
 * @param isMale Whether the cat is male (true) or female (false).
 */
@Serializable
data class Cat(

    @SerialName("name")
    val name: String,

    @SerialName("age")
    val age: Int,

    @SerialName("is_male")
    val isMale: Boolean
)