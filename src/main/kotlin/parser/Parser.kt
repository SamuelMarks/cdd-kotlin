package io.offscale.parser

import kotlin.collections.mutableListOf
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import kotlin.reflect.KClass
import io.offscale.lexer.Token
import io.offscale.lexer.*


abstract class Block(var name: String, var content: MutableList<Any>) {


    fun kdocToMap(kdoc: String): MutableMap<String, Any> {
        val result = mutableMapOf<String, Any>()

        val descriptionRegex = Regex("""/\*\*\s*\n\s*\*(.+?)\n\s*\*""", RegexOption.DOT_MATCHES_ALL)
        val descriptionMatch = descriptionRegex.find(kdoc)
        val description = descriptionMatch?.groups?.get(1)?.value?.trim()
        if (!description.isNullOrBlank()) {
            result["description"] = description
        }

        val producesRegex = Regex("""@produces\s+([^\n\r]+)""")
        val producesMatch = producesRegex.find(kdoc)
        producesMatch?.groups?.get(1)?.value?.trim()?.let {
            result["produces"] = it
        }


        val responseRegex = Regex("""@response\s+(\d+)\s+(.+)""")
        val responses = mutableMapOf<String,String>()
        responseRegex.findAll(kdoc).forEach { match ->
            val (code, respDescription) = match.destructured
            responses[code] = respDescription.trim()
        }
        if (responses.isNotEmpty()) {
            result["responses"] = responses
        }

        val paramRegex = Regex("""@param\s+(\w+)\s+(.+)""")
        val params = mutableMapOf<String, String>()
        paramRegex.findAll(kdoc).forEach { match ->
            val (paramName, paramDescription) = match.destructured
            params[paramName] = paramDescription.trim()
        }
        if (params.isNotEmpty()) {
            result["parameters"] = params
        }

        return result
    }

    fun toCode(): String {
        var code = ""

        for (item in content) {
            when (item) {
                is Token -> {
                    code += item.value
                }

                is Block -> {
                    code += item.toCode()
                }
            }
        }
        return code
    }

    override fun toString(): String {
        return toStringWithIndent("", true)
    }

    private fun toStringWithIndent(prefix: String, isLast: Boolean): String {
        val connector = if (isLast) "|__ " else "|-- "
        val newPrefix = prefix + if (isLast) "\t" else "|\t"


        val blockString = "$prefix$connector${name}Block"

        val childBlocks = content.filterIsInstance<Block>()
        val childrenString = childBlocks.mapIndexed { index, block ->
            block.toStringWithIndent(newPrefix, index == childBlocks.lastIndex)
        }.joinToString("\n")

        return if (childrenString.isEmpty()) {
            blockString
        } else {
            "$blockString\n$childrenString"
        }
    }
}


class InterfaceBlock(content: MutableList<Any>) : Block("Interface", content) {
    var structure: MutableMap<String, Any?> = mutableMapOf()

    init {
        for (item in content) {
            when (item) {
                is IdentifierBlock -> structure["name"] = item.value
                is ArgumentsBlock -> structure["arguments"] = item.arguments
            }
        }
        structure["return"] = null
    }
}

class ClassBlock(content: MutableList<Any>) : Block("Class", content) {
    var structure: MutableMap<String, Any?> = mutableMapOf()

    init {
        for (item in content) {
            when (item) {
                is IdentifierBlock -> structure["name"] = item.value
                is ArgumentsBlock -> structure["arguments"] = item.arguments
            }
        }
        structure["return"] = null
    }


}

class SourceBlock(content: MutableList<Any>) : Block("Source", content) {
    lateinit var kdoc : MutableMap<String, Any>
    init {
        for (item in content) {
            when (item) {
                is CommentBlock -> kdoc = kdocToMap(item.kdoc)
            }
        }

    }

}

class FunctionBlock(content: MutableList<Any>) : Block("Function", content) {
    var structure: MutableMap<String, Any> = mutableMapOf()

    init {
        for (item in content) {
            when (item) {
                is IdentifierBlock -> structure["name"] = item.value
                is ArgumentsBlock -> structure["arguments"] = item.arguments
                is TypeBlock -> structure["type"] = item.type
                is BodyBraceBlock -> structure["Body"] = item.toCode()
            }
        }
    }
}


class ArgumentsBlock(content: MutableList<Any>) : Block("Arguments", content) {
    var arguments: MutableList<MutableMap<String, String?>> = mutableListOf()

    init {
        for (item in content) {
            when (item) {
                is VarArgumentBlock -> arguments.add(item.argument)
                is ValArgumentBlock -> arguments.add(item.argument)
                is ArgumentBlock -> arguments.add(item.argument)
            }
        }
    }

}

class VarArgumentBlock(content: MutableList<Any>) : Block("VarArgument", content) {
    var argument: MutableMap<String, String?> = mutableMapOf()

