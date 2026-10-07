package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import java.util.Locale

@Composable
fun MinesGameView(
    minesCount: Int,
    betAmount: Double,
    isActive: Boolean,
    revealedIndices: Set<Int>,
    minePositions: Set<Int>,
    isExploded: Boolean,
    explodedIndex: Int?,
    currentMultiplier: Double,
    selectedCurrency: CryptoCurrency,
    balance: Double,
    onSetMinesCount: (Int) -> Unit,
    onSetBetAmount: (Double) -> Unit,
    onStartGame: () -> Unit,
    onPickTile: (Int) -> Unit,
    onCashout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("mines_game_view")
    ) {
        // Multiplier & Status Header
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CURRENT MULTIPLIER", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isExploded) "0.00x" else String.format(Locale.US, "%.2fx", currentMultiplier),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isExploded) ElectricCrimson else if (revealedIndices.isNotEmpty()) NeonEmerald else TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "GEMS FOUND", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${revealedIndices.size} / ${25 - minesCount}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5x5 Mines Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSurface)
                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 5) {
                        val index = row * 5 + col
                        val isRevealed = revealedIndices.contains(index)
                        val isMine = minePositions.contains(index)
                        val isThisExploded = isExploded && index == explodedIndex
                        val showAsMine = isExploded && isMine

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isThisExploded -> ElectricCrimson
                                        showAsMine -> ElectricCrimson.copy(alpha = 0.4f)
                                        isRevealed -> NeonEmerald.copy(alpha = 0.25f)
                                        else -> CyberSurfaceElevated
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isThisExploded -> ElectricCrimson
                                        isRevealed -> NeonEmerald
                                        else -> CyberSurfaceBorder
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = isActive && !isRevealed && !isExploded) {
                                    onPickTile(index)
                                }
                                .testTag("mines_tile_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                showAsMine || isThisExploded -> {
                                    Text(text = "💣", fontSize = 18.sp)
                                }
                                isRevealed -> {
                                    Text(text = "💎", fontSize = 18.sp)
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(CyberSurfaceBorder)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Button
        if (isActive && !isExploded && revealedIndices.isNotEmpty()) {
            val winAmount = betAmount * currentMultiplier
            Button(
                onClick = onCashout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGold,
                    contentColor = Color(0xFF3B2800)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("mines_cashout_button")
            ) {
                Text(
                    text = "CASHOUT +${selectedCurrency.formatAmount(winAmount)} (${String.format(Locale.US, "%.2fx", currentMultiplier)})",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        } else {
            Button(
                onClick = onStartGame,
                enabled = balance >= betAmount && betAmount > 0.0 && !isActive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonEmerald,
                    contentColor = Color(0xFF00210B)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("mines_start_button")
            ) {
                Text(
                    text = if (isExploded) "PLAY AGAIN (${selectedCurrency.formatAmount(betAmount)})" else "START ROUND (${selectedCurrency.formatAmount(betAmount)})",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mine Count Selector (1, 3, 5, 10, 24)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Select Number of Mines", fontSize = 12.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1, 3, 5, 10, 24).forEach { count ->
                    val isSel = minesCount == count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) NeonEmerald.copy(alpha = 0.2f) else CyberSurfaceElevated)
                            .border(1.dp, if (isSel) NeonEmerald else CyberSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isActive) { onSetMinesCount(count) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$count",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSel) NeonEmerald else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bet Amount Controls
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
                enabled = !isActive,
                label = { Text("Bet (${selectedCurrency.symbol})") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonEmerald,
                    unfocusedBorderColor = CyberSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.weight(1.2f)
            )

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
                            .clickable(enabled = !isActive) { onSetBetAmount(value) }
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
    }
}
