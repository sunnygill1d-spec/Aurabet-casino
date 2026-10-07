package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BetRecord
import com.example.data.model.BettingMarket
import com.example.data.model.CryptoCurrency
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LiveFeedBet
import com.example.data.model.ProvablyFairEngine
import com.example.data.model.ReferralData
import com.example.data.model.ReferralTier
import com.example.data.model.SupportChatMessage
import com.example.data.model.SupportTicket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class CrashGameState {
    object Idle : CrashGameState()
    data class Countdown(val secondsLeft: Int) : CrashGameState()
    data class Running(val currentMultiplier: Double) : CrashGameState()
    data class Crashed(val finalMultiplier: Double) : CrashGameState()
}

data class CrashPlayerLive(
    val name: String,
    val betAmount: Double,
    val currency: CryptoCurrency,
    val cashoutMultiplier: Double? = null,
    val isUser: Boolean = false
)

data class CasinoUiState(
    // Balances
    val balances: Map<CryptoCurrency, Double> = mapOf(
        CryptoCurrency.BTC to 0.045000,
        CryptoCurrency.ETH to 0.8500,
        CryptoCurrency.SOL to 14.50,
        CryptoCurrency.USDT to 1250.00,
        CryptoCurrency.DOGE to 1500.0
    ),
    val selectedCurrency: CryptoCurrency = CryptoCurrency.USDT,

    // Provably Fair
    val serverSeed: String = ProvablyFairEngine.generateServerSeed(),
    val serverSeedHash: String = "",
    val clientSeed: String = "aura_luck_" + Random.nextInt(1000, 9999),
    val nonce: Long = 42L,

    // Security & Biometrics
    val isBiometricsEnabled: Boolean = true,
    val isAppLocked: Boolean = false,
    val securityPin: String = "7788",

    // Live Ticker & Feed
    val liveFeed: List<LiveFeedBet> = emptyList(),

    // Crash Game
    val crashState: CrashGameState = CrashGameState.Idle,
    val crashBetAmount: Double = 10.0,
    val crashAutoCashout: Double? = 2.00,
    val isCrashBetActive: Boolean = false,
    val crashCashedOut: Boolean = false,
    val crashCashoutMultiplier: Double? = null,
    val crashHistory: List<Double> = listOf(1.42, 3.10, 1.15, 12.44, 2.05, 1.88, 5.60, 1.02),
    val crashLivePlayers: List<CrashPlayerLive> = emptyList(),

    // Mines Game
    val minesCount: Int = 3,
    val minesBetAmount: Double = 10.0,
    val isMinesActive: Boolean = false,
    val minesRevealed: Set<Int> = emptySet(),
    val minePositions: Set<Int> = emptySet(),
    val isMinesExploded: Boolean = false,
    val explodedMineIndex: Int? = null,
    val currentMinesMultiplier: Double = 1.0,

    // Dice Game
    val diceBetAmount: Double = 10.0,
    val diceTarget: Double = 50.50,
    val isDiceRollUnder: Boolean = true,
    val isDiceRolling: Boolean = false,
    val lastDiceRoll: Double? = null,
    val lastDiceWon: Boolean? = null,

    // Markets
    val bettingMarkets: List<BettingMarket> = emptyList(),

    // History
    val betHistory: List<BetRecord> = emptyList(),

    // Referrals
    val referralData: ReferralData = ReferralData(),
    val referralLeaderboard: List<LeaderboardEntry> = emptyList(),

    // Support
    val supportMessages: List<SupportChatMessage> = emptyList(),
    val supportTickets: List<SupportTicket> = emptyList(),

    // Push notification banner simulation
    val activeNotificationBanner: String? = null
)

class CasinoViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CasinoUiState())
    val uiState: StateFlow<CasinoUiState> = _uiState.asStateFlow()

    private var crashJob: Job? = null
    private var tickerJob: Job? = null
    private var marketsJob: Job? = null

    init {
        // Initialize Server Seed Hash
        val initialSeed = _uiState.value.serverSeed
        val hash = ProvablyFairEngine.sha256(initialSeed)
        _uiState.update { it.copy(serverSeedHash = hash) }

        initSampleData()
        startLiveTickerBackground()
        startMarketsSimulation()
        startPeriodicCrashRound()
    }

    private fun initSampleData() {
        val initialMarkets = listOf(
            BettingMarket(
                id = "mkt_1",
                title = "BTC Breaks $100,000 Before Midnight UTC",
                category = "Crypto Price",
                asset = "BTC",
                expiryMinutes = 180,
                yesOdds = 1.85,
                noOdds = 1.95,
                poolVolumeUsd = 142500.0,
                resolutionCriteria = "Binance BTC/USDT spot index closes >= $100k at 23:59 UTC"
            ),
            BettingMarket(
                id = "mkt_2",
                title = "Ethereum Pectra Upgrade Mainnet Date Confirmed",
                category = "DeFi Milestone",
                asset = "ETH",
                expiryMinutes = 720,
                yesOdds = 2.40,
                noOdds = 1.55,
                poolVolumeUsd = 89200.0,
                resolutionCriteria = "Official Ethereum Foundation blog announcement"
            ),
            BettingMarket(
                id = "mkt_3",
                title = "Solana Daily Active Wallets Surpasses 5M",
                category = "Network Stats",
                asset = "SOL",
                expiryMinutes = 360,
                yesOdds = 1.62,
                noOdds = 2.20,
                poolVolumeUsd = 63800.0,
                resolutionCriteria = "Artemis Analytics 24h unique signers metric"
            ),
            BettingMarket(
                id = "mkt_4",
                title = "Dogecoin Integrated as X Payments Currency in Q4",
                category = "Ecosystem",
                asset = "DOGE",
                expiryMinutes = 1440,
                yesOdds = 3.25,
                noOdds = 1.30,
                poolVolumeUsd = 112000.0,
                resolutionCriteria = "Public rollout or official declaration on @XPayments"
            )
        )

        val initialHistory = listOf(
            BetRecord(
                id = "BET-9941",
                gameType = "Crash",
                currency = CryptoCurrency.USDT,
                betAmount = 25.0,
                multiplier = 3.42,
                payout = 85.50,
                isWin = true,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 12,
                serverSeedHash = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
                serverSeedRevealed = "d4e2a1b98c3f4e5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a",
                clientSeed = "lucky_strike_99",
                nonce = 39,
                gameDetails = "Cashed out at 3.42x (Bust: 4.88x)"
            ),
            BetRecord(
                id = "BET-9940",
                gameType = "Mines",
                currency = CryptoCurrency.SOL,
                betAmount = 0.50,
                multiplier = 2.28,
                payout = 1.14,
                isWin = true,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 25,
                serverSeedHash = "3a42c12948bdfb9e81d7c3f81e3a241b123d4e5f6a7b8c9d0e1f2a3b4c5d6e7f",
                serverSeedRevealed = "11223344556677889900aabbccddeeff00112233445566778899aabbccddeeff",
                clientSeed = "high_roller_v2",
                nonce = 38,
                gameDetails = "3 Mines, 4 Diamonds cleared"
            ),
            BetRecord(
                id = "BET-9939",
                gameType = "Dice",
                currency = CryptoCurrency.BTC,
                betAmount = 0.0005,
                multiplier = 1.98,
                payout = 0.0,
                isWin = false,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 48,
                serverSeedHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                serverSeedRevealed = "5f4dcc3b5aa765d61d8327deb882cf992b322a5542a222123d5a2d2f4412389a",
                clientSeed = "cryptoking_01",
                nonce = 37,
                gameDetails = "Rolled 64.20 (Needed < 50.00)"
            ),
            BetRecord(
                id = "BET-9938",
                gameType = "Market",
                currency = CryptoCurrency.USDT,
                betAmount = 50.0,
                multiplier = 1.85,
                payout = 92.50,
                isWin = true,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 90,
                serverSeedHash = "a1b2c3d4e5f67890123456789abcdef0123456789abcdef0123456789abcdef0",
                serverSeedRevealed = "c0ffee112233445566778899aabbccddeeff00112233445566778899aabbccdd",
                clientSeed = "market_bull_88",
                nonce = 36,
                gameDetails = "Prediction: BTC > $98,000 (YES)"
            )
        )

        val initialLeaderboard = listOf(
            LeaderboardEntry(1, "CryptoWhale_007", 142, 458000.0, ReferralTier.DIAMOND),
            LeaderboardEntry(2, "SatoshiGhost", 98, 289000.0, ReferralTier.DIAMOND),
            LeaderboardEntry(3, "SolanaSurfer", 64, 184500.0, ReferralTier.GOLD),
            LeaderboardEntry(4, "NeonRoller", 42, 98200.0, ReferralTier.GOLD),
            LeaderboardEntry(5, "AuraVIP_Ace", 31, 62100.0, ReferralTier.SILVER),
            LeaderboardEntry(6, "You (AURA-779X)", 18, 34500.0, ReferralTier.SILVER)
        )

        val initialSupport = listOf(
            SupportChatMessage(
                id = "msg_1",
                sender = "AuraSupport Bot",
                message = "Welcome to AuraBet 24/7 VIP Support! How can we assist you with deposits, provably fair verifications, or VIP tiers today?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                isFromUser = false
            )
        )

        val initialTickets = listOf(
            SupportTicket(
                ticketId = "TKT-8821",
                subject = "Fast TRC-20 deposit confirmation check",
                category = "Deposit/Withdrawal",
                status = "Resolved",
                priority = "VIP"
            )
        )

        _uiState.update {
            it.copy(
                bettingMarkets = initialMarkets,
                betHistory = initialHistory,
                referralLeaderboard = initialLeaderboard,
                supportMessages = initialSupport,
                supportTickets = initialTickets
            )
        }
    }

    // -------------------------------------------------------------
    // CURRENCY & WALLET OPERATIONS
    // -------------------------------------------------------------
    fun selectCurrency(currency: CryptoCurrency) {
        _uiState.update {
            it.copy(
                selectedCurrency = currency,
                crashBetAmount = currency.defaultBet,
                minesBetAmount = currency.defaultBet,
                diceBetAmount = currency.defaultBet
            )
        }
    }

    fun depositFunds(currency: CryptoCurrency, amount: Double) {
        _uiState.update { current ->
            val updated = current.balances.toMutableMap()
            updated[currency] = (updated[currency] ?: 0.0) + amount
            current.copy(
                balances = updated,
                activeNotificationBanner = "Instant Deposit Confirmed: +${currency.formatAmount(amount)}"
            )
        }
        vibrate(50)
    }

    fun withdrawFunds(currency: CryptoCurrency, amount: Double, toAddress: String): Boolean {
        val currentBalance = _uiState.value.balances[currency] ?: 0.0
        val gasFee = amount * 0.005 // 0.5% network fee
        val totalDeduction = amount + gasFee

        if (currentBalance < totalDeduction || amount <= 0.0) {
            return false
        }

        _uiState.update { current ->
            val updated = current.balances.toMutableMap()
            updated[currency] = currentBalance - totalDeduction
            current.copy(
                balances = updated,
                activeNotificationBanner = "Withdrawal Broadcasted: -${currency.formatAmount(amount)} to ${toAddress.take(6)}...${toAddress.takeLast(4)}"
            )
        }
        vibrate(80)
        return true
    }

    // -------------------------------------------------------------
    // PROVABLY FAIR SEED MANAGEMENT
    // -------------------------------------------------------------
    fun rotateServerSeed() {
        val newSeed = ProvablyFairEngine.generateServerSeed()
        val newHash = ProvablyFairEngine.sha256(newSeed)
        _uiState.update {
            it.copy(
                serverSeed = newSeed,
                serverSeedHash = newHash,
                nonce = 0L,
                activeNotificationBanner = "Provably Fair Seeds Rotated & Hashed with SHA-256"
            )
        }
    }

    fun updateClientSeed(newClientSeed: String) {
        if (newClientSeed.isNotBlank()) {
            _uiState.update {
                it.copy(
                    clientSeed = newClientSeed.trim(),
                    activeNotificationBanner = "Client Seed Updated: ${newClientSeed.trim()}"
                )
            }
        }
    }

    // -------------------------------------------------------------
    // BIOMETRIC AUTHENTICATION & SECURITY
    // -------------------------------------------------------------
    fun toggleBiometrics(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricsEnabled = enabled) }
    }

    fun lockApp() {
        if (_uiState.value.isBiometricsEnabled) {
            _uiState.update { it.copy(isAppLocked = true) }
        }
    }

    fun unlockAppWithBiometrics(): Boolean {
        vibrate(40)
        _uiState.update { it.copy(isAppLocked = false) }
        return true
    }

    fun unlockAppWithPin(enteredPin: String): Boolean {
        return if (enteredPin == _uiState.value.securityPin) {
            vibrate(40)
            _uiState.update { it.copy(isAppLocked = false) }
            true
        } else {
            vibrate(120)
            false
        }
    }

    // -------------------------------------------------------------
    // CRASH GAME ENGINE
    // -------------------------------------------------------------
    fun setCrashBetAmount(amount: Double) {
        _uiState.update { it.copy(crashBetAmount = amount) }
    }

    fun setCrashAutoCashout(multiplier: Double?) {
        _uiState.update { it.copy(crashAutoCashout = multiplier) }
    }

    fun placeCrashBet() {
        val state = _uiState.value
        val balance = state.balances[state.selectedCurrency] ?: 0.0
        if (balance < state.crashBetAmount || state.crashBetAmount <= 0.0) return
        if (state.isCrashBetActive) return

        // Deduct bet amount
        val updatedBalances = state.balances.toMutableMap()
        updatedBalances[state.selectedCurrency] = balance - state.crashBetAmount

        _uiState.update {
            it.copy(
                balances = updatedBalances,
                isCrashBetActive = true,
                crashCashedOut = false,
                crashCashoutMultiplier = null
            )
        }
        vibrate(30)
    }

    fun cashoutCrash() {
        val state = _uiState.value
        val crash = state.crashState
        if (!state.isCrashBetActive || state.crashCashedOut) return
        if (crash !is CrashGameState.Running) return

        val cashoutMultiplier = crash.currentMultiplier
        val winAmount = state.crashBetAmount * cashoutMultiplier
        val updatedBalances = state.balances.toMutableMap()
        updatedBalances[state.selectedCurrency] = (updatedBalances[state.selectedCurrency] ?: 0.0) + winAmount

        val revealedSeed = state.serverSeed
        val record = BetRecord(
            id = "BET-" + Random.nextInt(1000, 9999),
            gameType = "Crash",
            currency = state.selectedCurrency,
            betAmount = state.crashBetAmount,
            multiplier = cashoutMultiplier,
            payout = winAmount,
            isWin = true,
            serverSeedHash = state.serverSeedHash,
            serverSeedRevealed = revealedSeed,
            clientSeed = state.clientSeed,
            nonce = state.nonce,
            gameDetails = String.format(Locale.US, "Cashed at %.2fx", cashoutMultiplier)
        )

        _uiState.update {
            it.copy(
                balances = updatedBalances,
                crashCashedOut = true,
                crashCashoutMultiplier = cashoutMultiplier,
                betHistory = listOf(record) + it.betHistory,
                activeNotificationBanner = String.format(Locale.US, "CRASH CASHOUT: Won +%s (%.2fx)!", state.selectedCurrency.formatAmount(winAmount), cashoutMultiplier)
            )
        }
        vibrate(60)
    }

    private fun startPeriodicCrashRound() {
        crashJob?.cancel()
        crashJob = viewModelScope.launch {
            while (isActive) {
                // 1. Countdown Phase (5 seconds)
                val mockPlayers = generateCrashMockPlayers(_uiState.value.selectedCurrency)
                _uiState.update {
                    it.copy(
                        crashLivePlayers = mockPlayers,
                        crashState = CrashGameState.Countdown(5)
                    )
                }

                for (sec in 4 downTo 1) {
                    delay(1000)
                    _uiState.update { it.copy(crashState = CrashGameState.Countdown(sec)) }
                }

                // Increment nonce & calculate Provably Fair crash point
                val currentNonce = _uiState.value.nonce + 1
                val serverSeed = _uiState.value.serverSeed
                val clientSeed = _uiState.value.clientSeed
                val calculatedCrashPoint = ProvablyFairEngine.calculateCrashOutcome(serverSeed, clientSeed, currentNonce)

                _uiState.update {
                    it.copy(
                        nonce = currentNonce,
                        crashState = CrashGameState.Running(1.00)
                    )
                }

                // 2. Running Phase
                var currentMultiplier = 1.00
                val startTime = System.currentTimeMillis()
                val growthRate = 0.06

                while (currentMultiplier < calculatedCrashPoint && isActive) {
                    delay(50) // 20 FPS high-performance smooth curve
                    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                    currentMultiplier = 1.00 + (elapsedSec.pow(1.6) * growthRate) + (elapsedSec * 0.15)
                    val roundedMultiplier = ((currentMultiplier * 100).roundToInt()) / 100.0

                    if (roundedMultiplier >= calculatedCrashPoint) {
                        currentMultiplier = calculatedCrashPoint
                        break
                    }

                    // Check auto-cashout for user
                    val currentState = _uiState.value
                    if (currentState.isCrashBetActive && !currentState.crashCashedOut) {
                        val autoVal = currentState.crashAutoCashout
                        if (autoVal != null && roundedMultiplier >= autoVal) {
                            cashoutCrash()
                        }
                    }

                    // Update mock players random cashouts
                    updateCrashMockPlayers(roundedMultiplier)

                    _uiState.update { it.copy(crashState = CrashGameState.Running(roundedMultiplier)) }
                }

                // 3. Crashed Phase
                val finalCrashPoint = calculatedCrashPoint
                val wasActiveAndLost = _uiState.value.isCrashBetActive && !_uiState.value.crashCashedOut

                if (wasActiveAndLost) {
                    val lostRecord = BetRecord(
                        id = "BET-" + Random.nextInt(1000, 9999),
                        gameType = "Crash",
                        currency = _uiState.value.selectedCurrency,
                        betAmount = _uiState.value.crashBetAmount,
                        multiplier = 0.0,
                        payout = 0.0,
                        isWin = false,
                        serverSeedHash = _uiState.value.serverSeedHash,
                        serverSeedRevealed = _uiState.value.serverSeed,
                        clientSeed = _uiState.value.clientSeed,
                        nonce = currentNonce,
                        gameDetails = String.format(Locale.US, "Crashed at %.2fx", finalCrashPoint)
                    )
                    _uiState.update {
                        it.copy(betHistory = listOf(lostRecord) + it.betHistory)
                    }
                    vibrate(100)
                }

                _uiState.update {
                    it.copy(
                        crashState = CrashGameState.Crashed(finalCrashPoint),
                        crashHistory = (listOf(finalCrashPoint) + it.crashHistory).take(12),
                        isCrashBetActive = false,
                        crashCashedOut = false,
                        crashCashoutMultiplier = null
                    )
                }

                // Rotate seed periodically for provable transparency
                if (currentNonce % 10L == 0L) {
                    val newSeed = ProvablyFairEngine.generateServerSeed()
                    _uiState.update {
                        it.copy(
                            serverSeed = newSeed,
                            serverSeedHash = ProvablyFairEngine.sha256(newSeed)
                        )
                    }
                }

                delay(3000) // 3 seconds pause before next round
            }
        }
    }

    private fun generateCrashMockPlayers(currency: CryptoCurrency): List<CrashPlayerLive> {
        val names = listOf("Alex_Degen", "MoonHodler", "NeonSniper", "SatoshiFan", "AlphaBull", "ViperRoll")
        return names.shuffled().take(4).map { name ->
            CrashPlayerLive(
                name = name,
                betAmount = currency.defaultBet * (Random.nextInt(1, 5)),
                currency = currency
            )
        }
    }

    private fun updateCrashMockPlayers(currentMultiplier: Double) {
        _uiState.update { state ->
            val updated = state.crashLivePlayers.map { p ->
                if (p.cashoutMultiplier == null && Random.nextFloat() < 0.03 && currentMultiplier > 1.3) {
                    p.copy(cashoutMultiplier = currentMultiplier)
                } else p
            }
            state.copy(crashLivePlayers = updated)
        }
    }

    // -------------------------------------------------------------
    // MINES GAME ENGINE
    // -------------------------------------------------------------
    fun setMinesCount(count: Int) {
        if (!_uiState.value.isMinesActive) {
            _uiState.update { it.copy(minesCount = count.coerceIn(1, 24)) }
        }
    }

    fun setMinesBetAmount(amount: Double) {
        if (!_uiState.value.isMinesActive) {
            _uiState.update { it.copy(minesBetAmount = amount) }
        }
    }

    fun startMinesGame() {
        val state = _uiState.value
        val balance = state.balances[state.selectedCurrency] ?: 0.0
        if (balance < state.minesBetAmount || state.minesBetAmount <= 0.0) return

        val updatedBalances = state.balances.toMutableMap()
        updatedBalances[state.selectedCurrency] = balance - state.minesBetAmount

        val nextNonce = state.nonce + 1
        val minePositions = ProvablyFairEngine.calculateMinesLayout(
            state.serverSeed,
            state.clientSeed,
            nextNonce,
            state.minesCount
        )

        _uiState.update {
            it.copy(
                balances = updatedBalances,
                nonce = nextNonce,
                isMinesActive = true,
                minesRevealed = emptySet(),
                minePositions = minePositions,
                isMinesExploded = false,
                explodedMineIndex = null,
                currentMinesMultiplier = 1.0
            )
        }
        vibrate(30)
    }

    fun pickMinesTile(tileIndex: Int) {
        val state = _uiState.value
        if (!state.isMinesActive || state.isMinesExploded) return
        if (state.minesRevealed.contains(tileIndex)) return

        if (state.minePositions.contains(tileIndex)) {
            // Exploded!
            val record = BetRecord(
                id = "BET-" + Random.nextInt(1000, 9999),
                gameType = "Mines",
                currency = state.selectedCurrency,
                betAmount = state.minesBetAmount,
                multiplier = 0.0,
                payout = 0.0,
                isWin = false,
                serverSeedHash = state.serverSeedHash,
                serverSeedRevealed = state.serverSeed,
                clientSeed = state.clientSeed,
                nonce = state.nonce,
                gameDetails = "${state.minesCount} Mines: Exploded at tile #$tileIndex"
            )

            _uiState.update {
                it.copy(
                    isMinesActive = false,
                    isMinesExploded = true,
                    explodedMineIndex = tileIndex,
                    minesRevealed = it.minesRevealed + tileIndex,
                    betHistory = listOf(record) + it.betHistory,
                    activeNotificationBanner = "Mines: Detonated! Bet lost."
                )
            }
            vibrate(150)
        } else {
            // Uncovered Diamond Gem!
            val newRevealed = state.minesRevealed + tileIndex
            val diamondsFound = newRevealed.size
            val newMultiplier = calculateMinesMultiplier(state.minesCount, diamondsFound)

            _uiState.update {
                it.copy(
                    minesRevealed = newRevealed,
                    currentMinesMultiplier = newMultiplier
                )
            }
            vibrate(30)
        }
    }

    fun cashoutMines() {
        val state = _uiState.value
        if (!state.isMinesActive || state.isMinesExploded) return
        if (state.minesRevealed.isEmpty()) return

        val winAmount = state.minesBetAmount * state.currentMinesMultiplier
        val updatedBalances = state.balances.toMutableMap()
        updatedBalances[state.selectedCurrency] = (updatedBalances[state.selectedCurrency] ?: 0.0) + winAmount

        val record = BetRecord(
            id = "BET-" + Random.nextInt(1000, 9999),
            gameType = "Mines",
            currency = state.selectedCurrency,
            betAmount = state.minesBetAmount,
            multiplier = state.currentMinesMultiplier,
            payout = winAmount,
            isWin = true,
            serverSeedHash = state.serverSeedHash,
            serverSeedRevealed = state.serverSeed,
            clientSeed = state.clientSeed,
            nonce = state.nonce,
            gameDetails = String.format(Locale.US, "%d Mines: %d Gems found (%.2fx)", state.minesCount, state.minesRevealed.size, state.currentMinesMultiplier)
        )

        _uiState.update {
            it.copy(
                balances = updatedBalances,
                isMinesActive = false,
                betHistory = listOf(record) + it.betHistory,
                activeNotificationBanner = String.format(Locale.US, "Mines Cashed Out: +%s (%.2fx)!", state.selectedCurrency.formatAmount(winAmount), state.currentMinesMultiplier)
            )
        }
        vibrate(60)
    }

    private fun calculateMinesMultiplier(mines: Int, gemsFound: Int): Double {
        var mult = 0.99
        val totalTiles = 25
        for (i in 0 until gemsFound) {
            val safeLeft = (totalTiles - mines - i).toDouble()
            val totalLeft = (totalTiles - i).toDouble()
            mult *= (totalLeft / safeLeft)
        }
        return ((mult * 100).roundToInt()) / 100.0
    }

    // -------------------------------------------------------------
    // CRYPTO DICE ENGINE
    // -------------------------------------------------------------
    fun setDiceBetAmount(amount: Double) {
        _uiState.update { it.copy(diceBetAmount = amount) }
    }

    fun setDiceTarget(target: Double) {
        _uiState.update { it.copy(diceTarget = target.coerceIn(1.0, 98.0)) }
    }

    fun toggleDiceRollMode() {
        _uiState.update { it.copy(isDiceRollUnder = !it.isDiceRollUnder) }
    }

    fun rollDice() {
        val state = _uiState.value
        val balance = state.balances[state.selectedCurrency] ?: 0.0
        if (balance < state.diceBetAmount || state.diceBetAmount <= 0.0) return

        val updatedBalances = state.balances.toMutableMap()
        updatedBalances[state.selectedCurrency] = balance - state.diceBetAmount

        val nextNonce = state.nonce + 1
        val winChance = if (state.isDiceRollUnder) state.diceTarget else (100.0 - state.diceTarget)
        val multiplier = ((99.0 / winChance) * 100).roundToInt() / 100.0

        _uiState.update {
            it.copy(
                balances = updatedBalances,
                nonce = nextNonce,
                isDiceRolling = true
            )
        }

        viewModelScope.launch {
            delay(350) // tactile dice roll delay
            val rolledValue = ProvablyFairEngine.calculateDiceOutcome(state.serverSeed, state.clientSeed, nextNonce)
            val won = if (state.isDiceRollUnder) rolledValue < state.diceTarget else rolledValue > state.diceTarget

            val payout = if (won) state.diceBetAmount * multiplier else 0.0
            if (won) {
                val finalBalances = _uiState.value.balances.toMutableMap()
                finalBalances[state.selectedCurrency] = (finalBalances[state.selectedCurrency] ?: 0.0) + payout
                _uiState.update { it.copy(balances = finalBalances) }
                vibrate(60)
            } else {
                vibrate(100)
            }

            val record = BetRecord(
                id = "BET-" + Random.nextInt(1000, 9999),
                gameType = "Dice",
                currency = state.selectedCurrency,
                betAmount = state.diceBetAmount,
                multiplier = if (won) multiplier else 0.0,
                payout = payout,
                isWin = won,
                serverSeedHash = state.serverSeedHash,
                serverSeedRevealed = state.serverSeed,
                clientSeed = state.clientSeed,
                nonce = nextNonce,
                gameDetails = String.format(Locale.US, "Rolled %.2f (%s %.2f)", rolledValue, if (state.isDiceRollUnder) "<" else ">", state.diceTarget)
            )

            _uiState.update {
                it.copy(
                    isDiceRolling = false,
                    lastDiceRoll = rolledValue,
                    lastDiceWon = won,
                    betHistory = listOf(record) + it.betHistory,
                    activeNotificationBanner = if (won) String.format(Locale.US, "Dice Won: +%s (%.2fx)!", state.selectedCurrency.formatAmount(payout), multiplier) else "Dice Lost: Rolled ${String.format(Locale.US, "%.2f", rolledValue)}"
                )
            }
        }
    }

    // -------------------------------------------------------------
    // LIVE PREDICTION MARKETS
    // -------------------------------------------------------------
    fun placeMarketBet(marketId: String, isYes: Boolean, betAmount: Double): Boolean {
        val state = _uiState.value
        val market = state.bettingMarkets.find { it.id == marketId } ?: return false
        val balance = state.balances[state.selectedCurrency] ?: 0.0
        if (balance < betAmount || betAmount <= 0.0) return false

        val updatedBalances = state.balances.toMutableMap()
        updatedBalances[state.selectedCurrency] = balance - betAmount

        val odds = if (isYes) market.yesOdds else market.noOdds
        val potentialPayout = betAmount * odds

        val record = BetRecord(
            id = "BET-" + Random.nextInt(1000, 9999),
            gameType = "Market",
            currency = state.selectedCurrency,
            betAmount = betAmount,
            multiplier = odds,
            payout = potentialPayout,
            isWin = true,
            serverSeedHash = state.serverSeedHash,
            serverSeedRevealed = state.serverSeed,
            clientSeed = state.clientSeed,
            nonce = state.nonce + 1,
            gameDetails = "${market.asset}: ${if (isYes) "YES" else "NO"} @ ${odds}x"
        )

        _uiState.update {
            it.copy(
                balances = updatedBalances,
                nonce = it.nonce + 1,
                betHistory = listOf(record) + it.betHistory,
                activeNotificationBanner = "Market Bet Placed: ${market.asset} ${if (isYes) "YES" else "NO"} @ ${odds}x"
            )
        }
        vibrate(50)
        return true
    }

    private fun startMarketsSimulation() {
        marketsJob?.cancel()
        marketsJob = viewModelScope.launch {
            while (isActive) {
                delay(6000) // update market odds realistically
                _uiState.update { state ->
                    val updated = state.bettingMarkets.map { m ->
                        val delta = (Random.nextInt(-3, 4)) * 0.01
                        val newYes = ((m.yesOdds + delta).coerceIn(1.10, 4.50) * 100).roundToInt() / 100.0
                        val newNo = (((1.0 / (1.0 - (1.0 / newYes))) * 0.95).coerceIn(1.10, 4.50) * 100).roundToInt() / 100.0
                        val trend = if (delta > 0) 1 else if (delta < 0) -1 else 0
                        m.copy(yesOdds = newYes, noOdds = newNo, yesOddsTrend = trend)
                    }
                    state.copy(bettingMarkets = updated)
                }
            }
        }
    }

    // -------------------------------------------------------------
    // LIVE TICKER BACKGROUND
    // -------------------------------------------------------------
    private fun startLiveTickerBackground() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            val games = listOf("Crash", "Mines", "Dice", "Market")
            val tags = listOf("AuraKing", "CryptoSlick", "Whale_88", "HyperRoll", "LuckyDuck", "DegenQueen", "SolWarrior")
            val currencies = CryptoCurrency.values().toList()

            while (isActive) {
                delay(3500)
                val coin = currencies.random()
                val isWin = Random.nextFloat() < 0.55
                val mult = if (isWin) (Random.nextInt(120, 850) / 100.0) else 0.0
                val amount = coin.defaultBet * Random.nextInt(1, 4)
                val newBet = LiveFeedBet(
                    playerTag = tags.random(),
                    game = games.random(),
                    currency = coin,
                    betAmount = amount,
                    multiplier = mult,
                    isWin = isWin
                )

                _uiState.update {
                    it.copy(liveFeed = (listOf(newBet) + it.liveFeed).take(10))
                }
            }
        }
    }

    // -------------------------------------------------------------
    // REFERRAL PROGRAM
    // -------------------------------------------------------------
    fun claimReferralRewards(): Boolean {
        val unclaimed = _uiState.value.referralData.unclaimedUsdt
        if (unclaimed <= 0.0) return false

        _uiState.update { current ->
            val updatedBalances = current.balances.toMutableMap()
            updatedBalances[CryptoCurrency.USDT] = (updatedBalances[CryptoCurrency.USDT] ?: 0.0) + unclaimed

            val newReferralData = current.referralData.copy(
                unclaimedUsdt = 0.0,
                lifetimeEarnedUsdt = current.referralData.lifetimeEarnedUsdt + unclaimed
            )

            current.copy(
                balances = updatedBalances,
                referralData = newReferralData,
                activeNotificationBanner = String.format(Locale.US, "Claimed $%.2f USDT in referral commissions!", unclaimed)
            )
        }
        vibrate(70)
        return true
    }

    // -------------------------------------------------------------
    // 24/7 CUSTOMER SUPPORT
    // -------------------------------------------------------------
    fun sendSupportMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = SupportChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "You",
            message = userText.trim(),
            timestamp = System.currentTimeMillis(),
            isFromUser = true
        )

        _uiState.update {
            it.copy(supportMessages = it.supportMessages + userMsg)
        }

        viewModelScope.launch {
            delay(1200) // realistic instant response
            val lower = userText.lowercase()
            val replyText = when {
                lower.contains("deposit") || lower.contains("withdraw") || lower.contains("wallet") ->
                    "Deposits are credited instantly with 0 block confirmation waiting on our testnet node! For withdrawals, ensure biometric authorization is confirmed."
                lower.contains("provably fair") || lower.contains("seed") || lower.contains("fair") ->
                    "Every round uses HMAC-SHA256! You can inspect the Server Seed SHA-256 hash before betting and verify the unhashed seed after the round in the Built-in Verifier."
                lower.contains("referral") || lower.contains("commission") || lower.contains("affiliate") ->
                    "Our 4-tier affiliate program pays up to 25% revenue share in USDT with zero lockup. You can claim earned commissions directly to your wallet anytime."
                lower.contains("biometric") || lower.contains("fingerprint") || lower.contains("security") ->
                    "Biometric security encrypts your session with Android Keyguard & Secure Enclave simulation. High-roller transactions mandate biometric signing."
                else ->
                    "Thank you for contacting AuraBet 24/7 VIP Concierge! A senior crypto risk agent is monitoring this chat. How can we make your betting experience even smoother?"
            }

            val botMsg = SupportChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "AuraSupport Specialist",
                message = replyText,
                timestamp = System.currentTimeMillis(),
                isFromUser = false
            )
            _uiState.update { it.copy(supportMessages = it.supportMessages + botMsg) }
            vibrate(40)
        }
    }

    fun createSupportTicket(subject: String, category: String) {
        val newTicket = SupportTicket(
            ticketId = "TKT-" + Random.nextInt(1000, 9999),
            subject = subject,
            category = category,
            status = "Agent Assigned",
            priority = "VIP"
        )
        _uiState.update {
            it.copy(
                supportTickets = listOf(newTicket) + it.supportTickets,
                activeNotificationBanner = "Support Ticket #${newTicket.ticketId} Created! An agent has been assigned."
            )
        }
    }

    fun dismissNotificationBanner() {
        _uiState.update { it.copy(activeNotificationBanner = null) }
    }

    private fun vibrate(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
