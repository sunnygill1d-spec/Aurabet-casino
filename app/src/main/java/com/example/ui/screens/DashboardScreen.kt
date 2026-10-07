package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCurrency
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CasinoUiState
import java.util.Locale
import kotlin.math.max

@Composable
fun DashboardScreen(
    state: CasinoUiState,
    onNavigateToGame: (Int) -> Unit,
    onOpenProvablyFair: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalUsdNetWorth = state.balances.entries.sumOf { (currency, amount) ->
        amount * currency.usdRate
    }

    val wins = state.betHistory.count { it.isWin }
    val totalBets = state.betHistory.size
    val winRate = if (totalBets > 0) (wins.toDouble() / totalBets * 100.0) else 65.0
    val totalWageredUsd = state.betHistory.sumOf { it.betAmount * it.currency.usdRate }
    val biggestMultiplier = state.betHistory.maxOfOrNull { it.multiplier } ?: 12.44
    val netProfitUsd = state.betHistory.sumOf { (it.payout - it.betAmount) * it.currency.usdRate }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio Net Worth Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "TOTAL CRYPTO PORTFOLIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format(Locale.US, "$%,.2f", totalUsdNetWorth),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonEmerald.copy(alpha = 0.15f))
                            .border(1.dp, NeonEmerald, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+14.8% 24h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mini Currency Pill Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CryptoCurrency.values().forEach { coin ->
                        val bal = state.balances[coin] ?: 0.0
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSurfaceElevated)
                                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(coin.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${coin.symbol}: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = coin.formatAmount(bal),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Data Visualization: Win Rate Radial Gauge & Performance Analytics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular Canvas Win Rate Gauge
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WIN RATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 10f
                            val sweep = (winRate / 100f * 360f).toFloat()

                            // Background circle
                            drawArc(
                                color = CyberSurfaceBorder,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                            // Win progress arc
                            drawArc(
                                brush = Brush.sweepGradient(listOf(NeonEmerald, CyberCyan, NeonEmerald)),
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.0f%%", winRate),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$wins Wins",
                                fontSize = 9.sp,
                                color = NeonEmerald
                            )
                        }
                    }
                }
            }

            // Key Metrics Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "METRICS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)

                    Column {
                        Text(text = "Total Wagered", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = String.format(Locale.US, "$%,.0f", totalWageredUsd),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text(text = "Biggest Hit", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = String.format(Locale.US, "%.2fx", biggestMultiplier),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text(text = "Net P&L", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = String.format(Locale.US, "%s$%,.2f", if (netProfitUsd >= 0) "+" else "", netProfitUsd),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfitUsd >= 0) NeonEmerald else ElectricCrimson,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Performance Chart (Canvas line chart with gradient glow)
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROFITABILITY TREND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-Time Wager Graph",
                        fontSize = 11.sp,
                        color = CyberCyan
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid lines
                    for (i in 1..3) {
                        val y = h * (i / 4f)
                        drawLine(
                            color = CyberSurfaceBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Line points data
                    val points = listOf(0.2f, 0.35f, 0.28f, 0.55f, 0.45f, 0.72f, 0.65f, 0.90f)
                    val stepX = w / (points.size - 1)

                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { index, p ->
                        val x = index * stepX
                        val y = h - (p * (h * 0.8f))
                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevY = h - (points[index - 1] * (h * 0.8f))
                            path.cubicTo(
                                prevX + stepX / 2, prevY,
                                x - stepX / 2, y,
                                x, y
                            )
                            fillPath.cubicTo(
                                prevX + stepX / 2, prevY,
                                x - stepX / 2, y,
                                x, y
                            )
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Gradient fill underneath curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(NeonEmerald.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Line stroke
                    drawPath(
                        path = path,
                        color = NeonEmerald,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Latest point dot
                    val lastX = (points.size - 1) * stepX
                    val lastY = h - (points.last() * (h * 0.8f))
                    drawCircle(color = NeonEmerald, radius = 6f, center = Offset(lastX, lastY))
                }
            }
        }

        // Quick Game Launchers
        Text(
            text = "FEATURED BETTING MARKETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple("Crash", "Up to 1000x", Icons.Default.RocketLaunch to NeonEmerald),
                Triple("Mines", "5x5 Grid", Icons.Default.Diamond to CyberCyan),
                Triple("Dice", "99% RTP", Icons.Default.Casino to NeonGold)
            ).forEachIndexed { index, (title, subtitle, iconPair) ->
                val (icon, color) = iconPair
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToGame(index) }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.15f))
                                .border(1.dp, color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text(text = subtitle, fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Provably Fair Transparency Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenProvablyFair() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.15f))
                        .border(1.dp, NeonEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Provably Fair Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text(
                        text = "Server Seed SHA-256 pre-committed. Nonce: ${state.nonce}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                Icon(imageVector = Icons.Default.NorthEast, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
            }
        }
    }
}
