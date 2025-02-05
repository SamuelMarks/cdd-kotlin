package com.example.parsing.lexer

import java.io.File


// Constants
const val INTERPOLATIONOFF = 0
const val INTERPOLATIONSIMPLE = 1
const val INTERPOLATIONCOMPOSED = 2

enum class TokenType {
    FUNCTION, IDENTIFIER, LPAREN, RPAREN, COLON, LBRACE, RBRACE, ATTRIBUTE, INT, STRING_START,
    STRING_END, STRING_INTERPOLATION, STRING_CONTENT, PLUS, SUB, TIMES, DIVIDE, MOD, INCREMENT,
    DECREMENT, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, NOT_EQUAL, EQUALS, AND, OR, WHITESPACE,
    COMMENT, RETURN, VALUE, VARIABLE, UNKNOWN, AND_BITWISE, OR_BITWISE, COMMA, SEMICOLON, CLASS,
    INTERFACE, ENUM, DOT, DOUBLE, RANGE_DOTS, BOOLEAN, LSQUARE, RSQUARE, RESERVED, RANGE_UNTIL,
    ADD_ASSIGNMENT, SUB_ASSIGNMENT, MOD_ASSIGNMENT, DIV_ASSIGNMENT, MULTI_ASSIGNMENT, ARROW,
    DOUBLE_ARROW, DOUBLE_COLON, DOUBLE_SEMICOLON, AT, EQUALS_REFERENCE, NOT_EQUALS_REFERENCE,
    QUESTION_MARK, CONVERSION, NULL, EXCLAMATION_MARK, OBJECT, TYPEALIAS, CONSTRUCTOR, SEALED,
    IN_KEYWORD, OUT_KEYWORD, SUPER, THIS, TRY, CATCH, FINALLY, THROW, BREAK, CONTINUE, PACKAGE,
    IMPORT, IF, ELSE, WHEN, FOR, WHILE, DO, SUSPEND, COMPION, CHAR_START, CHAR, CHAR_END, EOF
}


data class Token(val type: TokenType, val value: String, val position: List<Int>) {
    override fun toString(): String {
        return "$type"
    }
}

class Lexer(private val sourceCode: String) {

    private val tokens = mutableListOf<Token>()
    private var position = 0
    private val codeLength = sourceCode.length
    private var interpolatedStringMode = INTERPOLATIONOFF

    // Reserved keywords
    private val keywords = mapOf(
        "fun" to TokenType.FUNCTION, "val" to TokenType.VALUE, "var" to TokenType.VARIABLE,
        "return" to TokenType.RETURN, "class" to TokenType.CLASS, "interface" to TokenType.INTERFACE,
        "enum" to TokenType.ENUM, "and" to TokenType.AND_BITWISE, "or" to TokenType.OR_BITWISE,
        "true" to TokenType.BOOLEAN, "false" to TokenType.BOOLEAN, "as" to TokenType.CONVERSION,
        "null" to TokenType.NULL, "object" to TokenType.OBJECT, "typealias" to TokenType.TYPEALIAS,
        "constructor" to TokenType.CONSTRUCTOR, "sealed" to TokenType.SEALED, "in" to TokenType.IN_KEYWORD,
        "out" to TokenType.OUT_KEYWORD, "super" to TokenType.SUPER, "this" to TokenType.THIS,
        "try" to TokenType.TRY, "catch" to TokenType.CATCH, "finally" to TokenType.FINALLY,
        "throw" to TokenType.THROW, "break" to TokenType.BREAK, "continue" to TokenType.CONTINUE,
        "package" to TokenType.PACKAGE, "import" to TokenType.IMPORT, "if" to TokenType.IF,
        "else" to TokenType.ELSE, "when" to TokenType.WHEN, "for" to TokenType.FOR,
        "while" to TokenType.WHILE, "do" to TokenType.DO, "suspend" to TokenType.SUSPEND,
        "companion" to TokenType.COMPION
    )

    // Reserved symbols (excluding comments and strings)
    private val symbols = mapOf(
        "..." to TokenType.RESERVED, "..<" to TokenType.RANGE_UNTIL,
        "===" to TokenType.EQUALS_REFERENCE, "!==" to TokenType.NOT_EQUALS_REFERENCE,
        "==" to TokenType.EQUALS, "!=" to TokenType.NOT_EQUAL,
        "<=" to TokenType.LESS_EQUAL, ">=" to TokenType.GREATER_EQUAL,
        "++" to TokenType.INCREMENT, "--" to TokenType.DECREMENT,
        "&&" to TokenType.AND, "||" to TokenType.OR,
        "+=" to TokenType.ADD_ASSIGNMENT, "-=" to TokenType.SUB_ASSIGNMENT,
        "*=" to TokenType.MULTI_ASSIGNMENT, "/=" to TokenType.DIV_ASSIGNMENT,
        "%=" to TokenType.MOD_ASSIGNMENT, ".." to TokenType.RANGE_DOTS,
        "->" to TokenType.ARROW, "=>" to TokenType.DOUBLE_ARROW,
        "::" to TokenType.DOUBLE_COLON, ";;" to TokenType.DOUBLE_SEMICOLON,
        "(" to TokenType.LPAREN, ")" to TokenType.RPAREN,
        "{" to TokenType.LBRACE, "}" to TokenType.RBRACE,
        "[" to TokenType.LSQUARE, "]" to TokenType.RSQUARE,
        ":" to TokenType.COLON, "=" to TokenType.ATTRIBUTE,
        "+" to TokenType.PLUS, "-" to TokenType.SUB, "?" to TokenType.QUESTION_MARK,
        "*" to TokenType.TIMES, "/" to TokenType.DIVIDE, "%" to TokenType.MOD,
        "<" to TokenType.LESS, ">" to TokenType.GREATER, "@" to TokenType.AT,
        "," to TokenType.COMMA, ";" to TokenType.SEMICOLON, "." to TokenType.DOT,
        "!" to TokenType.EXCLAMATION_MARK
    )

