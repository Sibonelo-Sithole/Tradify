package com.example.tradify

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// CoinGecko example: simple/price?ids=bitcoin&vs_currencies=usd
// Response looks like {"bitcoin":{"usd":67000}}
interface PriceApi {

    @GET("simple/price")
    fun getPrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") currency: String
    ): Call<Map<String, Map<String, Double>>>
}