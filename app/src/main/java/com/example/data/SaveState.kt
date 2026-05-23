package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_state")
data class SaveState(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 150, // Starting coins
    val catLove: Int = 0, // Level of customer love/pettings
    val unlockedTables: Int = 2, // Range: 1-4 tables
    val unlockedCatsCsv: String = "Ginger,Boba", // Starter cats
    val currentDay: Int = 1, // Current cafe Shift / Day
    val highScore: Int = 0, // High score of coins collected in one day
    val upgradeCoffeeOvenLevel: Int = 1, // Coffee maker level (increases profit/prep speed)
    val upgradePastryOvenLevel: Int = 1, // Pastry bakery level
    val upgradeCatToysLevel: Int = 1, // Cat Toy station level
    val totalPlayTimeSec: Long = 0L,
    val purchasedDecorsCsv: String = "", // Bought decor keys, e.g., "CARDBOARD_BOX,SISAL_CAT_TREE"
    val placedDecorsCsv: String = "" // Table placement status, e.g., "1:CARDBOARD_BOX,2:SISAL_CAT_TREE"
)
