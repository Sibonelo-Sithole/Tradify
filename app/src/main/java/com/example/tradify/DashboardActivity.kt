package com.example.tradify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvWelcome: TextView
    private lateinit var tvTotalTrades: TextView
    private lateinit var tvWinRate: TextView
    private lateinit var tvWinsLosses: TextView
    private lateinit var tvTotalPL: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvPoints: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvTotalTrades = findViewById(R.id.tvTotalTrades)
        tvWinRate = findViewById(R.id.tvWinRate)
        tvWinsLosses = findViewById(R.id.tvWinsLosses)
        tvTotalPL = findViewById(R.id.tvTotalPL)
        tvAvg = findViewById(R.id.tvAvg)
        tvPoints = findViewById(R.id.tvPoints)

        tvWelcome.text = "Welcome, " + auth.currentUser?.email

        findViewById<Button>(R.id.btnAddTrade).setOnClickListener {
            startActivity(Intent(this, AddTradeActivity::class.java))
        }
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, TradeHistoryActivity::class.java))
        }
        findViewById<Button>(R.id.btnJournal).setOnClickListener {
            startActivity(Intent(this, JournalActivity::class.java))
        }
        findViewById<Button>(R.id.btnAchievements).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Reload the numbers every time we come back to this screen
    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val uid = auth.currentUser?.uid ?: return
        val userDoc = db.collection("users").document(uid)

        userDoc.collection("trades").get().addOnSuccessListener { tradeResult ->
            val trades = tradeResult.documents.mapNotNull { it.toObject(Trade::class.java) }

            tvTotalTrades.text = "Total trades: " + StatsCalculator.totalTrades(trades)
            tvWinRate.text = "Win rate: " + String.format("%.1f", StatsCalculator.winRate(trades)) + "%"
            tvWinsLosses.text = "Wins: " + StatsCalculator.winningTrades(trades) +
                    "   Losses: " + StatsCalculator.losingTrades(trades)
            tvTotalPL.text = "Total profit/loss: " + String.format("%.2f", StatsCalculator.totalProfit(trades))
            tvAvg.text = "Avg profit: " + String.format("%.2f", StatsCalculator.averageProfit(trades)) +
                    "   Avg loss: " + String.format("%.2f", StatsCalculator.averageLoss(trades))

            // Need the journal count too for the points
            userDoc.collection("journal").get().addOnSuccessListener { journalResult ->
                val points = AchievementHelper.calculatePoints(trades.size, journalResult.size())
                tvPoints.text = "Achievement points: $points"
            }
        }
    }
}