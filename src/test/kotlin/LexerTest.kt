package com.example.lexer


import io.offscale.lexer.Lexer
import io.offscale.lexer.Token
import io.offscale.lexer.TokenType
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals


const val absPath = "C:\\Users\\jeans\\Desktop\\Parsing\\composeApp\\src\\desktopTest\\kotlin\\"

class LexerTest {

    fun eof(code: String): Token {
        val pos: Int = code.length
        return Token(TokenType.EOF, "", listOf(pos, pos))
    }

    @Test
    fun tokenizerTest() {
        val lexer = Lexer("fun")
        val tokens = lexer.tokenize()
        assertEquals(expected = listOf(Token(TokenType.FUNCTION, "fun", listOf(0, 2)),eof("fun")), actual = tokens)
    }

    @Test
    fun whiteSpacesTest() {
        val oneSpace = " "
        val twoSpaces = "  "
        val threeSpaces = "   "
        val jumpLine = """
            
        """
        assertEquals(
            expected = listOf(Token(TokenType.WHITESPACE, oneSpace, listOf(0, oneSpace.length - 1)), eof(oneSpace)),
            actual = Lexer(oneSpace).tokenize()
        )

        assertEquals(
            expected = listOf(Token(TokenType.WHITESPACE, twoSpaces, listOf(0, twoSpaces.length - 1)), eof(twoSpaces)),
            actual = Lexer(twoSpaces).tokenize()
        )

        assertEquals(
            expected = listOf(
                Token(TokenType.WHITESPACE, threeSpaces, listOf(0, threeSpaces.length - 1)),
                eof(threeSpaces)
            ),
            actual = Lexer(threeSpaces).tokenize()
        )

        assertEquals(
            expected = listOf(Token(TokenType.WHITESPACE, jumpLine, listOf(0, jumpLine.length - 1)), eof(jumpLine)),
            actual = Lexer(jumpLine).tokenize()
        )
    }