    // Comment delimiters
    private val commentDelimiters = listOf(
        Pair("//", "\n"),  // Single-line comment
        Pair("/*", "*/")  // Multi-line comment
    )

    // String delimiters
    private val stringDelimiters = listOf(
        Pair("\"\"\"", "\"\"\""), // Multi-line strings
        Pair("\"", "\""),  // Single-line strings
        Pair("\'", "\'") // Single-line strings

    )

    private fun isWhiteSpaceChar(position: Int): Boolean {
        return sourceCode[position].isWhitespace()
    }

    private fun isAlphaNumericChar(position: Int): Boolean {
        val char = sourceCode[position]
        return char.isLetterOrDigit() || char == '_'
    }

    private fun isDigitChar(position: Int): Boolean {
        return sourceCode[position].isDigit()
    }

    private fun isDoubleSymbol(position: Int): Boolean {
        return sourceCode[position] in listOf('e', '.', 'E')
    }

    private fun findSymbolMatch(): Pair<String, TokenType>? {
        return symbols.entries.firstOrNull { (symbol, _) ->
            sourceCode.startsWith(symbol, position)
        }?.toPair()
    }

    private fun findMatch(delimiters: List<Pair<String, String>>): Pair<String, String>? {
        return delimiters.firstOrNull { (start, _) ->
            sourceCode.startsWith(start, position)
        }
    }

    private fun extractDelimitedComment(
        startDelimiter: String, endDelimiter: String
    ): Token {
        val start = position
        position += startDelimiter.length
        var commentSymbols = 0

        while (position < codeLength) {
            if (sourceCode.startsWith(endDelimiter, position)) {
                position += endDelimiter.length
                commentSymbols--
                if (commentSymbols<0)
                break
            } else if (sourceCode.startsWith("/*", position)) {
                commentSymbols++
                position += startDelimiter.length
            }

            else {
                position++
            }
        }

        val value = sourceCode.substring(start, position)
        return Token(TokenType.COMMENT, value, listOf(start, position - 1))
    }

    private fun extractDelimitedString(startDelimiter: String, endDelimiter: String) {
        if (startDelimiter == "\'") {
            tokens.add(Token(TokenType.CHAR_START, sourceCode[position].toString(), listOf(position, position)))
            position++

            if (sourceCode[position] == '\\' && sourceCode[position + 1] == 'u') {
                // Process Unicode escape sequence
                val unicodeSequence = sourceCode.substring(position, position + 6) // e.g., '\u1234'
                if (unicodeSequence.matches(Regex("""\\u[0-9a-fA-F]{4}"""))) {
                    tokens.add(Token(TokenType.CHAR, unicodeSequence, listOf(position, position + 5)))
                    position += 6 // Move past the Unicode sequence
                } else {
                    throw IllegalArgumentException("Invalid Unicode escape sequence at position $position")
                }
            } else {
                // Regular character
                tokens.add(Token(TokenType.CHAR, sourceCode[position].toString(), listOf(position, position)))
                position++
            }

            tokens.add(Token(TokenType.CHAR_END, sourceCode[position].toString(), listOf(position, position)))
            position++
            return
        }
        var start = position
        position += startDelimiter.length
        tokens.add(Token(TokenType.STRING_START, sourceCode.substring(start, position), listOf(start, position - 1)))
        start = position
        while (position < codeLength) {
            if (sourceCode.startsWith(endDelimiter, position)) {
                if (position != start) {
                    tokens.add(
                        Token(
                            TokenType.STRING_CONTENT,
                            sourceCode.substring(start, position),
                            listOf(start, position - 1)
                        )
                    )
                }
                tokens.add(
                    Token(
                        TokenType.STRING_END,
                        sourceCode.substring(position, position + endDelimiter.length),
                        listOf(position, position + endDelimiter.length - 1)
                    )
                )
                position += endDelimiter.length
                break
            } else if (sourceCode[position] == '\\') {
                // Handle escape character
                position += 2
                // Handle String Interpolation
            } else if (sourceCode[position] == '\$') {
                if (position != start) {
                    tokens.add(
                        Token(
                            TokenType.STRING_CONTENT,
                            sourceCode.substring(start, position),
                            listOf(start, position - 1)
                        )
                    )
                }
                tokens.add(Token(TokenType.STRING_INTERPOLATION, "\$", listOf(position, position)))
                position++
                if (position < codeLength && sourceCode[position] == '{') {
                    tokens.add(Token(TokenType.LBRACE, "{", listOf(position, position)))
                    interpolatedStringMode = INTERPOLATIONCOMPOSED
                    position++
                    tokenize()
                    start = position
                } else {
                    interpolatedStringMode = INTERPOLATIONSIMPLE
                    tokenize()
                    start = position
                }


            } else {
                position++
            }
        }
    }

