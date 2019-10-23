package com.publicmethod.kparsx

data class Position(
    val streamName: String = "Character Stream",
    var index: Int = 0,
    var lineNumber: Int = 1,
    var columnNumber: Int = 0
)
