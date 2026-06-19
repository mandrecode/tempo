package com.mandrecode.tempo.core.ui.theme

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mandrecode.tempo.R

/**
 * Represents available icons across the app (habits, categories, etc.)
 * Each icon has an identifier string and keywords for smart matching.
 */
enum class TempoIcon(
    val iconName: String,
    @DrawableRes val iconRes: Int,
    @StringRes val keywordsRes: Int,
) {
    // Fitness & Sports
    FITNESS(
        "fitness",
        R.drawable.ic_fitness_center,
        R.string.keywords_fitness,
    ),
    RUN(
        "run",
        R.drawable.ic_directions_run,
        R.string.keywords_run,
    ),
    WALK(
        "walk",
        R.drawable.ic_directions_walk,
        R.string.keywords_walk,
    ),
    SPORTS(
        "sports",
        R.drawable.ic_sports,
        R.string.keywords_sports,
    ),

    // Health & Wellness
    HEALTH(
        "health",
        R.drawable.ic_health_cross,
        R.string.keywords_health,
    ),
    HEART(
        "heart",
        R.drawable.ic_heart,
        R.string.keywords_heart,
    ),
    MOOD(
        "mood",
        R.drawable.ic_mood,
        R.string.keywords_mood,
    ),
    SPA(
        "spa",
        R.drawable.ic_spa,
        R.string.keywords_spa,
    ),

    // Food & Nutrition
    RESTAURANT(
        "restaurant",
        R.drawable.ic_restaurant,
        R.string.keywords_restaurant,
    ),
    COFFEE(
        "coffee",
        R.drawable.ic_coffee,
        R.string.keywords_coffee,
    ),
    WATER(
        "water",
        R.drawable.ic_water,
        R.string.keywords_water,
    ),

    // Productivity & Work
    WORK(
        "work",
        R.drawable.ic_work,
        R.string.keywords_work,
    ),
    SCHOOL(
        "school",
        R.drawable.ic_school,
        R.string.keywords_school,
    ),
    BOOK(
        "book",
        R.drawable.ic_book,
        R.string.keywords_book,
    ),
    CREATE(
        "create",
        R.drawable.ic_draw,
        R.string.keywords_create,
    ),

    // Home & Daily Life
    HOME(
        "home",
        R.drawable.ic_home,
        R.string.keywords_home,
    ),
    CALENDAR(
        "calendar",
        R.drawable.ic_calendar,
        R.string.keywords_calendar,
    ),
    BED(
        "bed",
        R.drawable.ic_bed,
        R.string.keywords_bed,
    ),

    // Communication & Social
    CALL(
        "call",
        R.drawable.ic_call,
        R.string.keywords_call,
    ),
    EMAIL(
        "email",
        R.drawable.ic_mail,
        R.string.keywords_email,
    ),

    // Entertainment & Hobbies
    MUSIC(
        "music",
        R.drawable.ic_music_cast,
        R.string.keywords_music,
    ),
    MUSIC_NOTE(
        "music_note",
        R.drawable.ic_music_note_2,
        R.string.keywords_music_note,
    ),
    GAME(
        "game",
        R.drawable.ic_videogame_asset,
        R.string.keywords_game,
    ),
    MOVIE(
        "movie",
        R.drawable.ic_movie,
        R.string.keywords_movie,
    ),

    // Personal Development
    STAR(
        "star",
        R.drawable.ic_star,
        R.string.keywords_star,
    ),
    TROPHY(
        "trophy",
        R.drawable.ic_trophy,
        R.string.keywords_trophy,
    ),
    TARGET(
        "target",
        R.drawable.ic_flag,
        R.string.keywords_target,
    ),

    // Miscellaneous
    LIST(
        "list",
        R.drawable.ic_list,
        R.string.keywords_list,
    ),
    REMIND(
        "remind",
        R.drawable.ic_notifications,
        R.string.keywords_remind,
    ),
    CATEGORY(
        "category",
        R.drawable.ic_category,
        R.string.keywords_category,
    ),
    INBOX(
        "inbox",
        R.drawable.ic_inbox,
        R.string.keywords_inbox,
    ),
    PETS(
        "pets",
        R.drawable.ic_pets,
        R.string.keywords_pets,
    ),
    ROUTINE(
        "routine",
        R.drawable.ic_routine,
        R.string.keywords_routine,
    ),
    SHIELD(
        "shield",
        R.drawable.ic_shield,
        R.string.keywords_shield,
    ),
    PSYCHIATRY(
        "psychiatry",
        R.drawable.ic_psychiatry,
        R.string.keywords_psychiatry,
    ),
    SMOKE_FREE(
        "smoke_free",
        R.drawable.ic_smoke_free,
        R.string.keywords_smoke_free,
    ),
    ;

    companion object {
        fun fromName(name: String?): TempoIcon? {
            if (name == null) return null
            return entries.find { it.iconName == name }
        }

        fun getAllIcons(): List<TempoIcon> = entries

        fun suggestIcon(
            text: String,
            context: Context,
        ): TempoIcon? {
            val lowercaseText = text.lowercase()
            return entries.firstOrNull { icon ->
                val keywords = context.getString(icon.keywordsRes).split(",").map { it.trim() }
                keywords.any { keyword ->
                    lowercaseText.contains(keyword)
                }
            }
        }
    }
}
