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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddTradeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

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

        // What is SAVED stays in English, what the user SEES is translated
        val typesEn = listOf("Long", "Short")
        val strategiesEn = listOf("Breakout", "Trend Following", "Scalping", "Swing", "Other")
        val emotionsEn = listOf("Confident", "Nervous", "Excited", "Frustrated", "Neutral")

        val layout = android.R.layout.simple_spinner_dropdown_item
        spType.adapter = ArrayAdapter(this, layout, resources.getStringArray(R.array.types_array))
        spStrategy.adapter = ArrayAdapter(this, layout, resources.getStringArray(R.array.strategies_array))
        spEmotion.adapter = ArrayAdapter(this, layout, resources.getStringArray(R.array.emotions_array))

        // Default strategy comes from Settings
        spStrategy.setSelection(Prefs.defaultStrategy(this))

        etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        // REST API call using Retrofit (currency comes from Settings)
        btnLivePrice.setOnClickListener {
            val coin = etAsset.text.toString().trim().lowercase()
            if (coin.isEmpty()) {
                Toast.makeText(this, getString(R.string.type_coin_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currency = Prefs.currency(this)
            tvLivePrice.text = getString(R.string.loading)
            RetrofitClient.priceApi.getPrice(coin, currency)
                .enqueue(object : Callback<Map<String, Map<String, Double>>> {
                    override fun onResponse(
                        call: Call<Map<String, Map<String, Double>>>,
                        response: Response<Map<String, Map<String, Double>>>
                    ) {
                        val price = response.body()?.get(coin)?.get(currency)
                        if (price != null) {
                            tvLivePrice.text = getString(R.string.live_price, price.toString(), currency.uppercase())
                            etEntry.setText(price.toString())
                        } else {
                            tvLivePrice.text = getString(R.string.coin_not_found)
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Map<String, Double>>>, t: Throwable) {
                        tvLivePrice.text = getString(R.string.price_error)
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

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val type = typesEn[spType.selectedItemPosition]
            val entry = entryText.toDouble()
            val exit = exitText.toDouble()
            val quantity = quantityText.toDouble()
            val profit = ProfitCalculator.calculateProfit(type, entry, exit, quantity)

            val trade = Trade(
                id = UUID.randomUUID().toString(),
                asset = asset,
                type = type,
                entryPrice = entry,
                exitPrice = exit,
                quantity = quantity,
                date = etDate.text.toString(),
                strategy = strategiesEn[spStrategy.selectedItemPosition],
                profitLoss = profit,
                emotion = emotionsEn[spEmotion.selectedItemPosition],
                notes = etNotes.text.toString(),
                synced = false
            )

            // 1. Always save on the phone first (works offline)
            LocalDb(this).insertPending(uid, trade)

            // 2. If online, upload straight away. If not, it uploads when internet returns.
            if (SyncManager.isOnline(this)) {
                SyncManager.syncNow(this)
                Toast.makeText(this, getString(R.string.trade_saved, String.format("%.2f", profit)), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.trade_saved_offline), Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }
}