package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BetRecord(
    val id: String,
    val gameType: String, // "Crash", "Mines", "Dice", "Market"
    val currency: CryptoCurrency,
    val betAmount: Double,
    val multiplier: Double,
    val payout: Double,
    val isWin: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val serverSeedHash: String,
    val serverSeedRevealed: String,
    val clientSeed: String,
    val nonce: Long,
    val gameDetails: String = "" // e.g. "Target 2.50x, Cashed at 2.50x" or "Mines: 3 opened"
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    val profit: Double
        get() = if (isWin) payout - betAmount else -betAmount
}
