package io.offscale.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 
 *
 * @param id 
 * @param name 
 * @param tag 
 */
@Serializable
data class Pet(
	@SerialName("id")
	val id: Int,
	@SerialName("name")
	val name: String,
	@SerialName("tag")
	val tag: String
)
