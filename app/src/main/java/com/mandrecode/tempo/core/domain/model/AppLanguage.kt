package com.mandrecode.tempo.core.domain.model

/**
 * Represents the available app language options.
 * Extensible for future language additions.
 */
enum class AppLanguage {
    /**
     * Follow the system language setting.
     */
    SYSTEM,

    /**
     * English language.
     */
    ENGLISH,

    ;

    companion object {
        /**
         * Convert string to AppLanguage, defaults to SYSTEM if invalid.
         */
        fun fromString(value: String): AppLanguage = entries.find { it.name == value } ?: SYSTEM
    }
}
