package com.example.tradify

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class TradeHistoryActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    private var allTrades: List<Trade> = listOf()
    private lateinit var adapter: TradeAdapter
    private lateinit var spFilter: Spinner
    private lateinit var tvEmpty: TextView
    private lateinit var tvOffline: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_history)

        spFilter = findViewById(R.id.spFilter)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvOffline = findViewById(R.id.tvOffline)
        val recycler = findViewById<RecyclerView>(R.id.recyclerTrades)

        adapter = TradeAdapter(listOf()) { trade -> showTradeDialog(trade) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        spFilter.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.filters_array)
        )
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        SyncManager.onSyncFinished = { reload() }
        reload()
        SyncManager.syncNow(this)
    }

    override fun onPause() {
        super.onPause()
        SyncManager.onSyncFinished = null
    }

    // Trades are read from the phone's database, so history works offline
    private fun reload() {
        val uid = auth.currentUser?.uid ?: return
        tvOffline.visibility = if (SyncManager.isOnline(this)) View.GONE else View.VISIBLE
        allTrades = LocalDb(this).getTrades(uid)
        applyFilter()
    }

    // Filter positions: 0 All, 1 Profitable, 2 Losing, 3 Long, 4 Short
    private fun applyFilter() {
        val filtered = when (spFilter.selectedItemPosition) {
            1 -> allTrades.filter { it.profitLoss > 0 }
            2 -> allTrades.filter { it.profitLoss < 0 }
            3 -> allTrades.filter { it.type == "Long" }
            4 -> allTrades.filter { it.type == "Short" }
            else -> allTrades
        }
        adapter.updateList(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showTradeDialog(trade: Trade) {
        val message = getString(
            R.string.trade_details,
            trade.entryPrice.toString(),
            trade.exitPrice.toString(),
            trade.quantity.toString(),
            trade.strategy,
            trade.emotion,
            trade.notes
        )

        AlertDialog.Builder(this)
            .setTitle(trade.asset + " (" + trade.type + ")")
            .setMessage(message)
            .setPositiveButton(getString(R.string.close), null)
            .setNegativeButton(getString(R.string.delete)) { _, _ -> deleteTrade(trade) }
            .show()
    }

    private fun deleteTrade(trade: Trade) {
        LocalDb(this).markDeleted(trade.id)
        SyncManager.syncNow(this)
        Toast.makeText(this, getString(R.string.trade_deleted), Toast.LENGTH_SHORT).show()
        reload()
    }
}