package com.example.tradify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvWelcome: TextView
    private lateinit var tvOffline: TextView
    private lateinit var tvTotalTrades: TextView
    private lateinit var tvWinRate: TextView
    private lateinit var tvWinsLosses: TextView
    private lateinit var tvTotalPL: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvPoints: TextView

    // Needed to ask for the notification permission on Android 13+
    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvOffline = findViewById(R.id.tvOffline)
        tvTotalTrades = findViewById(R.id.tvTotalTrades)
        tvWinRate = findViewById(R.id.tvWinRate)
        tvWinsLosses = findViewById(R.id.tvWinsLosses)
        tvTotalPL = findViewById(R.id.tvTotalPL)
        tvAvg = findViewById(R.id.tvAvg)
        tvPoints = findViewById(R.id.tvPoints)

        tvWelcome.text = getString(R.string.welcome, auth.currentUser?.email ?: "")

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
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Push notifications: permission, token and topic
        NotificationHelper.setupPush(this, notifPermission)
    }

    override fun onResume() {
        super.onResume()
        SyncManager.onSyncFinished = { refresh() }
        refresh()
        SyncManager.syncNow(this)
    }

    override fun onPause() {
        super.onPause()
        SyncManager.onSyncFinished = null
    }

    private fun refresh() {
        val uid = auth.currentUser?.uid ?: return

        tvOffline.visibility = if (SyncManager.isOnline(this)) View.GONE else View.VISIBLE

        // Stats come from the phone's own database, so they work offline
        val trades = LocalDb(this).getTrades(uid)
        showStats(trades, Prefs.journalCount(this, uid))

        // Journal count comes from Firestore when online
        if (SyncManager.isOnline(this)) {
            db.collection("users").document(uid).collection("journal").get()
                .addOnSuccessListener { journalResult ->
                    Prefs.setJournalCount(this, uid, journalResult.size())
                    showStats(trades, journalResult.size())
                }
        }
    }

    private fun showStats(trades: List<Trade>, journalCount: Int) {
        tvTotalTrades.text = getString(R.string.total_trades, StatsCalculator.totalTrades(trades))
        tvWinRate.text = getString(R.string.win_rate, String.format("%.1f", StatsCalculator.winRate(trades)))
        tvWinsLosses.text = getString(
            R.string.wins_losses,
            StatsCalculator.winningTrades(trades),
            StatsCalculator.losingTrades(trades)
        )
        tvTotalPL.text = getString(R.string.total_pl, String.format("%.2f", StatsCalculator.totalProfit(trades)))
        tvAvg.text = getString(
            R.string.avg_pl,
            String.format("%.2f", StatsCalculator.averageProfit(trades)),
            String.format("%.2f", StatsCalculator.averageLoss(trades))
        )
        tvPoints.text = getString(R.string.points, AchievementHelper.calculatePoints(trades.size, journalCount))
    }
}