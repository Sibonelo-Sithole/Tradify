package com.example.tradify

data class Badge(val name: String, val unlocked: Boolean)

object AchievementHelper {

    // 10 points per trade and 5 points per journal entry
    fun calculatePoints(tradeCount: Int, journalCount: Int): Int {
        return tradeCount * 10 + journalCount * 5
    }

    fun getBadges(tradeCount: Int, journalCount: Int): List<Badge> {
        return listOf(
            Badge("First Trade", tradeCount >= 1),
            Badge("10 Trades", tradeCount >= 10),
            Badge("25 Trades", tradeCount >= 25),
            Badge("First Journal Entry", journalCount >= 1),
            Badge("5 Journal Entries", journalCount >= 5)
        )
    }
}