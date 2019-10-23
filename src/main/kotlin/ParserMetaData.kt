package com.publicmethod.kparsx

data class ParserMetaData(
    val position: Position = Position(),
    override val message: String? = ""
): Throwable()