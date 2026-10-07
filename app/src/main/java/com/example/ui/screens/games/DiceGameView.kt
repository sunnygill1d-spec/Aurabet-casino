package com.example.ui.screens.games

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import kotlin.math.roundToInt

@Composable
fun DiceGameView(
    betAmount: Double,
    target: Double,
    isRollUnder: Boolean,
    isRolling: Boolean,
    lastRoll: Double?,
    lastWon: Boolean?,
    selectedCurrency: CryptoCurrency,
    balance: Double,
    onSetBetAmount: (Double) -> Unit,
    onSetTarget: (Double) -> Unit,
    onToggleMode: () -> Unit,
    onRollDice: () -> Unit
) {
    val winChance = if (isRollUnder) target else (100.0 - target)
    val multiplier = ((99.0 / winChance) * 100).roundToInt() / 100.0
    val potentialProfit = (betAmount * multiplier) - betAmount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("dice_game_view")
    ) {
        // Roll Outcome Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRolling) "ROLLING PROVABLY FAIR SEED..." else "OUTCOME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isRolling) "--.--" else String.format(Locale.US, "%.2f", lastRoll ?: 50.00),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        isRolling -> NeonGold
                        lastWon == true -> NeonEmerald
                        lastWon == false -> ElectricCrimson
                        else -> TextPrimary
                    },
                    fontFamily = FontFamily.Monospace
                )

                if (!isRolling && lastWon != null) {
                    Text(
                        text = if (lastWon) "WON +${selectedCurrency.formatAmount(betAmount * multiplier)} (${multiplier}x)" else "LOST ROLL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (lastWon) NeonEmerald else ElectricCrimson
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Slider & Stats
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Multiplier", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = String.format(Locale.US, "%.4fx", multiplier),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonEmerald,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Mode Toggle (Roll Under vs Roll Over)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceBorder)
                            .clickable { onToggleMode() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isRollUnder) "Roll Under ▾" else "Roll Over ▴",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonGold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Win Chance", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = String.format(Locale.US, "%.2f%%", winChance),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Slider
                Slider(
                    value = target.toFloat(),
                    onValueChange = { onSetTarget(it.toDouble()) },
                    valueRange = 2f..98f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonEmerald,
                        activeTrackColor = NeonEmerald,
                        inactiveTrackColor = CyberSurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0", fontSize = 10.sp, color = TextMuted)
                    Text(text = String.format(Locale.US, "Target: %.1f", target), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                    Text(text = "100", fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Roll Action Button
        Button(
            onClick = onRollDice,
            enabled = !isRolling && balance >= betAmount && betAmount > 0.0,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonEmerald,
                contentColor = Color(0xFF00210B)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("roll_dice_button")
        ) {
            Icon(imageVector = Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRolling) "ROLLING..." else "ROLL ${selectedCurrency.formatAmount(betAmount)}",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                enabled = !isRolling,
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
                            .clickable(enabled = !isRolling) { onSetBetAmount(value) }
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
