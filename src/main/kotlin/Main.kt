package io.offscale

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

class CddKotlin : CliktCommand() {
    init {
        versionOption("0.0.1")
    }

    override fun run() {
       println("run")
    }
}

enum class CddTypes { KTOR_CLIENT, KTOR_CLIENT_TESTS, VIEW_MODEL }


class Sync : CliktCommand() {
    private val truth by option(help = "the correct starting point, e.g., the ktor client")
        .file(canBeDir = false)
    private val generate by option(help = "what to generate, * to generate everything (default)")
        .enum<CddTypes> { toCamelCase(it.name) }
    override fun run() {
        print("[sync] truth: $truth ; generate: $generate")
    }
}

class Emit : CliktCommand() {
    private val replaceExisting by option(help="whether to override any existing file")
        .boolean()
    private val filename by option(help="path to OpenAPI file, default: ./openapi.json")
        .file(canBeDir = false)
        .default(File("./openapi.json"))

    override fun run() {
        print("[Emit] $replaceExisting $filename")
    }
}

fun main(args: Array<String>) = CddKotlin().subcommands(Sync(), Emit()).main(args)