    init {
        for (item in content) {
            when (item) {
                is IdentifierBlock -> argument["value"] = item.value
                is TypeBlock -> argument["type"] = item.type
                is StringBlock -> argument["default"] = item.toCode()
                is NumberBlock -> argument["default"] = item.toCode()
                is AnnotationBlock -> argument["alias"] = item.alias
                // TODO: Implement the required values
            }
        }
    }
}

class ValArgumentBlock(content: MutableList<Any>) : Block("ValArgument", content) {
    var argument: MutableMap<String, String?> = mutableMapOf()

    init {
        for (item in content) {
            when (item) {
                is IdentifierBlock -> argument["value"] = item.value
                is TypeBlock -> argument["type"] = item.type
                is StringBlock -> argument["default"] = item.toCode()
                is NumberBlock -> argument["default"] = item.toCode()
                is AnnotationBlock -> argument["alias"] = item.alias
            }
        }
    }
}

class ArgumentBlock(content: MutableList<Any>) : Block("Argument", content) {
    var argument: MutableMap<String, String?> = mutableMapOf()

    init {
        for (item in content) {
            when (item) {
                is IdentifierBlock -> argument["value"] = item.value
                is TypeBlock -> argument["type"] = item.type
                is StringBlock -> argument["default"] = item.toCode()
                is NumberBlock -> argument["default"] = item.toCode()
                is AnnotationBlock -> argument["alias"] = item.alias

            }
        }
    }
}

class IdentifierBlock(content: MutableList<Any>) : Block("Identifier", content) {
    lateinit var value: String

    init {
        val token = content[0]
        if (token is Token) value = token.value
    }

}

class TypeBlock(content: MutableList<Any>) : Block("Type", content) {
    lateinit var type: String

    init {
        for (item in content) {
            if (item is IdentifierBlock) type = item.value
        }
    }
}

class AnnotationBlock(content: MutableList<Any>) : Block("Annotation", content){
    lateinit var alias : String

    init {
        for (item in content) {
            if (item is StringBlock) alias = item.value
        }
    }
}

class StringBlock(content: MutableList<Any>) : Block("String", content){
    lateinit var value : String
    init {
        for (item in content) {
            if (item is StringContent) value = item.toCode()
        }
    }
}

class CommentBlock(content: MutableList<Any>) : Block("Comment", content){
    lateinit var kdoc : String

    init {
        for (item in content) {
            if (item is Token && item.type==TokenType.COMMENT)
                kdoc = item.value
        }
    }
}
class KtorMethodBlock(content: MutableList<Any>) : Block("KtorMethod", content) {
    lateinit var kdoc : MutableMap<String, Any>
    var subroute = ""
    var method = ""
    init {
        for (item in content) {
                if(item is CommentBlock)
                    kdoc = kdocToMap(item.kdoc)
                else if (item is StringBlock)
                    subroute = item.value
                else if (item is IdentifierBlock)
                    method = item.value
            }
            kdoc["method"] = method
        }

}

class KtorRouteBlock(content: MutableList<Any>) : Block("KtorRoute", content) {
    var routes : MutableMap<String, MutableList<MutableMap<String, Any>>> = mutableMapOf()

    init {
        var rootRoute = ""
        for (item in content) {
            if (item is StringBlock)
                rootRoute = item.value
        }

        for (item in content) {
            if (item is KtorMethodBlock)
                routes.getOrPut(rootRoute+item.subroute) { mutableListOf() }.add(item.kdoc)
        }
    }
}

class PackOrImportBlock(content: MutableList<Any>) : Block("PackOrImport",content)
class BodyBraceBlock(content: MutableList<Any>) : Block("BodyBrace", content)
class ControlBlock(content: MutableList<Any>) : Block("Control", content)
class WhileBlock(content: MutableList<Any>) : Block("While", content)
class IfBlock(content: MutableList<Any>) : Block("If", content)
class IncrementBlock(content: MutableList<Any>) : Block("Increment", content)
class FunctionCallBlock(content: MutableList<Any>) : Block("FunctionCall", content)
class VariableBlock(content: MutableList<Any>) : Block("Variable", content)
class AssignmentBlock(content: MutableList<Any>) : Block("Assignment", content)
class DeclarationBlock(content: MutableList<Any>) : Block("Declaration", content)
class StringContent(content: MutableList<Any>) : Block("StringContent", content)
class StringInterpolated(content: MutableList<Any>) : Block("StringInterpolated", content)
class SignalBlock(content: MutableList<Any>) : Block("Signal", content)
class NumberBlock(content: MutableList<Any>) : Block("Number", content)
class ParenthesisBlock(content: MutableList<Any>) : Block("Parenthesis", content)
class BinaryExpressionBlock(content: MutableList<Any>) : Block("BinaryExpression", content)


class Parser(val tokens: List<Token>) {
    var tokenPosition = 0
    var actualToken: Token = tokens[tokenPosition]

