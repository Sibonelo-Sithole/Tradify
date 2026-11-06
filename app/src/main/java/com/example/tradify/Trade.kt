package com.example.tradify

import com.google.firebase.firestore.Exclude

// Firestore needs default values so it can turn a document back into a Trade
data class Trade(
    var id: String = "",
    var asset: String = "",
    var type: String = "Long",
    var entryPrice: Double = 0.0,
    var exitPrice: Double = 0.0,
    var quantity: Double = 0.0,
    var date: String = "",
    var strategy: String = "",
    var profitLoss: Double = 0.0,
    var emotion: String = "",
    var notes: String = "",
    // Only used on the phone (not saved to Firestore): false = waiting to sync
    @get:Exclude var synced: Boolean = true
)