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

    private lateinit var tvTotalPoints: TextView
    private lateinit var listBadges: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        tvTotalPoints = findViewById(R.id.tvTotalPoints)
        listBadges = findViewById(R.id.listBadges)

        val uid = auth.currentUser?.uid ?: return
        val tradeCount = LocalDb(this).getTrades(uid).size

        // Show something straight away (works offline), then update from Firestore
        show(tradeCount, Prefs.journalCount(this, uid))

        if (SyncManager.isOnline(this)) {
            db.collection("users").document(uid).collection("journal").get()
                .addOnSuccessListener { journalResult ->
                    Prefs.setJournalCount(this, uid, journalResult.size())
                    show(tradeCount, journalResult.size())
                }
        }
    }

    private fun show(tradeCount: Int, journalCount: Int) {
        val points = AchievementHelper.calculatePoints(tradeCount, journalCount)
        tvTotalPoints.text = getString(R.string.total_points, points)

        val names = resources.getStringArray(R.array.badge_names)
        val badges = AchievementHelper.getBadges(tradeCount, journalCount)
        val lines = badges.mapIndexed { index, badge ->
            val status = if (badge.unlocked) getString(R.string.unlocked) else getString(R.string.locked)
            "[" + status + "]  " + names[index]
        }
        listBadges.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines)
    }
}