    @Test
    fun identifierTest() {
        val identifier = "identifier"
        val identifierWithNumber = "identifier123"
        val identifierWithUndescore = "identifier_123"
        val identifierOneLetter = "a"
        val identifierUnderscore = "_"
        val identifierStartingWithNumber = "123identifier"

        assertEquals(
            expected = listOf(
                Token(TokenType.IDENTIFIER, identifier, listOf(0, identifier.length - 1)),
                eof(identifier)
            ),
            actual = Lexer(identifier).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.IDENTIFIER,
                    identifierWithNumber,
                    listOf(0, identifierWithNumber.length - 1)
                ),
                eof(identifierWithNumber)
            ),
            actual = Lexer(identifierWithNumber).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.IDENTIFIER,
                    identifierWithUndescore,
                    listOf(0, identifierWithUndescore.length - 1)
                ),
                eof(identifierWithUndescore)
            ),
            actual = Lexer(identifierWithUndescore).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.IDENTIFIER,
                    identifierOneLetter,
                    listOf(0, identifierOneLetter.length - 1)
                ),
                eof(identifierOneLetter)
            ),
            actual = Lexer(identifierOneLetter).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.IDENTIFIER,
                    identifierUnderscore,
                    listOf(0, identifierUnderscore.length - 1)
                ),
                eof(identifierUnderscore)
            ),
            actual = Lexer(identifierUnderscore).tokenize()
        )

        assertFailsWith<Exception> {
            Lexer(identifierStartingWithNumber).tokenize()
        }


    }

    @Test
    fun numbersTest() {

        val digit = "1"
        val integer = "123"
        val doubleOneDecimal = "3.1"
        val double = "3.14"
        val doubleExponetialNotation = "1e10"
        val doubleExponetialNotationMinus = "1e-10"
        val doubleExponetialNotationPlus = "1e+10"
        val doubleExponetialNotationUpperCase = "1E10"
        val doubleExponentialNotationUpperCaseMinus = "1E-10"
        val doubleExponentialNotationUpperCasePlus = "1E+10"

        assertEquals(
            expected = listOf(Token(TokenType.INT, digit, listOf(0, 0)),eof(digit)),
            actual = Lexer(digit).tokenize()
        )
        assertEquals(
            expected = listOf(Token(TokenType.INT, integer, listOf(0, integer.length - 1)),eof(integer)),
            actual = Lexer(integer).tokenize()
        )
        assertEquals(
            expected = listOf(Token(TokenType.DOUBLE, doubleOneDecimal, listOf(0, doubleOneDecimal.length - 1)),eof(doubleOneDecimal)),
            actual = Lexer(doubleOneDecimal).tokenize()
        )
        assertEquals(
            expected = listOf(Token(TokenType.DOUBLE, double, listOf(0, double.length - 1)),eof(double)),
            actual = Lexer(double).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.DOUBLE,
                    doubleExponetialNotation,
                    listOf(0, doubleExponetialNotation.length - 1)
                ),
                eof(doubleExponetialNotation)
            ),
            actual = Lexer(doubleExponetialNotation).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.DOUBLE,
                    doubleExponetialNotationMinus,
                    listOf(0, doubleExponetialNotationMinus.length - 1)
                ),
                eof(doubleExponetialNotationMinus)
            ),
            actual = Lexer(doubleExponetialNotationMinus).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.DOUBLE,
                    doubleExponetialNotationPlus,
                    listOf(0, doubleExponetialNotationPlus.length - 1)
                ),
                eof(doubleExponetialNotationPlus)
            ),
            actual = Lexer(doubleExponetialNotationPlus).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.DOUBLE,
                    doubleExponetialNotationUpperCase,
                    listOf(0, doubleExponetialNotationUpperCase.length - 1)
                ),
                eof(doubleExponetialNotationUpperCase)
            ),
            actual = Lexer(doubleExponetialNotationUpperCase).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.DOUBLE,
                    doubleExponentialNotationUpperCaseMinus,
                    listOf(0, doubleExponentialNotationUpperCaseMinus.length - 1)
                ),
                eof(doubleExponentialNotationUpperCaseMinus)
            ),
            actual = Lexer(doubleExponentialNotationUpperCaseMinus).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.DOUBLE,
                    doubleExponentialNotationUpperCasePlus,
                    listOf(0, doubleExponentialNotationUpperCasePlus.length - 1)
                ),
                eof(doubleExponentialNotationUpperCasePlus)
            ),
            actual = Lexer(doubleExponentialNotationUpperCasePlus).tokenize()
        )


    }

    @Test
    fun stringTest() {
        val stringTestTemplate = listOf(
            Token(TokenType.VALUE, "val", listOf(0, 2)),
            Token(TokenType.WHITESPACE, " ", listOf(3, 3)),
            Token(TokenType.IDENTIFIER, "name", listOf(4, 7)),
            Token(TokenType.ATTRIBUTE, "=", listOf(8, 8)),
            Token(TokenType.STRING_START, "\"", listOf(9, 9)),
            Token(TokenType.STRING_CONTENT, "Fulano", listOf(10, 15)),
            Token(TokenType.STRING_END, "\"", listOf(16, 16)),
            Token(TokenType.WHITESPACE, "\r\n", listOf(17, 18)),
            Token(TokenType.VALUE, "val", listOf(19, 21)),
            Token(TokenType.WHITESPACE, " ", listOf(22, 22)),
            Token(TokenType.IDENTIFIER, "string", listOf(23, 28)),
            Token(TokenType.ATTRIBUTE, "=", listOf(29, 29)),
            Token(TokenType.STRING_START, "\"", listOf(30, 30))
        )

        val stringHello = File(absPath + "stringTests/stringHello.txt").readText()
        val stringSimpleInterpolation = File(absPath + "stringTests/stringSimpleInterpolation.txt").readText()
        val stringComplexInterpolation = File(absPath + "stringTests/stringComplexInterpolation.txt").readText()
        val stringScape = File(absPath + "stringTests/stringScape.txt").readText()


        assertEquals(expected = mutableListOf<Token>().apply {
            addAll(stringTestTemplate)
            add(Token(TokenType.STRING_CONTENT, "Hello World", listOf(31, 41)))
            add(Token(TokenType.STRING_END, "\"", listOf(42, 42)))
            add(eof(stringHello))
        }, actual = Lexer(stringHello).tokenize())
        assertEquals(expected = mutableListOf<Token>().apply {
            addAll(stringTestTemplate)
            add(Token(TokenType.STRING_CONTENT, "Hello World ", listOf(31, 42)))
            add(Token(TokenType.STRING_INTERPOLATION, "$", listOf(43, 43)))
            add(Token(TokenType.IDENTIFIER, "name", listOf(44, 47)))
            add(Token(TokenType.STRING_END, "\"", listOf(48, 48)))
            add(eof(stringSimpleInterpolation))
        }, actual = Lexer(stringSimpleInterpolation).tokenize())
        assertEquals(expected = mutableListOf<Token>().apply {
            addAll(stringTestTemplate)
            add(Token(TokenType.STRING_CONTENT, "Hello World ", listOf(31, 42)))
            add(Token(TokenType.STRING_INTERPOLATION, "$", listOf(43, 43)))
            add(Token(TokenType.LBRACE, "{", listOf(44, 44)))
            add(Token(TokenType.IDENTIFIER, "name", listOf(45, 48)))
            add(Token(TokenType.PLUS, "+", listOf(49, 49)))
            add(Token(TokenType.STRING_START, "\"", listOf(50, 50)))
            add(Token(TokenType.STRING_CONTENT, "is", listOf(51, 52)))
            add(Token(TokenType.STRING_END, "\"", listOf(53, 53)))
            add(Token(TokenType.PLUS, "+", listOf(54, 54)))
            add(Token(TokenType.IDENTIFIER, "name", listOf(55, 58)))
            add(Token(TokenType.RBRACE, "}", listOf(59, 59)))
            add(Token(TokenType.STRING_END, "\"", listOf(60, 60)))
            add(eof(stringComplexInterpolation))
        }, actual = Lexer(stringComplexInterpolation).tokenize())
        assertEquals(expected = mutableListOf<Token>().apply {
            addAll(stringTestTemplate)
            add(Token(TokenType.STRING_CONTENT, "Hello World ", listOf(31, 42)))
            add(Token(TokenType.STRING_INTERPOLATION, "$", listOf(43, 43)))
            add(Token(TokenType.IDENTIFIER, "name", listOf(44, 47)))
            add(Token(TokenType.STRING_CONTENT, "\\n", listOf(48, 49)))
            add(Token(TokenType.STRING_END, "\"", listOf(50, 50)))
            add(eof(stringScape))
        } as List<Token>, actual = Lexer(stringScape).tokenize())


    }

    @Test
    fun charTest() {
        val simpleChar = File(absPath + "charTests/simpleChar.txt").readText()
        val charWithUnicode = File(absPath + "charTests/charWithUnicodes.txt").readText()

        assertEquals(
            expected = listOf(
                Token(TokenType.CHAR_START, "\'", listOf(0, 0)),
                Token(TokenType.CHAR, "a", listOf(1, 1)),
                Token(TokenType.CHAR_END, "\'", listOf(2, 2)),
                eof(simpleChar)
            ), actual = Lexer(simpleChar).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(TokenType.CHAR_START, "\'", listOf(0, 0)),
                Token(TokenType.CHAR, "\\u0041", listOf(1, 6)),
                Token(TokenType.CHAR_END, "\'", listOf(7, 7)),
                eof(charWithUnicode)
            ), actual = Lexer(charWithUnicode).tokenize()
        )

    }

    @Test
    fun commentsTest() {
        val commentOneLine = File(absPath + "commentTests/commentOneLine.txt").readText().replace("\r\n", "\n")
        val commentMultiline = File(absPath + "commentTests/commentMultiline.txt").readText().replace("\r\n", "\n")
        val commentInsideComment = File(absPath + "commentTests/commentInsideComment.txt").readText().replace("\r\n","\n")

        assertEquals(
            expected = listOf(
                Token(TokenType.COMMENT, "// This is a simple comment\n", listOf(0, 27)),
                Token(TokenType.COMMENT, "//This is also a simple comment", listOf(28, 58)),
                eof(commentOneLine)
            ),
            actual = Lexer(commentOneLine).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(
                    TokenType.COMMENT, "/* This is a multiline-comment\n" +
                            "This is also a multiline-comment*/", listOf(0, commentMultiline.length - 1)
                ),
                eof(commentMultiline)
            ),
            actual = Lexer(commentMultiline).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(TokenType.COMMENT,commentInsideComment,listOf(0, commentInsideComment.length -1)),
                 eof(commentInsideComment)
                ),
            actual = Lexer(commentInsideComment).tokenize()
            )

    }

    @Test
    fun symbolsTest() {
        val equationSigns = "val=1+3-4/5*6%7"
        val logicSigns = "val==true&&false||!true"

        assertEquals(
            expected = listOf(
                Token(TokenType.VALUE, "val", listOf(0, 2)),
                Token(TokenType.ATTRIBUTE, "=", listOf(3, 3)),
                Token(TokenType.INT, "1", listOf(4, 4)),
                Token(TokenType.PLUS, "+", listOf(5, 5)),
                Token(TokenType.INT, "3", listOf(6, 6)),
                Token(TokenType.SUB, "-", listOf(7, 7)),
                Token(TokenType.INT, "4", listOf(8, 8)),
                Token(TokenType.DIVIDE, "/", listOf(9, 9)),
                Token(TokenType.INT, "5", listOf(10, 10)),
                Token(TokenType.TIMES, "*", listOf(11, 11)),
                Token(TokenType.INT, "6", listOf(12, 12)),
                Token(TokenType.MOD, "%", listOf(13, 13)),
                Token(TokenType.INT, "7", listOf(14, 14)),
                eof(equationSigns)
            ),
            actual = Lexer(equationSigns).tokenize()
        )
        assertEquals(
            expected = listOf(
                Token(TokenType.VALUE, "val", listOf(0, 2)),
                Token(TokenType.EQUALS, "==", listOf(3, 4)),
                Token(TokenType.BOOLEAN, "true", listOf(5, 8)),
                Token(TokenType.AND, "&&", listOf(9, 10)),
                Token(TokenType.BOOLEAN, "false", listOf(11, 15)),
                Token(TokenType.OR, "||", listOf(16, 17)),
                Token(TokenType.EXCLAMATION_MARK, "!", listOf(18, 18)),
                Token(TokenType.BOOLEAN, "true", listOf(19, 22)),
                eof(logicSigns)
            ),
            actual = Lexer(logicSigns).tokenize()
        )
    }

    @Test
    fun scriptsTest() {
        val utils = Lexer("")
        val evenOrOdd = File(absPath + "scripts/evenOrOdd.kt").readText().replace("\r\n", "\n")
        val countTillHundred = File(absPath + "scripts/countTillHundred.kt").readText().replace("\r\n", "\n")


        assertEquals(
            expected =
                listOf(
                    "PACKAGE",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "WHITESPACE",
                    "FUNCTION",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "LPAREN",
                    "IDENTIFIER",
                    "COLON",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "RPAREN",
                    "LBRACE",
                    "WHITESPACE",
                    "IF",
                    "WHITESPACE",
                    "LPAREN",
                    "IDENTIFIER",
                    "WHITESPACE",
                    "MOD",
                    "WHITESPACE",
                    "INT",
                    "WHITESPACE",
                    "EQUALS",
                    "WHITESPACE",
                    "INT",
                    "RPAREN",
                    "LBRACE",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "LPAREN",
                    "STRING_START",
                    "STRING_CONTENT",
                    "STRING_END",
                    "RPAREN",
                    "WHITESPACE",
                    "RBRACE",
                    "WHITESPACE",
                    "ELSE",
                    "WHITESPACE",
                    "LBRACE",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "LPAREN",
                    "STRING_START",
                    "STRING_CONTENT",
                    "STRING_END",
                    "RPAREN",
                    "WHITESPACE",
                    "RBRACE",
                    "WHITESPACE",
                    "RBRACE",
                    "EOF"
                ),
            actual = utils.extractTokenTypes(Lexer(evenOrOdd).tokenize())
        )

        assertEquals(
            expected =
                listOf(
                    "PACKAGE",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "DOT",
                    "IDENTIFIER",
                    "WHITESPACE",
                    "FUNCTION",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "LPAREN",
                    "RPAREN",
                    "LBRACE",
                    "WHITESPACE",
                    "VARIABLE",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "WHITESPACE",
                    "ATTRIBUTE",
                    "WHITESPACE",
                    "INT",
                    "WHITESPACE",
                    "WHILE",
                    "LPAREN",
                    "IDENTIFIER",
                    "LESS_EQUAL",
                    "INT",
                    "RPAREN",
                    "LBRACE",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "LPAREN",
                    "IDENTIFIER",
                    "RPAREN",
                    "WHITESPACE",
                    "IDENTIFIER",
                    "INCREMENT",
                    "WHITESPACE",
                    "RBRACE",
                    "WHITESPACE",
                    "RBRACE",
                    "EOF"
                ),
            actual = utils.extractTokenTypes(Lexer(countTillHundred).tokenize())
        )
    }
}



