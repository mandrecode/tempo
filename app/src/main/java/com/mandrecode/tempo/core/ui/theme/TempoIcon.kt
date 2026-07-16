package com.mandrecode.tempo.core.ui.theme

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mandrecode.tempo.R
import kotlin.random.Random

/**
 * Represents available icons across the app (habits, categories, etc.)
 * Each icon has an identifier string and keywords for smart matching.
 */
enum class TempoIcon(
    val iconName: String,
    @DrawableRes val iconRes: Int,
    @StringRes val keywordsRes: Int,
    val category: IconCategory,
) {
    FITNESS(
        "fitness",
        R.drawable.ic_fitness_center,
        R.string.keywords_fitness,
        IconCategory.FITNESS_SPORTS,
    ),
    RUN(
        "run",
        R.drawable.ic_directions_run,
        R.string.keywords_run,
        IconCategory.FITNESS_SPORTS,
    ),
    WALK(
        "walk",
        R.drawable.ic_directions_walk,
        R.string.keywords_walk,
        IconCategory.FITNESS_SPORTS,
    ),
    SPORTS(
        "sports",
        R.drawable.ic_sports,
        R.string.keywords_sports,
        IconCategory.FITNESS_SPORTS,
    ),
    HEALTH(
        "health",
        R.drawable.ic_health_cross,
        R.string.keywords_health,
        IconCategory.HEALTH_WELLNESS,
    ),
    HEART(
        "heart",
        R.drawable.ic_heart,
        R.string.keywords_heart,
        IconCategory.HEALTH_WELLNESS,
    ),
    MOOD(
        "mood",
        R.drawable.ic_mood,
        R.string.keywords_mood,
        IconCategory.HEALTH_WELLNESS,
    ),
    SPA(
        "spa",
        R.drawable.ic_spa,
        R.string.keywords_spa,
        IconCategory.HEALTH_WELLNESS,
    ),
    RESTAURANT(
        "restaurant",
        R.drawable.ic_restaurant,
        R.string.keywords_restaurant,
        IconCategory.FOOD_NUTRITION,
    ),
    COFFEE(
        "coffee",
        R.drawable.ic_coffee,
        R.string.keywords_coffee,
        IconCategory.FOOD_NUTRITION,
    ),
    WATER(
        "water",
        R.drawable.ic_water,
        R.string.keywords_water,
        IconCategory.FOOD_NUTRITION,
    ),
    WORK(
        "work",
        R.drawable.ic_work,
        R.string.keywords_work,
        IconCategory.PRODUCTIVITY_WORK,
    ),
    SCHOOL(
        "school",
        R.drawable.ic_school,
        R.string.keywords_school,
        IconCategory.PRODUCTIVITY_WORK,
    ),
    BOOK(
        "book",
        R.drawable.ic_book,
        R.string.keywords_book,
        IconCategory.PRODUCTIVITY_WORK,
    ),
    CREATE(
        "create",
        R.drawable.ic_draw,
        R.string.keywords_create,
        IconCategory.PRODUCTIVITY_WORK,
    ),
    HOME(
        "home",
        R.drawable.ic_home,
        R.string.keywords_home,
        IconCategory.HOME_DAILY_LIFE,
    ),
    CALENDAR(
        "calendar",
        R.drawable.ic_calendar,
        R.string.keywords_calendar,
        IconCategory.HOME_DAILY_LIFE,
    ),
    BED(
        "bed",
        R.drawable.ic_bed,
        R.string.keywords_bed,
        IconCategory.HOME_DAILY_LIFE,
    ),
    CALL(
        "call",
        R.drawable.ic_call,
        R.string.keywords_call,
        IconCategory.COMMUNICATION_SOCIAL,
    ),
    EMAIL(
        "email",
        R.drawable.ic_mail,
        R.string.keywords_email,
        IconCategory.COMMUNICATION_SOCIAL,
    ),
    MUSIC(
        "music",
        R.drawable.ic_music_cast,
        R.string.keywords_music,
        IconCategory.ENTERTAINMENT_HOBBIES,
    ),
    MUSIC_NOTE(
        "music_note",
        R.drawable.ic_music_note_2,
        R.string.keywords_music_note,
        IconCategory.ENTERTAINMENT_HOBBIES,
    ),
    GAME(
        "game",
        R.drawable.ic_videogame_asset,
        R.string.keywords_game,
        IconCategory.ENTERTAINMENT_HOBBIES,
    ),
    MOVIE(
        "movie",
        R.drawable.ic_movie,
        R.string.keywords_movie,
        IconCategory.ENTERTAINMENT_HOBBIES,
    ),
    STAR(
        "star",
        R.drawable.ic_star,
        R.string.keywords_star,
        IconCategory.PERSONAL_DEVELOPMENT,
    ),
    TROPHY(
        "trophy",
        R.drawable.ic_trophy,
        R.string.keywords_trophy,
        IconCategory.PERSONAL_DEVELOPMENT,
    ),
    TARGET(
        "target",
        R.drawable.ic_flag,
        R.string.keywords_target,
        IconCategory.PERSONAL_DEVELOPMENT,
    ),
    LIST(
        "list",
        R.drawable.ic_list,
        R.string.keywords_list,
        IconCategory.MISCELLANEOUS,
    ),
    REMIND(
        "remind",
        R.drawable.ic_notifications,
        R.string.keywords_remind,
        IconCategory.MISCELLANEOUS,
    ),
    CATEGORY(
        "category",
        R.drawable.ic_category,
        R.string.keywords_category,
        IconCategory.MISCELLANEOUS,
    ),
    INBOX(
        "inbox",
        R.drawable.ic_inbox,
        R.string.keywords_inbox,
        IconCategory.MISCELLANEOUS,
    ),
    PETS(
        "pets",
        R.drawable.ic_pets,
        R.string.keywords_pets,
        IconCategory.MISCELLANEOUS,
    ),
    ROUTINE(
        "routine",
        R.drawable.ic_routine,
        R.string.keywords_routine,
        IconCategory.MISCELLANEOUS,
    ),
    SHIELD(
        "shield",
        R.drawable.ic_shield,
        R.string.keywords_shield,
        IconCategory.MISCELLANEOUS,
    ),
    PSYCHIATRY(
        "psychiatry",
        R.drawable.ic_psychiatry,
        R.string.keywords_psychiatry,
        IconCategory.MISCELLANEOUS,
    ),
    SMOKE_FREE(
        "smoke_free",
        R.drawable.ic_smoke_free,
        R.string.keywords_smoke_free,
        IconCategory.MISCELLANEOUS,
    ),
    ;

    companion object {
        fun fromName(name: String?): TempoIcon? {
            if (name == null) return null
            return entries.find { it.iconName == name }
        }

        fun getAllIcons(): List<TempoIcon> = entries

        /**
         * Samples up to [slotCount] icons out of [icons], drawing one at a time from each
         * category in round-robin order (both the icons within a category and the order
         * categories are drawn from are shuffled), so the result favors distinct categories
         * before repeating one - and which categories show up varies between calls too.
         */
        fun sampleAcrossCategories(
            icons: List<TempoIcon>,
            slotCount: Int,
            random: Random = Random.Default,
        ): List<TempoIcon> {
            if (slotCount <= 0 || icons.isEmpty()) return emptyList()
            val queues =
                icons
                    .groupBy { it.category }
                    .values
                    .map { it.shuffled(random).toMutableList() }
                    .shuffled(random)
            val result = mutableListOf<TempoIcon>()
            while (result.size < slotCount && queues.any { it.isNotEmpty() }) {
                for (queue in queues) {
                    if (result.size >= slotCount) break
                    if (queue.isNotEmpty()) {
                        result += queue.removeAt(0)
                    }
                }
            }
            return result
        }

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
