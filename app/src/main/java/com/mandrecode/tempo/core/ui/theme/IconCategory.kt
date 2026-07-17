package com.mandrecode.tempo.core.ui.theme

import androidx.annotation.StringRes
import com.mandrecode.tempo.R

/**
 * Groups [TempoIcon] entries for the icon picker's randomized row and category modal.
 */
enum class IconCategory(
    @StringRes val labelRes: Int,
) {
    FITNESS_SPORTS(R.string.icon_category_fitness_sports),
    HEALTH_WELLNESS(R.string.icon_category_health_wellness),
    FOOD_NUTRITION(R.string.icon_category_food_nutrition),
    PRODUCTIVITY_WORK(R.string.icon_category_productivity_work),
    HOME_DAILY_LIFE(R.string.icon_category_home_daily_life),
    COMMUNICATION_SOCIAL(R.string.icon_category_communication_social),
    ENTERTAINMENT_HOBBIES(R.string.icon_category_entertainment_hobbies),
    PERSONAL_DEVELOPMENT(R.string.icon_category_personal_development),
    FINANCE_CHORES(R.string.icon_category_finance_chores),
    MISCELLANEOUS(R.string.icon_category_miscellaneous),
}
