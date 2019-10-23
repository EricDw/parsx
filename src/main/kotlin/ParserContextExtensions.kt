package com.publicmethod.kparsx

fun <T, U> ParserContext<U>.succeed(
    expected: T,
    newStream: String? = null
): Result<T, U> =
    Success(
        expected,
        copy(
            stream = newStream ?: stream,
            userDefinedState = userDefinedState
        )
    )

fun <T, U> ParserContext<U>.succeed(
    expected: T,
    success: Success<*, U>
): Result<T, U> =
    Success(
        expected,
        copy(
            stream = success.context.stream,
            userDefinedState = userDefinedState
        )
    )

fun <T, U> ParserContext<U>.fail(
    message: String? = "",
    error: Throwable? = null
): Result<T, U> =
    Failure(
        error ?: Throwable(message),
        this
    )
