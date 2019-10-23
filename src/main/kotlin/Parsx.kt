package com.publicmethod.kparsx

typealias Parser<T, U> = ParserContext<U>.() -> Result<T, U>

fun charOf(chars: CharSequence) = chars.first()

fun <T, U> runP(
    parser: Parser<T, U>,
    context: ParserContext<U>
) = parser(context)

infix fun <T, U> Parser<T, U>.runWith(
    context: ParserContext<U>
) = runP(this, context)

fun <U> pChar(
    expected: Char
): Parser<Char, U> = {
    when {
        isEmpty() -> {
            val metaData = ParserMetaData(
                position = position,
                message = """Expected $expected, reached end of stream: $streamName"""
            )
            fail(
                error = metaData
            )
        }

        else      ->
            when (val head = head) {
                expected -> {
                    position.apply { columnNumber++ }
                    succeed(
                        head, tail
                    )
                }
                else     -> {
                    position.apply { columnNumber++ }
                    val metaData = ParserMetaData(
                        position = position,
                        message = "Expected $expected, got $head"
                    )
                    fail(
                        error = metaData
                    )
                }
            }
    }
}

fun <U> pNot(
    expected: Char
): Parser<Char, U> = {

    if (isEmpty()) fail("""Expected $expected, reached end of stream: $streamName""")
    else when (val result = pChar<U>(expected)()) {
        is Failure -> succeed(result.context.stream.first(), stream.drop(1))
        is Success -> fail("Expected: $expected to not match but instead found ${stream.first()}")
    }
}

fun <T, U> orParse(
    parser1: Parser<T, U>, parser2: Parser<T, U>, context: ParserContext<U>
): Result<T, U> {
    return when (val first = parser1(context)) {
        is Success -> first
        is Failure -> parser2(context)
    }
}

operator fun <T, U> Parser<T, U>.div(other: Parser<T, U>): Parser<T, U> = {
    orParse(this@div, other, this)
}

infix fun <T, U> Parser<T, U>.OR(other: Parser<T, U>): Parser<T, U> = {
    orParse(this@OR, other, this)
}

fun <T, U> choiceOf(vararg parsers: Parser<T, U>): Parser<T, U> =
    parsers.reduce { acc, parser ->
        acc / parser
    }

fun <U> Char.toParser(): Parser<Char, U> =
    pChar(expected = this)

fun <U> anyCharOf(validChars: CharSequence) =
    validChars
        .map { pChar<U>(it) }
        .toTypedArray()
        .run { choiceOf(*this) }

fun <U> anyCharOf(validChars: CharRange) =
    validChars
        .map { pChar<U>(it) }
        .toTypedArray()
        .run { choiceOf(*this) }

infix fun CharSequence.rangeTo(end: CharSequence): CharSequence =
    CharRange(
        charOf(this),
        charOf(end)
    ).joinTo(StringBuilder(), "")

infix fun <A, B, U> Parser<A, U>._AND_(
    other: Parser<B, U>
): Parser<Pair<A, B>, U> = {
    when (val first = this@_AND_()) {
        is Failure -> first
        is Success -> {
            when (val second = first.context.other()) {
                is Failure -> second
                is Success -> succeed(
                    first.data to second.data,
                    second
                )
            }
        }
    }
}

operator fun <A, B, U> Parser<A, U>.plus(other: Parser<B, U>) =
    this _AND_ other

infix fun <A, B, U> Parser<A, U>._AND(
    other: Parser<B, U>
): Parser<A, U> = {
    when (val first = this@_AND()) {
        is Failure -> fail(first.error.message)
        is Success -> {
            when (val second = first.context.other()) {
                is Failure -> fail(second.error.message)
                is Success -> succeed(
                    first.data, second
                )
            }
        }
    }
}

infix fun <A, B, U> Parser<A, U>.AND_(
    other: Parser<B, U>
): Parser<B, U> = {
    when (val r1 = this@AND_()) {
        is Failure -> fail(r1.error.message)
        is Success -> {
            when (val r2 = r1.context.other()) {
                is Failure -> fail(r2.error.message)
                is Success -> succeed(
                    r2.data, r2
                )
            }
        }
    }
}

fun <A, B, U> mapParser(
    transform: (A) -> B,
    parser: Parser<A, U>
): Parser<B, U> = {
    when (val result = parser()) {
        is Failure -> result
        is Success -> succeed(
            transform(result.data), result
        )
    }

}

