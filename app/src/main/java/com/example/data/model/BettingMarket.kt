package com.example.data.model

data class BettingMarket(
    val id: String,
    val title: String,
    val category: String, // "Crypto Price", "DeFi Milestone", "Network Stats"
    val asset: String,    // "BTC", "ETH", "SOL"
    val expiryMinutes: Int,
    val yesOdds: Double,
    val noOdds: Double,
    val yesOddsTrend: Int = 0, // -1 down, 0 neutral, 1 up
    val poolVolumeUsd: Double,
    val resolutionCriteria: String
)

data class LiveFeedBet(
    val playerTag: String,
    val game: String,
    val currency: CryptoCurrency,
    val betAmount: Double,
    val multiplier: Double,
    val isWin: Boolean
)
