package com.example.ui.screens.games

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BettingMarket
import com.example.data.model.CryptoCurrency
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PredictionMarketsView(
    markets: List<BettingMarket>,
    selectedCurrency: CryptoCurrency,
    balance: Double,
    onPlaceBet: (String, Boolean, Double) -> Boolean
) {
    val context = LocalContext.current
    var selectedSlipMarket by remember { mutableStateOf<BettingMarket?>(null) }
    var selectedSlipIsYes by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("prediction_markets_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Market category banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACTIVE PREDICTION MARKETS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "Live Odds Updates",
                fontSize = 11.sp,
                color = NeonEmerald
            )
        }

        markets.forEach { market ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header tag & timer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${market.asset} • ${market.category}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${market.expiryMinutes / 60}h remaining",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = market.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = market.resolutionCriteria,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Volume & Odds Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "24h Volume", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = String.format(Locale.US, "$%,.0f", market.poolVolumeUsd),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // YES Odds button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonEmerald.copy(alpha = 0.15f))
                                    .border(1.dp, NeonEmerald, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val betAmt = selectedCurrency.defaultBet
                                        val success = onPlaceBet(market.id, true, betAmt)
                                        if (success) {
                                            Toast.makeText(context, "Placed YES bet: ${selectedCurrency.formatAmount(betAmt)} @ ${market.yesOdds}x", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "YES", fontWeight = FontWeight.Black, fontSize = 12.sp, color = NeonEmerald)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${market.yesOdds}x",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (market.yesOddsTrend > 0) {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(12.dp))
                                    } else if (market.yesOddsTrend < 0) {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = ElectricCrimson, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }

                            // NO Odds button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricCrimson.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricCrimson, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val betAmt = selectedCurrency.defaultBet
                                        val success = onPlaceBet(market.id, false, betAmt)
                                        if (success) {
                                            Toast.makeText(context, "Placed NO bet: ${selectedCurrency.formatAmount(betAmt)} @ ${market.noOdds}x", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "NO", fontWeight = FontWeight.Black, fontSize = 12.sp, color = ElectricCrimson)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${market.noOdds}x",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
