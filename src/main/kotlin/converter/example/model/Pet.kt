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
	val id: Int,
	val name: String,
	val tag: String?
)
