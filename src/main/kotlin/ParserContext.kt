package com.publicmethod.kparsx

data class ParserContext<USER_DEFINED_STATE>(
    val stream: String = "",
    val userDefinedState: USER_DEFINED_STATE,
    val originalStream: String = stream,
    val streamName: String = "Character Stream"
) : CharSequence by stream {

    val position: Position = Position(
        streamName
    )

    val head: Char
        get() = stream.first()

    val tail: String
        get() = stream.drop(1)

}