fun <A, B, U> Parser<A, U>.map(
    transform: (A) -> B
): Parser<B, U> = mapParser(transform, this)

fun <A, B, U> Parser<(A) -> B, U>.apply(other: Parser<A, U>): Parser<B, U> =
    (this + other).map { (r1: (A) -> B, r2: A) ->
        r1(r2)
    }

fun <A, B, U> applyParser(p1: Parser<(A) -> B, U>, p2: Parser<A, U>): Parser<B, U> =
    (p1 + p2).map { (r1: (A) -> B, r2: A) ->
        r1(r2)
    }

operator fun <A, B, U> Parser<(A) -> B, U>.times(other: Parser<A, U>) =
    this.apply(other)

fun <T, U> T.just(): Parser<T, U> = {
    succeed(this@just)
}

fun <T, U> justParser(t: T): Parser<T, U> = {
    succeed(t)
}

fun <A, B, C, U> ((Pair<A, B>) -> C).lift(
    parser1: Parser<A, U>,
    parser2: Parser<B, U>
): Parser<C, U> = {
    when (val result = (parser1 + parser2)(this)) {
        is Failure -> result
        is Success -> {
            result.data.run {
                succeed(invoke(this), result)
            }
        }
    }
}

fun <T, U> List<Parser<T, U>>.sequence(): Parser<List<T>, U> = {

    tailrec fun ParserContext<U>.sequencer(
        accumulator: MutableList<T> = mutableListOf(),
        parsers: List<Parser<T, U>>
    ): Result<List<T>, U> {

        if (parsers.isEmpty()) return succeed(accumulator)

        val head = parsers.first()
        val tail = parsers.drop(1)

        return when (val result = head(this)) {
            is Failure -> result
            is Success -> {
                val newData = accumulator.apply {
                    add(result.data)
                }
                result.context.sequencer(newData, tail)
            }
        }
    }

    sequencer(parsers = this@sequence)

}

fun <U> pString(expectedSequence: String): Parser<String, U> =
    expectedSequence.map {
        it.toParser<U>()
    }.sequence().map {
        it.joinToString("")
    }

fun <U> pRegex(expected: Regex): Parser<MatchResult, U> = {

    if (stream.isEmpty()) fail<MatchResult, U>("Expected: ${expected.pattern} but stream is empty")

    class OutPut(
        val accumulator: String,
        val newStream: String,
        val matchResult: MatchResult?
    )

    val result: OutPut = stream.fold(
        OutPut("", stream, null)
    ) { output, char ->
        val accumulator = output.accumulator + char
        val newStream = output.newStream.drop(1)
        val matchResult = expected.matchEntire(accumulator)
        OutPut(
            accumulator = accumulator,
            newStream = newStream,
            matchResult = matchResult
        )
    }

    result.matchResult?.let {
        succeed(it, result.newStream)
    } ?: fail("Expected stream: $stream to match pattern: ${expected.pattern} found nothing")

}

fun <U> aSpace(): Parser<Char, U> =
    pChar(charOf(" "))

fun <U> pSpaces(): Parser<Unit, U> = {

    val newStream = stream.dropWhile { it == charOf(" ") }

    succeed(Unit, newStream)
}

fun <U> seqSpace(
    expected: String
): Parser<String, U> =
    pString<U>(expected) _AND aSpace()


fun <U> pCharsUntil(
    expected: Char
): Parser<String, U> = {

    tailrec fun ParserContext<U>.notLoop(
        accumulator: String = "",
        newStream: String = ""
    ): Pair<String, String> = when (val result = pNot<U>(expected)(this@notLoop)) {
        is Failure -> accumulator to newStream
        is Success -> result.context.notLoop(accumulator + result.data, this@notLoop.stream)
    }

    val result = notLoop()

    if (result.first.isEmpty())
        fail("Nothing found that does not match $expected")
    else succeed(result.first, result.second.drop(1))

}

fun <T, U> pBetween(
    leftParser: Parser<*, U>,
    middleParser: Parser<T, U>,
    rightParser: Parser<*, U>
): Parser<T, U> = {

    when (val leftResult = leftParser()) {
        is Failure -> leftResult
        is Success -> (middleParser _AND rightParser)(leftResult.context)
    }

}

