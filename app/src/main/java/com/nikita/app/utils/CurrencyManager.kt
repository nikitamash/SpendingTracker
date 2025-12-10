package com.nikita.app.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyManager {
    private const val PREF_NAME = "currency_prefs"
    private const val KEY_CURRENCY_CODE = "currency_code"
    
    enum class SupportedCurrency(val code: String, val displayName: String, val locale: Locale, val symbol: String) {
        EURO("EUR", "Euro (€)", Locale.GERMANY, "€"),
        US_DOLLAR("USD", "US Dollar ($)", Locale.US, "$"),
        LEK("ALL", "Lek (Lek)", Locale("sq", "AL"), "Lek"),
        RUBLE("RUB", "Ruble (₽)", Locale("ru", "RU"), "₽"),
        YUAN("CNY", "Yuan (¥)", Locale.CHINA, "¥"),
        YEN("JPY", "Yen (¥)", Locale.JAPAN, "¥")
    }

    fun saveCurrency(context: Context, currency: SupportedCurrency) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY_CODE, currency.code).apply()
    }

    fun getCurrency(context: Context): SupportedCurrency {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_CURRENCY_CODE, SupportedCurrency.LEK.code) ?: SupportedCurrency.LEK.code
        return SupportedCurrency.values().find { it.code == code } ?: SupportedCurrency.LEK
    }

    private var exchangeRates: Map<String, Double> = emptyMap()
    private val api: com.nikita.app.api.ExchangeRateApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.nikita.app.api.ExchangeRateApi::class.java)
    }

    suspend fun fetchRates() {
        try {
            val response = api.getRates()
            if (response.result == "success") {
                exchangeRates = response.rates
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun convertToLek(amount: Double, fromCurrency: SupportedCurrency): Double {
        if (exchangeRates.isEmpty()) return amount // Fallback if no rates
        if (fromCurrency == SupportedCurrency.LEK) return amount

        // Convert from -> USD -> LEK
        val fromRate = exchangeRates[fromCurrency.code] ?: return amount
        val lekRate = exchangeRates[SupportedCurrency.LEK.code] ?: return amount
        
        val amountInUsd = amount / fromRate
        return amountInUsd * lekRate
    }

    fun convertFromLek(amountInLek: Double, toCurrency: SupportedCurrency): Double {
        if (exchangeRates.isEmpty()) return amountInLek // Fallback
        if (toCurrency == SupportedCurrency.LEK) return amountInLek

        // Convert LEK -> USD -> to
        val lekRate = exchangeRates[SupportedCurrency.LEK.code] ?: return amountInLek
        val toRate = exchangeRates[toCurrency.code] ?: return amountInLek
        
        val amountInUsd = amountInLek / lekRate
        return amountInUsd * toRate
    }

    fun formatAmount(context: Context, amountInLek: Double): String {
        val currency = getCurrency(context)
        val convertedAmount = convertFromLek(amountInLek, currency)
        
        // Always use US locale for number formatting (e.g., 10,000.00)
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance("USD") // Temporary currency to get format
        
        // Format and remove the default symbol ($)
        val formattedNumber = format.format(convertedAmount).replace("$", "").trim()
        
        // Append the actual selected currency symbol at the end
        return "$formattedNumber ${currency.symbol}"
    }
}
