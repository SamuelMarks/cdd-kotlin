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
	val code: Int,
	val message: String
)
