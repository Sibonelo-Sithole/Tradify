package com.example.tradify

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TradeAdapter(
    private var trades: List<Trade>,
    private val onLongClick: (Trade) -> Unit
) : RecyclerView.Adapter<TradeAdapter.TradeViewHolder>() {

    class TradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAsset: TextView = view.findViewById(R.id.tvItemAsset)
        val tvDetails: TextView = view.findViewById(R.id.tvItemDetails)
        val tvProfit: TextView = view.findViewById(R.id.tvItemProfit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trade, parent, false)
        return TradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        val trade = trades[position]
        holder.tvAsset.text = trade.asset + " (" + trade.type + ")"

        var details = trade.date + " | " + trade.strategy + " | " + trade.emotion
        // Shows when a trade is saved on the phone but not uploaded yet
        if (!trade.synced) {
            details += " | " + holder.itemView.context.getString(R.string.pending_sync)
        }
        holder.tvDetails.text = details
        holder.tvProfit.text = String.format("%.2f", trade.profitLoss)

        if (trade.profitLoss >= 0) {
            holder.tvProfit.setTextColor(Color.parseColor("#2DD4A7"))
        } else {
            holder.tvProfit.setTextColor(Color.parseColor("#F87171"))
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(trade)
            true
        }
    }

    override fun getItemCount(): Int {
        return trades.size
    }

    fun updateList(newList: List<Trade>) {
        trades = newList
        notifyDataSetChanged()
    }
}