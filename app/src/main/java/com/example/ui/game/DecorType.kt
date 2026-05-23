package com.example.ui.game

import androidx.compose.ui.graphics.Color

enum class DecorType(
    val key: String,
    val displayName: String,
    val category: String, // "Bed", "Cat Tree", "Toy", "Decoration"
    val emoji: String,
    val cost: Int,
    val happinessBonus: Int,
    val description: String
) {
    CARDBOARD_BOX(
        "CARDBOARD_BOX",
        "Cardboard Box Bed",
        "Bed",
        "📦",
        50,
        20,
        "Cats love empty boxes! Boosts cat happiness +20%."
    ),
    SISAL_CAT_TREE(
        "SISAL_CAT_TREE",
        "Cozy Cat Tree",
        "Cat Tree",
        "🌳",
        120,
        50,
        "Sisal posts for scratching and climbing! Happiness +50%."
    ),
    FEATHER_WAND(
        "FEATHER_WAND",
        "Feather Wand Stand",
        "Toy",
        "🪶",
        80,
        30,
        "Provides dynamic stimulation! Happiness +30%."
    ),
    CAT_GRASS(
        "CAT_GRASS",
        "Organic Cat Grass",
        "Decoration",
        "🌿",
        60,
        25,
        "Tasty wheatgrass to chew! Happiness +25%."
    ),
    NEON_PAW(
        "NEON_PAW",
        "Neon Paw Light",
        "Decoration",
        "🐾",
        160,
        65,
        "Glowing retro ambient light! Happiness +65%."
    ),
    ROYAL_THRONE(
        "ROYAL_THRONE",
        "Royal Feline Throne",
        "Bed",
        "👑",
        250,
        100,
        "A velvet canopy throne for royals! Happiness +100%."
    );

    companion object {
        fun fromKey(key: String): DecorType? = values().find { it.key == key }
    }
}
