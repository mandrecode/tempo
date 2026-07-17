package com.mandrecode.tempo.core.ui.theme

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mandrecode.tempo.R
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
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
    SELF_IMPROVEMENT(
        "self_improvement",
        R.drawable.ic_self_improvement,
        R.string.keywords_self_improvement,
        IconCategory.FITNESS_SPORTS,
    ),
    POOL(
        "pool",
        R.drawable.ic_pool,
        R.string.keywords_pool,
        IconCategory.FITNESS_SPORTS,
    ),
    DIRECTIONS_BIKE(
        "directions_bike",
        R.drawable.ic_directions_bike,
        R.string.keywords_directions_bike,
        IconCategory.FITNESS_SPORTS,
    ),
    HIKING(
        "hiking",
        R.drawable.ic_hiking,
        R.string.keywords_hiking,
        IconCategory.FITNESS_SPORTS,
    ),
    SPORTS_GYMNASTICS(
        "sports_gymnastics",
        R.drawable.ic_sports_gymnastics,
        R.string.keywords_sports_gymnastics,
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
    BEDTIME(
        "bedtime",
        R.drawable.ic_bedtime,
        R.string.keywords_bedtime,
        IconCategory.HEALTH_WELLNESS,
    ),
    MEDICATION(
        "medication",
        R.drawable.ic_medication,
        R.string.keywords_medication,
        IconCategory.HEALTH_WELLNESS,
    ),
    MEDICAL_SERVICES(
        "medical_services",
        R.drawable.ic_medical_services,
        R.string.keywords_medical_services,
        IconCategory.HEALTH_WELLNESS,
    ),
    MONITOR_WEIGHT(
        "monitor_weight",
        R.drawable.ic_monitor_weight,
        R.string.keywords_monitor_weight,
        IconCategory.HEALTH_WELLNESS,
    ),
    PSYCHIATRY(
        "psychiatry",
        R.drawable.ic_psychiatry,
        R.string.keywords_psychiatry,
        IconCategory.HEALTH_WELLNESS,
    ),
    SMOKE_FREE(
        "smoke_free",
        R.drawable.ic_smoke_free,
        R.string.keywords_smoke_free,
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
    LUNCH_DINING(
        "lunch_dining",
        R.drawable.ic_lunch_dining,
        R.string.keywords_lunch_dining,
        IconCategory.FOOD_NUTRITION,
    ),
    NUTRITION(
        "nutrition",
        R.drawable.ic_nutrition,
        R.string.keywords_nutrition,
        IconCategory.FOOD_NUTRITION,
    ),
    NO_FOOD(
        "no_food",
        R.drawable.ic_no_food,
        R.string.keywords_no_food,
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
    CHECKLIST(
        "checklist",
        R.drawable.ic_checklist,
        R.string.keywords_checklist,
        IconCategory.PRODUCTIVITY_WORK,
    ),
    CODE(
        "code",
        R.drawable.ic_code,
        R.string.keywords_code,
        IconCategory.PRODUCTIVITY_WORK,
    ),
    EVENT(
        "event",
        R.drawable.ic_event,
        R.string.keywords_event,
        IconCategory.PRODUCTIVITY_WORK,
    ),
    TIMER(
        "timer",
        R.drawable.ic_timer,
        R.string.keywords_timer,
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
    DIRECTIONS_CAR(
        "directions_car",
        R.drawable.ic_directions_car,
        R.string.keywords_directions_car,
        IconCategory.HOME_DAILY_LIFE,
    ),
    POTTED_PLANT(
        "potted_plant",
        R.drawable.ic_potted_plant,
        R.string.keywords_potted_plant,
        IconCategory.HOME_DAILY_LIFE,
    ),
    PETS(
        "pets",
        R.drawable.ic_pets,
        R.string.keywords_pets,
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
    CHAT(
        "chat",
        R.drawable.ic_chat,
        R.string.keywords_chat,
        IconCategory.COMMUNICATION_SOCIAL,
    ),
    VIDEOCAM(
        "videocam",
        R.drawable.ic_videocam,
        R.string.keywords_videocam,
        IconCategory.COMMUNICATION_SOCIAL,
    ),
    CELEBRATION(
        "celebration",
        R.drawable.ic_celebration,
        R.string.keywords_celebration,
        IconCategory.COMMUNICATION_SOCIAL,
    ),
    DIVERSITY_3(
        "diversity_3",
        R.drawable.ic_diversity_3,
        R.string.keywords_diversity_3,
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
    PHOTO_CAMERA(
        "photo_camera",
        R.drawable.ic_photo_camera,
        R.string.keywords_photo_camera,
        IconCategory.ENTERTAINMENT_HOBBIES,
    ),
    PALETTE_OUTLINED(
        "palette_outlined",
        R.drawable.ic_palette_outlined,
        R.string.keywords_palette_outlined,
        IconCategory.ENTERTAINMENT_HOBBIES,
    ),
    FLIGHT(
        "flight",
        R.drawable.ic_flight,
        R.string.keywords_flight,
        IconCategory.ENTERTAINMENT_HOBBIES,
    ),
    EDIT_NOTE(
        "edit_note",
        R.drawable.ic_edit_note,
        R.string.keywords_edit_note,
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
    TRANSLATE(
        "translate",
        R.drawable.ic_translate,
        R.string.keywords_translate,
        IconCategory.PERSONAL_DEVELOPMENT,
    ),
    PSYCHOLOGY(
        "psychology",
        R.drawable.ic_psychology,
        R.string.keywords_psychology,
        IconCategory.PERSONAL_DEVELOPMENT,
    ),
    MILITARY_TECH(
        "military_tech",
        R.drawable.ic_military_tech,
        R.string.keywords_military_tech,
        IconCategory.PERSONAL_DEVELOPMENT,
    ),
    TRENDING_UP(
        "trending_up",
        R.drawable.ic_trending_up,
        R.string.keywords_trending_up,
        IconCategory.PERSONAL_DEVELOPMENT,
    ),
    ACCOUNT_BALANCE_WALLET(
        "account_balance_wallet",
        R.drawable.ic_account_balance_wallet,
        R.string.keywords_account_balance_wallet,
        IconCategory.FINANCE_CHORES,
    ),
    SAVINGS(
        "savings",
        R.drawable.ic_savings,
        R.string.keywords_savings,
        IconCategory.FINANCE_CHORES,
    ),
    SHOPPING_CART(
        "shopping_cart",
        R.drawable.ic_shopping_cart,
        R.string.keywords_shopping_cart,
        IconCategory.FINANCE_CHORES,
    ),
    CLEANING_SERVICES(
        "cleaning_services",
        R.drawable.ic_cleaning_services,
        R.string.keywords_cleaning_services,
        IconCategory.FINANCE_CHORES,
    ),
    LOCAL_LAUNDRY_SERVICE(
        "local_laundry_service",
        R.drawable.ic_local_laundry_service,
        R.string.keywords_local_laundry_service,
        IconCategory.FINANCE_CHORES,
    ),
    RECYCLING(
        "recycling",
        R.drawable.ic_recycling,
        R.string.keywords_recycling,
        IconCategory.FINANCE_CHORES,
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

        /**
         * Picks the icon whose keywords best match [text], scoring by how many distinct
         * keywords hit rather than which icon happens to be declared first in the enum
         * (e.g. "Drink water" should win on WATER's two hits - "water" + "drink" - over
         * COFFEE's single "drink" hit). Ties fall back to enum declaration order.
         */
        fun suggestIcon(
            text: String,
            context: Context,
        ): TempoIcon? {
            val normalizedText = text.lowercase(Locale.ROOT)
            var bestIcon: TempoIcon? = null
            var bestScore = 0
            for (icon in entries) {
                val score = matchScore(icon, normalizedText, context)
                if (score > bestScore) {
                    bestScore = score
                    bestIcon = icon
                }
            }
            return bestIcon
        }

        private fun matchScore(
            icon: TempoIcon,
            normalizedText: String,
            context: Context,
        ): Int {
            val keywords = normalizedKeywords(icon, context)
            val matched = keywords.filter { keyword -> normalizedText.matchesKeyword(keyword) }
            // A keyword list often lists both a root and its own inflected form
            // (e.g. "remind" + "reminder", "sport" + "sports"). Prefix leniency makes both
            // match the same word, so collapse a matched keyword into the longer matched
            // keyword it's a prefix of, rather than counting it as a second distinct hit.
            return matched.count { keyword -> matched.none { other -> other != keyword && other.startsWith(keyword) } }
        }

        // suggestIcon() runs on every keystroke (LaunchedEffect on the title/name field), so
        // cache the split+normalized keyword list instead of re-splitting and re-lowercasing
        // it on every call. Keyed by the raw resource string itself (not just keywordsRes) so
        // a locale change - which yields a different raw string - naturally invalidates it
        // without having to track the current locale separately.
        private fun normalizedKeywords(
            icon: TempoIcon,
            context: Context,
        ): List<String> {
            val rawKeywords = context.getString(icon.keywordsRes)
            return keywordListCache.computeIfAbsent(rawKeywords) {
                rawKeywords
                    .split(",")
                    .map { it.trim().lowercase(Locale.ROOT) }
                    .filter { it.isNotEmpty() }
            }
        }

        private fun String.matchesKeyword(keyword: String): Boolean {
            val pattern = keywordPatternCache.computeIfAbsent(keyword, ::buildKeywordPattern)
            return pattern.containsMatchIn(this)
        }

        // Left side is always a hard word boundary so a keyword can never match mid-word
        // (e.g. "run" must not match inside "brunch", Spanish "ver" must not match inside
        // "verano"). Longer keywords (>4 chars) may also match as a prefix so regular
        // suffixes are still caught without needing every inflected form listed explicitly
        // (e.g. "hydrate" -> "hydrated", "clean" -> "cleaning", Spanish "empleo" -> "empleos").
        // Short keywords require a hard boundary on the right too, but still allow a plain
        // "s"/"es" plural first (e.g. "meal" -> "meals", "goal" -> "goals") so common plurals
        // of short words keep matching without opening the door to arbitrary mid-word hits.
        private fun buildKeywordPattern(keyword: String): Regex {
            val escaped = Regex.escape(keyword)
            val pattern =
                if (keyword.length > WHOLE_WORD_BOUNDARY_MAX_LENGTH) {
                    "(?<![\\p{L}\\p{N}])$escaped"
                } else {
                    "(?<![\\p{L}\\p{N}])$escaped(?:es|s)?(?![\\p{L}\\p{N}])"
                }
            return Regex(pattern)
        }

        private const val WHOLE_WORD_BOUNDARY_MAX_LENGTH = 4
        private val keywordPatternCache = ConcurrentHashMap<String, Regex>()
        private val keywordListCache = ConcurrentHashMap<String, List<String>>()
    }
}
