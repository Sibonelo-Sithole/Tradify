package com.example.tradify

object ProfitCalculator {

    // Long trade: you make money when the price goes up
    // Short trade: you make money when the price goes down
    fun calculateProfit(type: String, entry: Double, exit: Double, quantity: Double): Double {
        return if (type == "Short") {
            (entry - exit) * quantity
        } else {
            (exit - entry) * quantity
        }
    }
}