package com.mandrecode.tempo.core.domain.util

object ValidationUtils {
    const val MAX_TITLE_LENGTH = 500
    const val MAX_DESCRIPTION_LENGTH = 5000
    const val MAX_CATEGORY_NAME_LENGTH = 50
    const val MAX_HABIT_CHAIN_SIZE = 50
    const val MAX_ICON_LENGTH = 50
    const val MAX_COLOR_KEY_LENGTH = 50

    fun validateTitle(
        title: String,
        maxLength: Int = MAX_TITLE_LENGTH,
    ): ValidationResult {
        if (title.isBlank()) return ValidationResult.Empty
        if (title.length > maxLength) return ValidationResult.TooLong
        return ValidationResult.Valid
    }

    fun validateDescription(
        description: String,
        maxLength: Int = MAX_DESCRIPTION_LENGTH,
    ): ValidationResult {
        if (description.length > maxLength) return ValidationResult.TooLong
        return ValidationResult.Valid
    }

    fun validateCategoryName(
        name: String,
        maxLength: Int = MAX_CATEGORY_NAME_LENGTH,
    ): ValidationResult {
        if (name.isBlank()) return ValidationResult.Empty
        if (name.length > maxLength) return ValidationResult.TooLong
        return ValidationResult.Valid
    }

    fun validateHabitChainSize(
        size: Int,
        maxSize: Int = MAX_HABIT_CHAIN_SIZE,
    ): ValidationResult {
        if (size > maxSize) return ValidationResult.TooManyItems
        return ValidationResult.Valid
    }

    fun validateIcon(
        icon: String?,
        maxLength: Int = MAX_ICON_LENGTH,
    ): ValidationResult {
        if (icon != null && icon.length > maxLength) return ValidationResult.TooLong
        return ValidationResult.Valid
    }

    fun validateColorKey(
        key: String?,
        maxLength: Int = MAX_COLOR_KEY_LENGTH,
    ): ValidationResult {
        if (key != null && key.length > maxLength) return ValidationResult.TooLong
        return ValidationResult.Valid
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()

    object Empty : ValidationResult()

    object TooLong : ValidationResult()

    object TooManyItems : ValidationResult()
}
