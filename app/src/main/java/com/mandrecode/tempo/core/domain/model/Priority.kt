package com.mandrecode.tempo.core.domain.model

enum class Priority(
    val sortOrder: Int,
) {
    HIGH(0),
    MEDIUM(1),
    LOW(2),
    ;

    companion object {
        val priorities = entries
    }
}
