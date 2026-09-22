package com.example.tradify

object StatsCalculator {

    fun totalTrades(trades: List<Trade>): Int {
        return trades.size
    }

    fun winningTrades(trades: List<Trade>): Int {
        return trades.count { it.profitLoss > 0 }
    }

    fun losingTrades(trades: List<Trade>): Int {
        return trades.count { it.profitLoss < 0 }
    }

    // Win Rate = Winning Trades / Total Trades * 100
    fun winRate(trades: List<Trade>): Double {
        if (trades.isEmpty()) {
            return 0.0
        }
        return winningTrades(trades).toDouble() / trades.size * 100
    }

    fun totalProfit(trades: List<Trade>): Double {
        return trades.sumOf { it.profitLoss }
    }

    fun averageProfit(trades: List<Trade>): Double {
        val wins = trades.filter { it.profitLoss > 0 }
        if (wins.isEmpty()) {
            return 0.0
        }
        return wins.sumOf { it.profitLoss } / wins.size
    }

    fun averageLoss(trades: List<Trade>): Double {
        val losses = trades.filter { it.profitLoss < 0 }
        if (losses.isEmpty()) {
            return 0.0
        }
        return losses.sumOf { it.profitLoss } / losses.size
    }
}