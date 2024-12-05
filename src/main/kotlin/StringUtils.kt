package io.offscale

import java.util.*

fun toCamelCase(s: String) =
    s.replace(Regex("['`]"), "").split(Regex("[\\W_]+|(?<=[a-z])(?=[A-Z][a-z])")).joinToString("") { s1 ->
        s1.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }.replaceFirstChar { it.lowercase(Locale.getDefault()) }
