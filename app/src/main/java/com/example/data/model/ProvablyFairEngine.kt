package com.example.data.model

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.math.max

/**
 * Industry-standard Provably Fair cryptographic verification algorithm.
 * Uses SHA-256 hashes of Server Seeds, combined with Client Seed and Nonce,
 * ensuring outcomes are predetermined, tamper-proof, and fully verifiable.
 */
object ProvablyFairEngine {

    private val secureRandom = SecureRandom()

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun hmacSha256(key: String, message: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val hashBytes = mac.doFinal(message.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun generateServerSeed(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Standard Crash Provably Fair algorithm:
     * Derives a crash multiplier from HMAC-SHA256(serverSeed, "$clientSeed:$nonce").
     * Formula: 0.99 * (2^52) / (2^52 - h), capped at 1.00x - 1000.00x.
     */
    fun calculateCrashOutcome(serverSeed: String, clientSeed: String, nonce: Long): Double {
        val hash = hmacSha256(serverSeed, "$clientSeed:$nonce")
        // Take the first 13 hex characters (52 bits)
        val subHex = hash.take(13)
        val h = subHex.toLong(16)
        val e = Math.pow(2.0, 52.0)

        // 1 in 33 chance of immediate 1.00x house edge (standard in crypto casinos)
        if (h % 33L == 0L) {
            return 1.00
        }

        val multiplier = floor((100.0 * e - h) / (e - h)) / 100.0
        return max(1.01, multiplier.coerceAtMost(1000.0))
    }

    /**
     * Standard Dice Provably Fair algorithm:
     * Derives a roll between 0.00 and 99.99 from HMAC-SHA256.
     */
    fun calculateDiceOutcome(serverSeed: String, clientSeed: String, nonce: Long): Double {
        val hash = hmacSha256(serverSeed, "$clientSeed:$nonce")
        val subHex = hash.take(8)
        val intVal = subHex.toLong(16)
        val outcome = (intVal % 10000) / 100.0
        return outcome
    }

    /**
     * Standard Mines algorithm:
     * Generates mine positions (0..24) without replacement using Fisher-Yates shuffle seeded with HMAC.
     */
    fun calculateMinesLayout(
        serverSeed: String,
        clientSeed: String,
        nonce: Long,
        mineCount: Int
    ): Set<Int> {
        val positions = (0 until 25).toMutableList()
        val hash = hmacSha256(serverSeed, "$clientSeed:$nonce:mines")
        var seedOffset = 0

        for (i in 24 downTo (25 - mineCount)) {
            val slice = hash.substring((seedOffset % (hash.length - 8)), (seedOffset % (hash.length - 8)) + 4)
            seedOffset += 4
            val randIdx = (slice.toInt(16) % (i + 1))
            val temp = positions[i]
            positions[i] = positions[randIdx]
            positions[randIdx] = temp
        }

        return positions.takeLast(mineCount).toSet()
    }
}