    fun extractTokenTypes(tokens: List<Token>): List<String> {
        return tokens.map { it.type.toString() }
    }

    fun tokenize(): List<Token> {
        while (position < codeLength) {
            when {
                // Handle Whitespace
                isWhiteSpaceChar(position) -> {
                    val start = position
                    while (position < codeLength && isWhiteSpaceChar(position)) {
                        position++
                    }
                    tokens.add(
                        Token(
                            TokenType.WHITESPACE,
                            sourceCode.substring(start, position),
                            listOf(start, position - 1)
                        )
                    )
                }

                // Handle Comments
                findMatch(commentDelimiters) != null -> {
                    val (startDelimiter, endDelimiter) = findMatch(commentDelimiters)
                        ?: error("Error handling the comments")
                    tokens.add(extractDelimitedComment(startDelimiter, endDelimiter))

                }

                // Handle Strings
                findMatch(stringDelimiters) != null -> {
                    val (startDelimiter, endDelimiter) = findMatch(stringDelimiters)
                        ?: error("Error handling the strings")
                    extractDelimitedString(startDelimiter, endDelimiter)
                }

                // Handle Numbers
                isDigitChar(position) -> {
                    val start = position
                    var isDouble = false
                    while (position < codeLength && (isDigitChar(position) || !isDouble && isDoubleSymbol(position))) {
                        // Handle range
                        if (codeLength >= position + 3) {
                            if (sourceCode.substring(position + 1, position + 3) == "..") {
                                position++
                                break
                            } else if (isDoubleSymbol(position)) {
                                isDouble = true
                                if (sourceCode[position] in listOf('e', 'E') && sourceCode[position + 1] in listOf(
                                        '+',
                                        '-'
                                    )
                                ) {
                                    position += 1
                                }
                            }
                        } else if (isDoubleSymbol(position)) {
                            isDouble = true
                            if (sourceCode[position] in listOf('e', 'E') && sourceCode[position + 1] in listOf(
                                    '+',
                                    '-'
                                )
                            ) {
                                position += 1
                            }
                        }

                        position++

                    }
                    if (position < codeLength) {
                        if (isAlphaNumericChar(position))
                            throw Exception("Identifiers that start with a number are not allowed position:$position")
                    }
                    tokens.add(
                        Token(
                            if (isDouble) TokenType.DOUBLE else TokenType.INT,
                            sourceCode.substring(start, position), listOf(start, position - 1)
                        )
                    )
                }

                // Handle Identifiers and Keywords
                isAlphaNumericChar(position) -> {
                    val start = position
                    while (position < codeLength && isAlphaNumericChar(position)) {
                        position++
                    }
                    val value = sourceCode.substring(start, position)
                    val type = keywords[value] ?: TokenType.IDENTIFIER
                    tokens.add(Token(type, value, listOf(start, position - 1)))
                    if (interpolatedStringMode == INTERPOLATIONSIMPLE) {
                        interpolatedStringMode = INTERPOLATIONOFF
                        break
                    }
                }


                // Handle Symbols
                findSymbolMatch() != null -> {
                    val (symbol, type) = findSymbolMatch()!!
                    tokens.add(Token(type, symbol, listOf(position, position + symbol.length - 1)))
                    position += symbol.length
                    if (type == TokenType.RBRACE && interpolatedStringMode == INTERPOLATIONCOMPOSED) {
                        interpolatedStringMode = INTERPOLATIONOFF
                        break
                    }
                }

                // Unknown or unsupported characters
                else -> {
                    tokens.add(
                        Token(
                            TokenType.UNKNOWN,
                            sourceCode[position].toString(),
                            listOf(position, position + sourceCode[position].toString().length - 1)
                        )
                    )
                    position++
                }
            }
        }
        if (position >= codeLength) tokens.add(Token(TokenType.EOF, "", listOf(position, position)))
        return tokens
    }
}

fun main() {
    val filePath =
        "C:\\Users\\jeans\\Desktop\\Parsing\\composeApp\\src\\desktopMain\\kotlin\\com\\example\\parsing\\lexer\\test_input.txt"
    val code = File(filePath).readText().replace("\r\n", "\n")
    val lexer = Lexer(code)
    val tokens = lexer.tokenize()
    val tokensTypes = lexer.extractTokenTypes(tokens)


    tokensTypes.forEach {
        println(it)
    }

}