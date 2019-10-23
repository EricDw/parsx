package com.publicmethod.kparsx

sealed class Result<out SUCCESS, USER_DEFINED_STATE> {
    abstract val context: ParserContext<USER_DEFINED_STATE>
}

data class Failure<U>(
    val error: Throwable,
    override val context: ParserContext<U>
) : Result<Nothing, U>()

data class Success<S, U>(
    val data: S,
    override val context: ParserContext<U>
) : Result<S, U>()
