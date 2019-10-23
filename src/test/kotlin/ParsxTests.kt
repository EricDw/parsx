package com.publicmethod.kparsx

import org.junit.Assert.assertEquals
import org.junit.Test

class ParsxTests {

    @Test
    fun `given car when expectChar then return Success of c and ar`() {
        // Arrange
        val inputChar = "c"
        val context = ParserContext("car", Unit)
        val expected = context.succeed(
            charOf(inputChar), "ar"
        )

        // Act
        val actual = pChar<Unit>(
            charOf(inputChar)
        )(context)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `c or t or b OR parser matches c or t or b`() {
        // Arrange
        val c = charOf("c")
        val t = charOf("t")
        val b = charOf("b")

        val context1 = ParserContext("car", Unit)
        val context2 = ParserContext("tar", Unit)
        val context3 = ParserContext("bar", Unit, streamName = "Stream 3")

        val expected = context1.succeed(
            c, "ar"
        )

        val expected2 = context2.succeed(
            t, "ar"
        )

        val expected3 = context3.succeed(
            b, "ar"
        )

        val cParser = c.toParser<Unit>()
        val tParser = t.toParser<Unit>()
        val bParser = b.toParser<Unit>()

        val cOrTOrbParser = cParser / tParser / bParser

        // Act
        var actual = cOrTOrbParser(context1)

        // Assert
        assertEquals(expected, actual)

        // Act
        actual = cOrTOrbParser(context2)

        // Assert
        assertEquals(expected2, actual)

        // Act
        actual = cOrTOrbParser(context3)

        // Assert
        assertEquals(expected3, actual)
    }

    @Test
    fun `choice parser matches c or t or b`() {
        // Arrange
        val c = charOf("c")
        val t = charOf("t")
        val b = charOf("b")

        val cParser = c.toParser<Unit>()
        val tParser = t.toParser<Unit>()
        val bParser = b.toParser<Unit>()

        val context1 = ParserContext("car", Unit)
        val context2 = ParserContext("tar", Unit)
        val context3 = ParserContext("bar", Unit)

        val expected = context1.succeed(
            c, "ar"
        )

        val expected2 = context2.succeed(
            t, "ar"
        )

        val expected3 = context3.succeed(
            b, "ar"
        )

        val cOrTOrBChoice = choiceOf(cParser, tParser, bParser)
        // Act
        var actual = cOrTOrBChoice(context1)

        // Assert
        assertEquals(expected, actual)

        // Act
        actual = cOrTOrBChoice(context2)

        // Assert
        assertEquals(expected2, actual)

        // Act
        actual = cOrTOrBChoice(context3)

        // Assert
        assertEquals(expected3, actual)
    }

    @Test
    fun `anyCharOf c or t or b matches c or t or b`() {
        // Arrange
        val c = "c"
        val t = "t"
        val b = "b"

        val context1 = ParserContext("car", Unit)
        val context2 = ParserContext("tar", Unit)
        val context3 = ParserContext("bar", Unit)

        val expected = context1.succeed(
            charOf(c), "ar"
        )

        val expected2 = context2.succeed(
            charOf(t), "ar"
        )

        val expected3 = context3.succeed(
            charOf(b), "ar"
        )

        val aToz = anyCharOf<Unit>("a" rangeTo "z")
        // Act
        var actual = aToz(context1)

        // Assert
        assertEquals(expected, actual)

        // Act
        actual = aToz(context2)

        // Assert
        assertEquals(expected2, actual)

        // Act
        actual = aToz(context3)

        // Assert
        assertEquals(expected3, actual)
    }

    @Test
    fun `anyCharOf c or t or b CharRange matches c or t or b`() {
        // Arrange
        val c = "c"
        val t = "t"
        val b = "b"

        val context1 = ParserContext("car", Unit)
        val context2 = ParserContext("tar", Unit)
        val context3 = ParserContext("bar", Unit)

        val expected = context1.succeed(
            charOf(c), "ar"
        )

        val expected2 = context2.succeed(
            charOf(t), "ar"
        )

        val expected3 = context3.succeed(
            charOf(b), "ar"
        )

        val aToz = anyCharOf<Unit>(
            CharRange(
                charOf("a"),
                charOf("z")
            )
        )
        // Act
        var actual = aToz(context1)

        // Assert
        assertEquals(expected, actual)

        // Act
        actual = aToz(context2)

        // Assert
        assertEquals(expected2, actual)

        // Act
        actual = aToz(context3)

        // Assert
        assertEquals(expected3, actual)
    }

    @Test
    fun `andParse c and b and t matches c and b and t`() {
        // Arrange
        val c = charOf("c")
        val t = charOf("t")
        val b = charOf("b")

        val cParser = c.toParser<Unit>()
        val tParser = t.toParser<Unit>()
        val bParser = b.toParser<Unit>()

        val context = ParserContext("ctbar", Unit)

        val expected = Success(
            Pair(Pair(c, t), b),
            context.copy("ar")
        )

        // Act
        val actual = (cParser + tParser + bParser)(context)


        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `land c and b takes c and b but returns only c with t remaining`() {
        // Arrange
        val c = charOf("c")
        val b = charOf("b")
        val cParser = c.toParser<Unit>()
        val bParser = b.toParser<Unit>()

        val context = ParserContext("cbt", Unit)

        val expected = context.succeed(
            c, "t"
        )

        // Act
        val actual = (cParser _AND bParser)(context)


        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun ` c _AND b matches c and b but returns only c with t remaining`() {
        // Arrange
        val c = charOf("c")
        val b = charOf("b")
        val cParser = c.toParser<Unit>()
        val bParser = b.toParser<Unit>()

        val context = ParserContext("cbt", Unit)

        val expected = context.succeed(
            c, "t"
        )

        // Act
        val actual = (cParser _AND bParser)(context)


        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `c AND_ b matches c and b but returns only b with t remaining`() {
        // Arrange
        val c = charOf("c")
        val b = charOf("b")
        val cParser = c.toParser<Unit>()
        val bParser = b.toParser<Unit>()

        val context = ParserContext("cbt", Unit)

        val expected = context.succeed(
            b, "t"
        )

        // Act
        val actual = (cParser AND_ bParser)(context)

        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `expectSequence extracts first string in string`() {
        // Arrange
        val commandName = "Show-Apps"

        val context = ParserContext("Show-Apps -Foreground green", Unit)

        val expected = context.succeed(
            commandName, " -Foreground green"
        )

        // Act
        val actual = pString<Unit>(commandName)(context)

        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `expectSequence extracts either string from a string`() {
        // Arrange
        val commandName = "Show-Apps"
        val commandName2 = "Clear-Host"

        val context = ParserContext("$commandName2 -Foreground green", Unit)

        val expected = context.succeed(
            commandName2, " -Foreground green"
        )

        // Act
        val actual = (pString<Unit>(commandName) / pString(
            commandName2
        ))(context)

        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `aSpace matches space char with a remaining`() {
        // Arrange
        val space = charOf(" ")

        val context = ParserContext(" a", Unit)

        val expected = context.succeed(
            space, "a"
        )

        // Act
        val actual = aSpace<Unit>()(context)

        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `pSpaces matches any amount of spaces with a remaining`() {
        // Arrange
        val context = ParserContext("     a", Unit)

        val expected = context.succeed(
            Unit, "a"
        )

        // Act
        val actual = pSpaces<Unit>()(context)

        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `pCharsUntil matches any amount of spaces with a remaining`() {
        // Arrange
        val context = ParserContext("Thing ", Unit)

        val expected = context.succeed(
            "Thing", " "
        )

        // Act
        val actual = pCharsUntil<Unit>(charOf(" "))(context)

        // Assert
        assertEquals(expected, actual)

    }

    @Test
    fun `seqSpace matches a String followed by a space a remaining`() {
        // Arrange
        val commandName = "Show-Apps"
        val context = ParserContext("$commandName a", Unit)

        val expected = context.succeed(
            commandName, "a"
        )

        // Act
        val actual = seqSpace<Unit>(commandName)(context)

        // Assert
        assertEquals(expected, actual)

    }

}