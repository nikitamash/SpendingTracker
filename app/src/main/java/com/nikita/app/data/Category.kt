package com.nikita.app.data

import com.nikita.app.R

/**
 * Enum representing expense categories with their display names and icon identifiers
 */
enum class Category(val displayName: String, val iconRes: String, val iconDrawable: Int? = null) {
    FOOD("Food", "", R.drawable.ic_burger),
    DRINKS("Drinks", "", R.drawable.ic_soda),
    RESTAURANTS("Restaurants", "", R.drawable.ic_lantern),
    ENTERTAINMENT("Leisure", "🎡"),
    BANK_CARD("Bank Card", "", R.drawable.ic_bank_card),
    SHOPPING("Shopping", "🛍️"),
    TRANSPORT("Transport", "🚗"),
    UTILITIES("Utilities", "💡")
}
