package com.example.tradify

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        val tvTotalPoints = findViewById<TextView>(R.id.tvTotalPoints)
        val listBadges = findViewById<ListView>(R.id.listBadges)

        val uid = auth.currentUser?.uid ?: return
        val userDoc = db.collection("users").document(uid)

        userDoc.collection("trades").get().addOnSuccessListener { tradeResult ->
            userDoc.collection("journal").get().addOnSuccessListener { journalResult ->
                val tradeCount = tradeResult.size()
                val journalCount = journalResult.size()

                val points = AchievementHelper.calculatePoints(tradeCount, journalCount)
                tvTotalPoints.text = "$points points"

                val badges = AchievementHelper.getBadges(tradeCount, journalCount)
                val lines = badges.map {
                    if (it.unlocked) "[Unlocked]  " + it.name else "[Locked]  " + it.name
                }
                listBadges.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)
            }
        }
    }
}