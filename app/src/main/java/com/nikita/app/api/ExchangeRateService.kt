package com.nikita.app.api

import retrofit2.http.GET

interface ExchangeRateApi {
    @GET("v6/latest/USD")
    suspend fun getRates(): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val result: String,
    val rates: Map<String, Double>
)
