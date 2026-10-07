package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SupportTicket(
    val ticketId: String,
    val subject: String,
    val category: String, // "Deposit/Withdrawal", "Provably Fair", "Account/Biometrics", "General"
    val status: String,   // "Open", "Agent Assigned", "Resolved"
    val timestamp: Long = System.currentTimeMillis(),
    val priority: String  // "High", "Normal", "VIP"
)

data class SupportChatMessage(
    val id: String,
    val sender: String, // "User", "AuraSupport Bot", "Senior Agent Alex"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromUser: Boolean
) {
    fun formattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

enum class ReferralTier(
    val tierName: String,
    val commissionPercent: Int,
    val requiredReferrals: Int,
    val badgeColorHex: Long
) {
    BRONZE("Bronze Affiliate", 5, 0, 0xFFCD7F32),
    SILVER("Silver VIP", 10, 5, 0xFFC0C0C0),
    GOLD("Gold High-Roller", 15, 20, 0xFFFFD700),
    DIAMOND("Diamond Elite", 25, 50, 0xFF00E5FF)
}

data class ReferralData(
    val referralCode: String = "AURA-779X",
    val inviteUrl: String = "https://aurabet.crypto/ref/AURA-779X",
    val totalInvited: Int = 18,
    val activeBettors: Int = 12,
    val totalVolumeUsd: Double = 34500.0,
    val currentTier: ReferralTier = ReferralTier.SILVER,
    val unclaimedUsdt: Double = 142.50,
    val lifetimeEarnedUsdt: Double = 890.00
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val totalReferrals: Int,
    val volumeUsd: Double,
    val tier: ReferralTier
)
