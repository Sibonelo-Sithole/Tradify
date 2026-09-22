package com.example.tradify

object TradeValidator {

    // Returns an error message, or null if everything is fine
    fun validate(asset: String, entry: String, exit: String, quantity: String): String? {
        if (asset.isBlank()) {
            return "Please enter an asset name"
        }
        val entryPrice = entry.toDoubleOrNull() ?: return "Entry price must be a number"
        val exitPrice = exit.toDoubleOrNull() ?: return "Exit price must be a number"
        val qty = quantity.toDoubleOrNull() ?: return "Quantity must be a number"

        if (entryPrice <= 0) {
            return "Entry price must be more than 0"
        }
        if (exitPrice <= 0) {
            return "Exit price must be more than 0"
        }
        if (qty <= 0) {
            return "Quantity must be more than 0"
        }
        return null
    }
}