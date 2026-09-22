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
import com.google.firebase.firestore.FirebaseFirestore

class TradeHistoryActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var allTrades: List<Trade> = listOf()
    private lateinit var adapter: TradeAdapter
    private lateinit var spFilter: Spinner
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_history)

        spFilter = findViewById(R.id.spFilter)
        tvEmpty = findViewById(R.id.tvEmpty)
        val recycler = findViewById<RecyclerView>(R.id.recyclerTrades)

        adapter = TradeAdapter(listOf()) { trade -> showTradeDialog(trade) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val filters = listOf("All", "Profitable", "Losing", "Long", "Short")
        spFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        loadTrades()
    }

    private fun loadTrades() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("trades")
            .get()
            .addOnSuccessListener { result ->
                allTrades = result.documents.mapNotNull { doc ->
                    val trade = doc.toObject(Trade::class.java)
                    trade?.id = doc.id
                    trade
                }.sortedByDescending { it.date }
                applyFilter()
            }
    }

    private fun applyFilter() {
        val filtered = when (spFilter.selectedItem.toString()) {
            "Profitable" -> allTrades.filter { it.profitLoss > 0 }
            "Losing" -> allTrades.filter { it.profitLoss < 0 }
            "Long" -> allTrades.filter { it.type == "Long" }
            "Short" -> allTrades.filter { it.type == "Short" }
            else -> allTrades
        }
        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    private fun showTradeDialog(trade: Trade) {
        val message = "Entry: " + trade.entryPrice +
                "\nExit: " + trade.exitPrice +
                "\nQuantity: " + trade.quantity +
                "\nStrategy: " + trade.strategy +
                "\nEmotion: " + trade.emotion +
                "\nNotes: " + trade.notes

        AlertDialog.Builder(this)
            .setTitle(trade.asset + " (" + trade.type + ")")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .setNegativeButton("Delete") { _, _ -> deleteTrade(trade) }
            .show()
    }

    private fun deleteTrade(trade: Trade) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("trades").document(trade.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Trade deleted", Toast.LENGTH_SHORT).show()
                loadTrades()
            }
    }
}