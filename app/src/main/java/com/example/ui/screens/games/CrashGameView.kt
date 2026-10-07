package com.example.ui.screens.games

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.viewmodel.CrashGameState
import com.example.viewmodel.CrashPlayerLive
import java.util.Locale
import kotlin.math.min

@Composable
fun CrashGameView(
    crashState: CrashGameState,
    betAmount: Double,
    autoCashout: Double?,
    isBetActive: Boolean,
    hasCashedOut: Boolean,
    cashoutMultiplier: Double?,
    crashHistory: List<Double>,
    livePlayers: List<CrashPlayerLive>,
    selectedCurrency: CryptoCurrency,
    balance: Double,
    onSetBetAmount: (Double) -> Unit,
    onSetAutoCashout: (Double?) -> Unit,
    onPlaceBet: () -> Unit,
    onCashout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("crash_game_view")
    ) {
        // Multiplier History Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            crashHistory.forEach { mult ->
                val isHigh = mult >= 2.00
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isHigh) NeonEmerald.copy(alpha = 0.18f) else CyberSurfaceBorder.copy(alpha = 0.6f))
                        .border(1.dp, if (isHigh) NeonEmerald.copy(alpha = 0.6f) else CyberSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2fx", mult),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHigh) NeonEmerald else TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas Crash Stage Area
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Background grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid lines
                    for (i in 1..4) {
                        val y = h * (i / 5f)
                        drawLine(
                            color = CyberSurfaceBorder.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw curve when running or crashed
                    val currentMult = when (crashState) {
                        is CrashGameState.Running -> crashState.currentMultiplier
                        is CrashGameState.Crashed -> crashState.finalMultiplier
                        else -> 1.0
                    }

                    if (crashState is CrashGameState.Running || crashState is CrashGameState.Crashed) {
                        val progress = min(1.0f, ((currentMult - 1.0) / 10.0).toFloat())
                        val endX = w * (0.15f + progress * 0.8f)
                        val endY = h * (0.85f - progress * 0.75f)

                        val path = Path().apply {
                            moveTo(w * 0.1f, h * 0.85f)
                            quadraticTo(
                                w * 0.3f, h * 0.85f,
                                endX, endY
                            )
                        }

                        val strokeColor = if (crashState is CrashGameState.Crashed) ElectricCrimson else NeonEmerald
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )

                        // Rocket tip point
                        drawCircle(
                            color = strokeColor,
                            radius = 10f,
                            center = Offset(endX, endY)
                        )
                    }
                }

                // Central Status Text
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (crashState) {
                        is CrashGameState.Countdown -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NEXT ROUND IN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${crashState.secondsLeft}s",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Place your bets now",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        is CrashGameState.Running -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.US, "%.2fx", crashState.currentMultiplier),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonEmerald,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isBetActive && !hasCashedOut) {
                                    val currentWin = betAmount * crashState.currentMultiplier
                                    Text(
                                        text = "Current Value: ${selectedCurrency.formatAmount(currentWin)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                        is CrashGameState.Crashed -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CRASHED",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricCrimson,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = String.format(Locale.US, "@ %.2fx", crashState.finalMultiplier),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricCrimson,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        CrashGameState.Idle -> {
                            Text(
                                text = "Preparing Launch...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Action Button (Bet or Cashout)
        when {
            // Player is in flight and hasn't cashed out yet!
            isBetActive && !hasCashedOut && crashState is CrashGameState.Running -> {
                val winAmount = betAmount * crashState.currentMultiplier
                Button(
                    onClick = onCashout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGold,
                        contentColor = Color(0xFF3B2800)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("crash_cashout_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CASHOUT NOW",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "+${selectedCurrency.formatAmount(winAmount)} (${String.format(Locale.US, "%.2fx", crashState.currentMultiplier)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            // Bet is placed and waiting for takeoff
            isBetActive && crashState is CrashGameState.Countdown -> {
                Button(
                    onClick = { /* already active */ },
                    enabled = false,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Bet Placed (${selectedCurrency.formatAmount(betAmount)}) - Launching...", fontWeight = FontWeight.Bold)
                }
            }
            // Cashed out successfully in this round
            hasCashedOut -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NeonEmerald.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "CASHED OUT @ ${String.format(Locale.US, "%.2fx", cashoutMultiplier ?: 1.0)}",
                            fontWeight = FontWeight.Black,
                            color = NeonEmerald,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            // Can place bet for next round
            else -> {
                Button(
                    onClick = onPlaceBet,
                    enabled = balance >= betAmount && betAmount > 0.0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonEmerald,
                        contentColor = Color(0xFF00210B)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("crash_place_bet_button")
                ) {
                    Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BET ${selectedCurrency.formatAmount(betAmount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bet Amount Controls & Quick Multipliers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = if (betAmount == 0.0) "" else betAmount.toString(),
                onValueChange = { str ->
                    val parsed = str.toDoubleOrNull() ?: 0.0
                    onSetBetAmount(parsed)
                },
                label = { Text("Bet (${selectedCurrency.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonEmerald,
                    unfocusedBorderColor = CyberSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("crash_bet_input")
            )

            // Quick 1/2, 2x, Max pills
            Row(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "½" to (betAmount / 2.0).coerceAtLeast(selectedCurrency.minBet),
                    "2x" to (betAmount * 2.0).coerceAtMost(balance),
                    "MAX" to balance
                ).forEach { (label, value) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceElevated)
                            .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { onSetBetAmount(value) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (label == "MAX") NeonGold else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Auto Cashout setting
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Auto Cashout Multiplier", fontSize = 12.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1.5, 2.0, 5.0, 10.0).forEach { mult ->
                    val isSel = autoCashout == mult
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) NeonEmerald.copy(alpha = 0.2f) else CyberSurfaceElevated)
                            .border(1.dp, if (isSel) NeonEmerald else CyberSurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                onSetAutoCashout(if (isSel) null else mult)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${mult}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) NeonEmerald else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Players Table
        Text(
            text = "Active Players in Round (${livePlayers.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurface)
                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            livePlayers.forEach { player ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = player.name,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.currency.formatAmount(player.betAmount),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (player.cashoutMultiplier != null) {
                            Text(
                                text = String.format(Locale.US, "%.2fx", player.cashoutMultiplier),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )
                        } else {
                            Text(
                                text = "In Flight",
                                fontSize = 10.sp,
                                color = NeonGold
                            )
                        }
                    }
                }
            }
        }
    }
}