    fun verifyWhiteSpace(elements: MutableList<Any>): MutableList<Any> {
        if (actualToken.type == TokenType.WHITESPACE) {
            elements.add(actualToken)
            consumeToken()
        }

        return elements
    }

    fun resetToken() {
        tokenPosition = 0
        actualToken = tokens[tokenPosition]
    }
    fun parserPackageOrImport(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if(actualToken.type == TokenType.PACKAGE || actualToken.type == TokenType.IMPORT) {
            elements.add(elements)
            consumeToken()
            verifyWhiteSpace(elements)
            elements.add(IdentifierBlock(mutableListOf(actualToken)))
            consumeToken()
            verifyWhiteSpace(elements)
            while(actualToken.type==TokenType.DOT){
                if(actualToken.type==TokenType.TIMES) {
                    elements.add(actualToken)
                    consumeToken()
                    verifyWhiteSpace(elements)
                    break
                }
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
                elements.add(IdentifierBlock(mutableListOf(actualToken)))
                consumeToken()
                verifyWhiteSpace(elements)
            }

        }
        return PackOrImportBlock(elements)
    }
    fun parserAnnotation(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if(actualToken.type==TokenType.AT) {
            elements.add(actualToken)
            consumeToken()
            elements.add(IdentifierBlock(mutableListOf(actualToken)))
            consumeToken()
        }
        verifyWhiteSpace(elements)
        if(actualToken.type==TokenType.LPAREN) {
            elements.add(actualToken)
            consumeToken()
            elements.add(parserString())
            if(actualToken.type != TokenType.RPAREN) throw Error("parserAnnotation: expected a ')' instead got $actualToken")
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
        }

        return AnnotationBlock(elements)
    }
    fun parserDeclaration(): Block {
        if (actualToken.type == TokenType.WHITESPACE) {

            return when (nextToken().type) {
                TokenType.COMMENT-> parserComment()
                TokenType.PACKAGE,TokenType.IMPORT -> parserPackageOrImport()
                TokenType.AT -> parserAnnotation()
                TokenType.FUNCTION -> parserFunctionDeclaration()
                TokenType.CLASS -> parserClassDeclaration()
                TokenType.INTERFACE -> parserInterface()
                TokenType.IDENTIFIER ->
                    if (actualToken.value=="route") {
                        parserKtorRoutes()
                    } else if (actualToken.value== "data") {
                        parserClassDeclaration()
                    } else  throw Error("Lacks implementation")
                else -> throw Error("parserDeclaration: expects top-level declaration : fun, class ...")
            }
        }
        return when (actualToken.type) {
            TokenType.COMMENT-> parserComment()
            TokenType.PACKAGE,TokenType.IMPORT -> parserPackageOrImport()
            TokenType.AT -> parserAnnotation()
            TokenType.FUNCTION -> parserFunctionDeclaration()
            TokenType.CLASS -> parserClassDeclaration()
            TokenType.INTERFACE -> parserInterface()
            TokenType.IDENTIFIER ->
                if (actualToken.value=="route") {
                    parserKtorRoutes()
                } else if (actualToken.value== "data") {
                    parserClassDeclaration()
                } else  throw Error("Lacks implementation")
            else -> throw Error("parserDeclaration: expects top-level declaration : fun, class ...")
        }
    }

