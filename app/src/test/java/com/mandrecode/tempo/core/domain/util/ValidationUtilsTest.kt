package com.mandrecode.tempo.core.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ValidationUtilsTest {
    @Test
    fun `validateTitle returns Valid for valid title`() {
        val title = "Valid Title"
        val result = ValidationUtils.validateTitle(title)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateTitle returns Empty for blank title`() {
        val title = "   "
        val result = ValidationUtils.validateTitle(title)
        assertThat(result).isInstanceOf(ValidationResult.Empty::class.java)
    }

    @Test
    fun `validateTitle returns TooLong for long title`() {
        val title = "a".repeat(ValidationUtils.MAX_TITLE_LENGTH + 1)
        val result = ValidationUtils.validateTitle(title)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }

    @Test
    fun `validateTitle returns TooLong for title exceeding custom length`() {
        val title = "abcd"
        val result = ValidationUtils.validateTitle(title, maxLength = 3)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }

    @Test
    fun `validateDescription returns Valid for valid description`() {
        val description = "Valid Description"
        val result = ValidationUtils.validateDescription(description)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateDescription returns TooLong for long description`() {
        val description = "a".repeat(ValidationUtils.MAX_DESCRIPTION_LENGTH + 1)
        val result = ValidationUtils.validateDescription(description)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }

    @Test
    fun `validateDescription returns TooLong for description exceeding custom length`() {
        val description = "abcd"
        val result = ValidationUtils.validateDescription(description, maxLength = 3)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }

    @Test
    fun `validateCategoryName returns Valid for valid name`() {
        val name = "Valid Category"
        val result = ValidationUtils.validateCategoryName(name)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateCategoryName returns Empty for blank name`() {
        val name = ""
        val result = ValidationUtils.validateCategoryName(name)
        assertThat(result).isInstanceOf(ValidationResult.Empty::class.java)
    }

    @Test
    fun `validateCategoryName returns TooLong for long name`() {
        val name = "a".repeat(ValidationUtils.MAX_CATEGORY_NAME_LENGTH + 1)
        val result = ValidationUtils.validateCategoryName(name)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }

    @Test
    fun `validateCategoryName returns TooLong for name exceeding custom length`() {
        val name = "abcd"
        val result = ValidationUtils.validateCategoryName(name, maxLength = 3)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }

    @Test
    fun `validateHabitChainSize returns Valid for valid size`() {
        val size = ValidationUtils.MAX_HABIT_CHAIN_SIZE
        val result = ValidationUtils.validateHabitChainSize(size)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateHabitChainSize returns TooManyItems for large size`() {
        val size = ValidationUtils.MAX_HABIT_CHAIN_SIZE + 1
        val result = ValidationUtils.validateHabitChainSize(size)
        assertThat(result).isInstanceOf(ValidationResult.TooManyItems::class.java)
    }

    @Test
    fun `validateHabitChainSize returns TooManyItems for size exceeding custom limit`() {
        val size = 10
        val result = ValidationUtils.validateHabitChainSize(size, maxSize = 5)
        assertThat(result).isInstanceOf(ValidationResult.TooManyItems::class.java)
    }

    @Test
    fun `validateIcon returns Valid for null icon`() {
        val result = ValidationUtils.validateIcon(null)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateIcon returns Valid for valid icon name`() {
        val icon = "fitness"
        val result = ValidationUtils.validateIcon(icon)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateIcon returns TooLong for long icon name`() {
        val icon = "a".repeat(ValidationUtils.MAX_ICON_LENGTH + 1)
        val result = ValidationUtils.validateIcon(icon)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }

    @Test
    fun `validateColorKey returns Valid for null key`() {
        val result = ValidationUtils.validateColorKey(null)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateColorKey returns Valid for valid key`() {
        val key = "pastel_red"
        val result = ValidationUtils.validateColorKey(key)
        assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
    }

    @Test
    fun `validateColorKey returns TooLong for long key`() {
        val key = "a".repeat(ValidationUtils.MAX_COLOR_KEY_LENGTH + 1)
        val result = ValidationUtils.validateColorKey(key)
        assertThat(result).isInstanceOf(ValidationResult.TooLong::class.java)
    }
}
