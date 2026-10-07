package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BtcColor
import com.example.ui.theme.DogeColor
import com.example.ui.theme.EthColor
import com.example.ui.theme.SolColor
import com.example.ui.theme.UsdtColor
import java.util.Locale

enum class CryptoCurrency(
    val symbol: String,
    val displayName: String,
    val usdRate: Double,
    val network: String,
    val color: Color,
    val defaultAddress: String,
    val minBet: Double,
    val defaultBet: Double,
    val decimals: Int
) {
    BTC(
        symbol = "BTC",
        displayName = "Bitcoin",
        usdRate = 96450.00,
        network = "Bitcoin Core (SegWit)",
        color = BtcColor,
        defaultAddress = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
        minBet = 0.0001,
        defaultBet = 0.0005,
        decimals = 6
    ),
    ETH(
        symbol = "ETH",
        displayName = "Ethereum",
        usdRate = 3480.00,
        network = "ERC-20 (Mainnet)",
        color = EthColor,
        defaultAddress = "0x71C...a8976C82B149",
        minBet = 0.002,
        defaultBet = 0.01,
        decimals = 4
    ),
    SOL(
        symbol = "SOL",
        displayName = "Solana",
        usdRate = 212.50,
        network = "Solana Mainnet-Beta",
        color = SolColor,
        defaultAddress = "8xZ7...9qRtM12",
        minBet = 0.05,
        defaultBet = 0.25,
        decimals = 3
    ),
    USDT(
        symbol = "USDT",
        displayName = "Tether USD",
        usdRate = 1.00,
        network = "TRC-20 / ERC-20",
        color = UsdtColor,
        defaultAddress = "TXy9...4kLp10",
        minBet = 5.0,
        defaultBet = 25.0,
        decimals = 2
    ),
    DOGE(
        symbol = "DOGE",
        displayName = "Dogecoin",
        usdRate = 0.22,
        network = "Dogecoin Network",
        color = DogeColor,
        defaultAddress = "DJ9p...83VwX",
        minBet = 20.0,
        defaultBet = 100.0,
        decimals = 1
    );

    fun formatAmount(amount: Double): String {
        return String.format(Locale.US, "%.${decimals}f %s", amount, symbol)
    }

    fun toUsdString(amount: Double): String {
        val usd = amount * usdRate
        return String.format(Locale.US, "$%,.2f", usd)
    }
}
