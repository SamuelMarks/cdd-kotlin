package io.offscale.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 
 *
 * @param code 
 * @param message 
 */
@Serializable
data class Error(
	@SerialName("code")
	val code: Int,
	@SerialName("message")
	val message: String
)
