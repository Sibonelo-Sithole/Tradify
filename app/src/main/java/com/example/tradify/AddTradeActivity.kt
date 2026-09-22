package com.example.tradify

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTradeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_trade)

        val etAsset = findViewById<EditText>(R.id.etAsset)
        val spType = findViewById<Spinner>(R.id.spType)
        val etEntry = findViewById<EditText>(R.id.etEntry)
        val etExit = findViewById<EditText>(R.id.etExit)
        val etQuantity = findViewById<EditText>(R.id.etQuantity)
        val etDate = findViewById<EditText>(R.id.etDate)
        val spStrategy = findViewById<Spinner>(R.id.spStrategy)
        val spEmotion = findViewById<Spinner>(R.id.spEmotion)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val tvLivePrice = findViewById<TextView>(R.id.tvLivePrice)
        val btnLivePrice = findViewById<Button>(R.id.btnLivePrice)
        val btnSave = findViewById<Button>(R.id.btnSaveTrade)

        val types = listOf("Long", "Short")
        val strategies = listOf("Breakout", "Trend Following", "Scalping", "Swing", "Other")
        val emotions = listOf("Confident", "Nervous", "Excited", "Frustrated", "Neutral")
        spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        spStrategy.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strategies)
        spEmotion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, emotions)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(today)

        // REST API call using Retrofit
        btnLivePrice.setOnClickListener {
            val coin = etAsset.text.toString().trim().lowercase()
            if (coin.isEmpty()) {
                Toast.makeText(this, "Type a coin name first (e.g. bitcoin)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvLivePrice.text = "Loading..."
            RetrofitClient.priceApi.getPrice(coin, "usd").enqueue(object : Callback<Map<String, Map<String, Double>>> {
                override fun onResponse(
                    call: Call<Map<String, Map<String, Double>>>,
                    response: Response<Map<String, Map<String, Double>>>
                ) {
                    val price = response.body()?.get(coin)?.get("usd")
                    if (price != null) {
                        tvLivePrice.text = "Live price: $$price"
                        etEntry.setText(price.toString())
                    } else {
                        tvLivePrice.text = "Coin not found"
                    }
                }

                override fun onFailure(call: Call<Map<String, Map<String, Double>>>, t: Throwable) {
                    tvLivePrice.text = "Could not get price (check internet)"
                }
            })
        }

        btnSave.setOnClickListener {
            val asset = etAsset.text.toString().trim()
            val entryText = etEntry.text.toString()
            val exitText = etExit.text.toString()
            val quantityText = etQuantity.text.toString()

            val error = TradeValidator.validate(asset, entryText, exitText, quantityText)
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = spType.selectedItem.toString()
            val entry = entryText.toDouble()
            val exit = exitText.toDouble()
            val quantity = quantityText.toDouble()
            val profit = ProfitCalculator.calculateProfit(type, entry, exit, quantity)

            val trade = Trade(
                asset = asset,
                type = type,
                entryPrice = entry,
                exitPrice = exit,
                quantity = quantity,
                date = etDate.text.toString(),
                strategy = spStrategy.selectedItem.toString(),
                profitLoss = profit,
                emotion = spEmotion.selectedItem.toString(),
                notes = etNotes.text.toString()
            )

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(uid).collection("trades")
                .add(trade)
                .addOnSuccessListener {
                    Toast.makeText(this, "Trade saved! P/L: " + String.format("%.2f", profit), Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not save trade", Toast.LENGTH_SHORT).show()
                }
        }
    }
}