    fun parserKtorRoutes() : Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.value!="route") throw Error("parserKtorRoutes: Expected 'route', instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type!=TokenType.LPAREN) throw Error("parserKtorRoutes: Expected '(', instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        elements.add(parserString())
        if(actualToken.type!=TokenType.RPAREN) throw Error("parserKtorRoutes: Expected ')', instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        if(actualToken.type!=TokenType.LBRACE) throw Error("parserKtorRoutes: Expected '{', instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        while(actualToken.type!=TokenType.RBRACE) {
            elements.add(parserKtorRouteMethods())
        }
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        return KtorRouteBlock(elements)
    }

    fun parserKtorRouteMethods(): Block {
        val httpsMethods = listOf("get","post","delete","put","patch","options","head")
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        elements.add(parserComment())
        if(!httpsMethods.contains(actualToken.value)) throw Error("parserKtorRouteMethods: expected method (get,post...), got $actualToken instead")
        elements.add(IdentifierBlock(mutableListOf(actualToken)))
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type==TokenType.LPAREN) {
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
            elements.add(parserString())
            if (actualToken.type != TokenType.RPAREN) throw Error("parserKtorRoutesMethods: Expected ')', instead got: $actualToken")
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
        }
        var LBrace = 0
        if(actualToken.type!=TokenType.LBRACE) throw Error("parserKtorRoutesMethods: Expected '{', instead got: $actualToken")
        LBrace++
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        while(LBrace > 0) {
            consumeToken()
            if(actualToken.type==TokenType.RBRACE) LBrace--
            if(actualToken.type==TokenType.LBRACE) LBrace++
        }
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)

        return KtorMethodBlock(elements)
    }

    fun parserInterface(): Block {
        val elements = mutableListOf<Any>()
        val elementsArg = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.INTERFACE) throw Error("parserInterface:Expected 'interface', instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.IDENTIFIER) throw Error("parserInterface:Expected Interface's name, instead got: $actualToken")
        elements.add(IdentifierBlock(mutableListOf(actualToken)))
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.LBRACE) throw Error("parserIterface: Expected '{', instead got: $actualToken ")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elementsArg)
        while (actualToken.type != TokenType.RBRACE) {
            elementsArg.add(parserVarArgument(isInterface = true))
        }
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        elements.add(ArgumentsBlock(elementsArg))

        return InterfaceBlock(elements)
    }


    fun parserClassDeclaration(): Block {
        val elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.value == "data"){
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
        }
        if (actualToken.type != TokenType.CLASS) throw Error("parserClassDeclaration:Expected 'class', instead got: $actualToken")

        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)

        if (actualToken.type != TokenType.IDENTIFIER) throw Error("parserFunctionDeclaration:Expected class identifier, instead got: $actualToken")

        elements.add(IdentifierBlock(mutableListOf(actualToken)))
        consumeToken()
        elements.add(parserArgumentsDeclaration(true))
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.LBRACE)
            elements.add(parserBodyBrace())

        return ClassBlock(elements)
    }

    fun parserVarArgument(isClass: Boolean = false, isInterface: Boolean = false): Block {
        var elements = mutableListOf<Any>()
        var isVariable = false
        var isValue = false
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.AT) elements.add(parserAnnotation())
        if (isClass && (actualToken.type == TokenType.VALUE || actualToken.type == TokenType.VARIABLE)) {
            if (actualToken.type == TokenType.VARIABLE) isVariable = true
            else if (actualToken.type == TokenType.VALUE) isValue = true
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
        } else if (isInterface) {
            if (actualToken.type == TokenType.VARIABLE) isVariable = true
            else if (actualToken.type == TokenType.VALUE) isValue = true
            else throw Error("parserVarArgument: Interfaces need val or var.")
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
        }

        if (actualToken.type != TokenType.IDENTIFIER) throw Error("parserArgumentsDeclaration: Expected identifier, got instead: $actualToken")
        elements.add(IdentifierBlock(mutableListOf(actualToken)))
        consumeToken()
        elements.add(parserType())
        verifyWhiteSpace(elements)

        if (isInterface) {
            if (actualToken.value == "get") {
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
                if (actualToken.type != TokenType.LPAREN) throw Error("parserVarArgument[Interface]: expected '(' got $actualToken instead")
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
                if (actualToken.type != TokenType.RPAREN) throw Error("parserVarArgument[Interface]: expected ')' got $actualToken instead")
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
                if (actualToken.type == TokenType.ATTRIBUTE) {
                    elements.add(actualToken)
                    consumeToken()
                    verifyWhiteSpace(elements)
                    elements.add(parserRealExpression())
                } else throw Error("parserVarArgument[Interface]: expected '=' got $actualToken instead")
            }
        }
        if (actualToken.type == TokenType.ATTRIBUTE) {
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
            elements.add(parserRealExpression())

        }

        if (isVariable) {
            return VarArgumentBlock(elements)
        } else if (isValue) {
            return ValArgumentBlock(elements)
        } else {
            return ArgumentBlock(elements)
        }
    }

    fun parserComment(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        return CommentBlock(elements)
    }

    fun parserArgumentsDeclaration(isClass: Boolean): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.LPAREN) throw Error("expected '(', instead got $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.RPAREN) {
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
            val argumentsFunction = ArgumentsBlock(elements)
            argumentsFunction.name = "ArgumentsFunction"
            return argumentsFunction
        }
        elements.add(parserVarArgument(isClass = isClass))
        while (actualToken.type == TokenType.COMMA) {
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
            elements.add(parserVarArgument(isClass = isClass))
        }
        if (actualToken.type != TokenType.RPAREN) throw Error("expected ')', instead got $actualToken")
        elements.add(actualToken)
        consumeToken()
        val argumentsFunction = ArgumentsBlock(elements)
        argumentsFunction.name = "ArgumentsFunction"
        return argumentsFunction
    }

    fun parserFunctionDeclaration(): Block {
        val elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.FUNCTION) throw Error("parserFunctionDeclaration:Expected 'fun', instead got: $actualToken")

        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)

        if (actualToken.type != TokenType.IDENTIFIER) throw Error("parserFunctionDeclaration:Expected fun identifier, instead got: $actualToken")
        elements.add(IdentifierBlock(mutableListOf(actualToken)))
        consumeToken()
        while(actualToken.type!=TokenType.LPAREN){
            elements.add(actualToken)
            consumeToken()
        }
        elements.add(parserArgumentsDeclaration(false))
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.COLON) elements.add(parserType())
        elements.add(parserBodyBrace())

        return FunctionBlock(elements)
    }

    fun parserStatement(): Block {
        if (actualToken.type == TokenType.WHITESPACE) {
            return when (nextToken().type) {
                TokenType.VARIABLE, TokenType.VALUE -> parserVariableDeclaration()
                TokenType.IDENTIFIER -> {
                    if(actualToken.value=="route"){
                        parserKtorRoutes()
                    }
                    else if (nextToken(2).type == TokenType.ATTRIBUTE ||
                        nextToken(2).type == TokenType.WHITESPACE &&
                        nextToken(3).type == TokenType.ATTRIBUTE
                    ) {
                        parserVariableAssignment()
                    } else {
                        parserExpressionStatement()

                    }
                }

                TokenType.IF -> parserIfStatement()
                TokenType.WHILE -> parserWhileStatement()
                TokenType.RETURN, TokenType.BREAK, TokenType.CONTINUE -> parserControlStatement()
                TokenType.FUNCTION -> parserFunctionDeclaration()
                TokenType.CLASS -> parserClassDeclaration()
                TokenType.INTERFACE -> parserInterface()
                TokenType.COMMENT -> parserComment()
                else -> throw Error("Unexpected token: ${nextToken()}")
            }
        } else {
            return when (actualToken.type) {
                TokenType.VARIABLE, TokenType.VALUE -> parserVariableDeclaration()
                TokenType.IDENTIFIER -> {
                    if(actualToken.value=="route"){
                        parserKtorRoutes()
                    }
                    else if (nextToken().type == TokenType.ATTRIBUTE ||
                        nextToken().type == TokenType.WHITESPACE &&
                        nextToken(2).type == TokenType.ATTRIBUTE
                    ) {
                        parserVariableAssignment()
                    } else {
                        parserExpressionStatement()
                    }
                }

                TokenType.IF -> parserIfStatement()
                TokenType.WHILE -> parserWhileStatement()
                TokenType.RETURN, TokenType.BREAK, TokenType.CONTINUE -> parserControlStatement()
                TokenType.FUNCTION -> parserFunctionDeclaration()
                TokenType.CLASS -> parserClassDeclaration()
                TokenType.INTERFACE -> parserInterface()
                TokenType.COMMENT -> parserComment()
                else -> throw Error("Unexpected token: $actualToken")
            }
        }


    }

    fun parserIncrement(): Block {
        var elements: MutableList<Any> = ArrayList()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.IDENTIFIER) {
            elements.add(IdentifierBlock(mutableListOf(actualToken)))
            consumeToken()
            verifyWhiteSpace(elements)
            if (actualToken.type == TokenType.INCREMENT || actualToken.type == TokenType.DECREMENT) {
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
                if (actualToken.type == TokenType.SEMICOLON || nextToken(-1).value.contains("\n") || actualToken.type == TokenType.EOF || actualToken.type == TokenType.COMMENT) {
                    if (actualToken.type == TokenType.SEMICOLON) {
                        elements.add(actualToken)
                        consumeToken()
                        verifyWhiteSpace(elements)
                    }
                    if (actualToken.type == TokenType.COMMENT) elements.add(parserComment())

                } else throw Error("parserIncrement: end statement ; or \\n, instead got $actualToken")
                return IncrementBlock(elements)
            } else throw Error("parserIncrement: expected ++ or --, instead got: $actualToken")
        } else throw Error("parserIncrement: expected identifier instead got: $actualToken")
    }

    fun parserVariableAssignment(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        elements.add(VariableBlock(mutableListOf(actualToken))) // adding the identifier Token
        consumeToken()
        verifyWhiteSpace(elements)
        elements.add(actualToken)// adding the attribute symbol token
        consumeToken()
        elements.add(parserRealExpression())
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.SEMICOLON || nextToken(-1).value.contains("\n") || actualToken.type == TokenType.EOF || actualToken.type == TokenType.COMMENT) {
            if (actualToken.type == TokenType.SEMICOLON) {
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
            }
            if (actualToken.type == TokenType.COMMENT) elements.add(parserComment())

        } else throw Error("ParserVariableAssignment: end statement ; or \\n, instead got $actualToken")


        return AssignmentBlock(elements)

    }


    fun parserVariableDeclaration(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        elements.add(actualToken) // adding the var or val token
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.IDENTIFIER) throw Error("expected identifier, instead got: $actualToken")
        elements.add(VariableBlock(mutableListOf(actualToken))) // adding the identifier Token
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.COLON) {
            elements.add(parserType())
        }
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.ATTRIBUTE) throw Error("expected '=', instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        elements.add(parserRealExpression())
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.SEMICOLON || nextToken(-1).value.contains("\n") || actualToken.type == TokenType.EOF || actualToken.type == TokenType.COMMENT) {
            if (actualToken.type == TokenType.SEMICOLON) {
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
            }
            if (actualToken.type == TokenType.COMMENT) elements.add(parserComment())

        } else throw Error("parserVariableDeclaration: end statement ; or \\n, instead got $actualToken")

        return DeclarationBlock(elements)
    }

    fun parserExpressionStatement(): Block {
        if (actualToken.type == TokenType.WHITESPACE) {
            if (nextToken().type == TokenType.IDENTIFIER) {
                if (nextToken(2).type == TokenType.LPAREN
                    || (nextToken(2).type == TokenType.WHITESPACE
                            && nextToken(3).type == TokenType.LPAREN)
                ) {
                    return parserFunctionCall()
                } else {
                    return parserIncrement()
                }
            } else throw Error("parserExpressionStatement: Expected identifier, instead got $actualToken")

        }
        if (actualToken.type == TokenType.IDENTIFIER) {
            if (nextToken().type == TokenType.LPAREN
                || (nextToken().type == TokenType.WHITESPACE
                        && nextToken(2).type == TokenType.LPAREN)
            ) {
                return parserFunctionCall()
            } else {
                return parserIncrement()
            }

        } else throw Error("parserExpressionStatement: Expected identifier, instead got $actualToken")
    }

    fun parserFunctionCall(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.IDENTIFIER) throw Error("ParserFunctionCall:expected identifier, instead got: $actualToken")
        elements.add(IdentifierBlock(mutableListOf(actualToken)))
        consumeToken()
        elements.add(parserArgument())
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.COMMENT) elements.add(parserComment())
        if (actualToken.type == TokenType.SEMICOLON || nextToken(-1).value.contains("\n") || actualToken.type == TokenType.EOF || actualToken.type == TokenType.COMMENT) {
            if (actualToken.type == TokenType.SEMICOLON) {
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
            }
            if (actualToken.type == TokenType.COMMENT) elements.add(parserComment())

        } else throw Error("parserFunctionCall: end statement ; or \\n, instead got $actualToken")
        return FunctionCallBlock(elements)
    }

    fun parserArgument(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.LPAREN) throw Error("expected '(', instead got $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.RPAREN) {
            elements.add(actualToken)
            consumeToken()
            return ArgumentsBlock(elements)
        }
        if ((actualToken.type == TokenType.IDENTIFIER && nextToken().type == TokenType.ATTRIBUTE)
            || (actualToken.type == TokenType.IDENTIFIER && nextToken().type == TokenType.WHITESPACE)
            && nextToken(2).type == TokenType.ATTRIBUTE
        ) {
            elements.add(IdentifierBlock(mutableListOf(actualToken)))
            consumeToken()
            verifyWhiteSpace(elements)
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
        }
        elements.add(parserRealExpression()) // Add first Argument
        verifyWhiteSpace(elements)
        while (actualToken.type == TokenType.COMMA) {
            elements.add(actualToken)
            consumeToken()
            if ((actualToken.type == TokenType.IDENTIFIER && nextToken().type == TokenType.ATTRIBUTE)
                || (actualToken.type == TokenType.IDENTIFIER && nextToken().type == TokenType.WHITESPACE)
                && nextToken(2).type == TokenType.ATTRIBUTE
            ) {
                elements.add(IdentifierBlock(mutableListOf(actualToken)))
                consumeToken()
                verifyWhiteSpace(elements)
                elements.add(actualToken)
                consumeToken()
                verifyWhiteSpace(elements)
            }
            elements.add(parserRealExpression())
            verifyWhiteSpace(elements)
        }
        if (actualToken.type != TokenType.RPAREN) throw Error("expected ')', instead got $actualToken")
        elements.add(actualToken)
        consumeToken()
        return ArgumentsBlock(elements)
    }

    fun parserControlStatement(): Block {
        var elements = mutableListOf<Any>()
        var controlType = ""
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.CONTINUE) {
            controlType = "Continue"
        } else if (actualToken.type == TokenType.BREAK) controlType = "Break"
        else if (actualToken.type == TokenType.RETURN) controlType = "Return"
        else throw Error("parserControlStatement: expected break, return or continue, instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        var controlBlock = ControlBlock(elements)
        controlBlock.name = controlType
        return controlBlock
    }

    fun parserBodyBrace(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.LBRACE) {
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
            while (actualToken.type != TokenType.RBRACE) {
                elements.add(parserStatement())
            }
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)

        } else throw Error("parserBodyBrace: Expected {, instead got $actualToken")
        return BodyBraceBlock(elements)
    }

    fun parserIfStatement(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.IF) {
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
            var conditional = parserParenthesis()
            conditional.name = "Conditional"
            elements.add(conditional)
            elements.add(parserBodyBrace())

            if (actualToken.type == TokenType.ELSE) {
                consumeToken()
                verifyWhiteSpace(elements)

                if (actualToken.type == TokenType.IF) {
                    var elseIfBlock = parserIfStatement()
                    elseIfBlock.name = "ElseIf"
                    elements.add(elseIfBlock)
                } else {
                    var elseBlock = parserBodyBrace()
                    elseBlock.name = "Else"
                    elements.add(elseBlock)
                }
            }
        }
        return IfBlock(elements)
    }

    fun parserWhileStatement(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.WHILE) {
            elements.add(actualToken)
            consumeToken()
            verifyWhiteSpace(elements)
            var conditional = parserParenthesis()
            conditional.name = "Conditional"
            elements.add(conditional)
            elements.add(parserBodyBrace())

        }
        return WhileBlock(elements)
    }


    fun parserType(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.COLON) throw Error("expected ':' , instead got: $actualToken")
        elements.add(actualToken) // adding the colon
        consumeToken()
        verifyWhiteSpace(elements)
        if (actualToken.type != TokenType.IDENTIFIER) throw Error("expected identifier, instead got: $actualToken")
        elements.add(IdentifierBlock(mutableListOf(actualToken)))
        consumeToken()
        return TypeBlock(elements)
    }


    fun parserString(): Block {
        var elements: MutableList<Any> = mutableListOf()
        verifyWhiteSpace(elements)
        if(actualToken.type != TokenType.STRING_START) throw Error("parserString: expected \", instead got: $actualToken ")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        while (actualToken.type == TokenType.STRING_CONTENT || actualToken.type == TokenType.STRING_INTERPOLATION) {
            when {
                actualToken.type == TokenType.STRING_CONTENT -> {
                    elements.add(StringContent(mutableListOf(actualToken)))
                    consumeToken()
                }

                actualToken.type == TokenType.STRING_INTERPOLATION -> {
                    var symbolInterpolation = actualToken
                    consumeToken()
                    when (actualToken.type) {
                        TokenType.IDENTIFIER -> {
                            elements.add(
                                StringInterpolated(
                                    mutableListOf(
                                        symbolInterpolation,
                                        IdentifierBlock(mutableListOf(actualToken))
                                    )
                                )
                            )
                            consumeToken()
                        }

                        TokenType.LBRACE -> {
                            var LBrace = actualToken
                            consumeToken()
                            var interpolatedExpression = parserRealExpression()
                            if (actualToken.type == TokenType.RBRACE) {
                                elements.add(
                                    StringInterpolated(
                                        mutableListOf(
                                            symbolInterpolation,
                                            LBrace,
                                            interpolatedExpression,
                                            actualToken
                                        )
                                    )
                                )
                                consumeToken()
                            } else {
                                throw Error("Interpolated String requires }, instead got $actualToken")
                            }
                        }

                        else -> throw Error("Unexpected token type for Interpolated. Expected { or variable: ${actualToken.type}")
                    }
                }
            }

        }
        if(actualToken.type!=TokenType.STRING_END) throw Error("parserString: Expected \" instead got: $actualToken")
        elements.add(actualToken)
        consumeToken()
        verifyWhiteSpace(elements)
        return StringBlock(elements)
    }

    fun parserRealExpression(): Block {
        var elements = mutableListOf<Any>()
        var left = parserExpression()
        elements.add(left)
        verifyWhiteSpace(elements)
        while (actualToken.type == TokenType.EQUALS ||
            actualToken.type == TokenType.LESS ||
            actualToken.type == TokenType.GREATER ||
            actualToken.type == TokenType.GREATER_EQUAL ||
            actualToken.type == TokenType.LESS_EQUAL ||
            actualToken.type == TokenType.NOT_EQUAL
        ) {

            elements.add(actualToken)
            consumeToken()

            elements.add(parserExpression())

            left = BinaryExpressionBlock(elements.toMutableList())
            left.name = "Comparative"
            elements = mutableListOf(left)
            verifyWhiteSpace(elements)
        }

        return left
    }

    fun parserExpression(): Block {
        var elements = mutableListOf<Any>()
        var left = parserTerm()
        elements.add(left)
        verifyWhiteSpace(elements)
        while (actualToken.type == TokenType.PLUS || actualToken.type == TokenType.SUB) {

            elements.add(actualToken)
            consumeToken()

            elements.add(parserTerm())

            left = BinaryExpressionBlock(elements.toMutableList())
            left.name = "AddSub"
            elements = mutableListOf(left)
            verifyWhiteSpace(elements)
        }

        return left
    }

    fun parserParenthesis(): Block {
        var elements = mutableListOf<Any>()
        verifyWhiteSpace(elements)
        if (actualToken.type == TokenType.LPAREN) {
            elements.add(actualToken)
            consumeToken()
            elements.add(parserRealExpression())
            verifyWhiteSpace(elements)
            if (actualToken.type == TokenType.RPAREN) {
                elements.add(actualToken)
                consumeToken()
                val parenthesis = ParenthesisBlock(elements)
                return parenthesis
            } else {
                throw Error("Expected ), got $actualToken")
            }
        }
        throw Error("Expected (, got $actualToken")
    }

    fun parserTerm(): Block {
        var elements = mutableListOf<Any>()
        var left = parserFactor()
        elements.add(left)
        verifyWhiteSpace(elements)
        while (actualToken.type == TokenType.TIMES ||
            actualToken.type == TokenType.DIVIDE ||
            actualToken.type == TokenType.MOD ||
            actualToken.type == TokenType.AND ||
            actualToken.type == TokenType.OR ||
            actualToken.type == TokenType.OR_BITWISE ||
            actualToken.type == TokenType.AND_BITWISE
        ) {

            elements.add(actualToken)
            val isLogical = listOf(
                TokenType.AND,
                TokenType.OR,
                TokenType.OR_BITWISE,
                TokenType.AND_BITWISE
            ).contains(actualToken.type)
            consumeToken()

            elements.add(parserFactor())

            left = BinaryExpressionBlock(elements.toMutableList())
            if (isLogical) left.name = "Logical" else left.name = "MultDivMod"
            elements = mutableListOf(left)
            verifyWhiteSpace(elements)
        }

        return left
    }


    fun parserFactor(): Block {
        var elements: MutableList<Any> = ArrayList()
        verifyWhiteSpace(elements)
        when {
            (actualToken.type == TokenType.IDENTIFIER) -> {

                when {
                    (nextToken().type == TokenType.LPAREN
                            || (nextToken().type == TokenType.WHITESPACE
                            && nextToken(2).type == TokenType.LPAREN)) ->
                        return parserFunctionCall()
                }
                elements.add(actualToken)
                consumeToken()
                return IdentifierBlock(elements)
            }

            (actualToken.type == TokenType.INT
                    || actualToken.type == TokenType.DOUBLE) -> {
                elements.add(actualToken)
                val isDouble = actualToken.type == TokenType.DOUBLE
                consumeToken()
                val number = NumberBlock(elements)
                if (isDouble) number.name = "Double" else number.name = "Integer"
                return number
            }

            (actualToken.type == TokenType.PLUS
                    || actualToken.type == TokenType.SUB
                    || actualToken.type == TokenType.EXCLAMATION_MARK) -> {
                elements.add(actualToken)
                consumeToken()
                elements.add(parserFactor())
                return SignalBlock(elements)
            }

            (actualToken.type == TokenType.STRING_START) -> {
                    return parserString()
            }

            else -> {
                return parserParenthesis()
            }
        }

    }

    fun parserSource(): MutableList<Any> {
        var blocks = mutableListOf<Any>()
        while (actualToken.type != TokenType.EOF) {
            blocks.add(parserDeclaration())
        }
        return blocks
    }

    fun parseCode(): Block {

        return SourceBlock(parserSource())
    }


    fun consumeToken() {
        if (tokenPosition < tokens.size - 1) {
            tokenPosition++
            actualToken = tokens[tokenPosition]

        }
    }

    fun nextToken(step: Int = 1): Token {
        return tokens[tokenPosition + step]
    }

    fun <T : Block> findBlock(root: Block, clazz: KClass<T>): T? {
        if (clazz.isInstance(root)) {
            @Suppress("UNCHECKED_CAST")
            return root as T
        }
        for (item in root.content) {
            if (item is Block) {
                val found = findBlock(item, clazz)
                if (found != null) return found
            }
        }
        return null
    }


}

fun main() {

    fun jsonToMap(jsonString: String): Map<String, Any?> {
        val jsonElement = Json.parseToJsonElement(jsonString)
        return if (jsonElement is JsonObject) jsonElement.jsonObject.mapValues { it.value }
        else emptyMap()
    }

    val filePath =
        "C:\\Users\\jeans\\Desktop\\Parsing\\composeApp\\src\\desktopMain\\kotlin\\com\\example\\parsing\\lexer\\test_input.txt"
    val code = File(filePath).readText().replace("\r\n", "\n")
    val lexer = Lexer(code)
    val tokens = lexer.tokenize()
    val tokensTypes = lexer.extractTokenTypes(tokens)
    val parser = Parser(tokens)
    val block = parser.parseCode().content[2] as KtorRouteBlock
    var routeDict = block.routes
  /*  for (route in routeDict) {
        println(route)
        println("\n\n\n")
    }*/
  /*  if (block is ClassBlock) {
        var ir = block.structure
        var jsonSchema = parser.convertToJsonSchema(ir)
        println(jsonSchema)
        println("\n\n")
        print(parser.convertJsonSchemaToIr(jsonSchema))
    }
    */

}
