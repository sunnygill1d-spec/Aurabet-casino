package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BiometricLockOverlay(
    onUnlockWithBiometrics: () -> Unit,
    onUnlockWithPin: (String) -> Boolean
) {
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("biometric_lock_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Shield & Lock Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceElevated)
                    .border(2.dp, NeonEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AuraBet Secure Enclave",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextPrimary
            )
            Text(
                text = "Biometric Authentication Required",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Pulsing Biometric Sensor Button
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(NeonEmerald.copy(alpha = 0.15f))
                    .border(2.dp, NeonEmerald.copy(alpha = 0.6f), CircleShape)
                    .clickable { onUnlockWithBiometrics() }
                    .testTag("biometric_sensor_tap"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Scan Fingerprint to Unlock",
                    tint = NeonEmerald,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tap sensor for Instant Biometric Unlock",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonEmerald
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Display
            Text(
                text = if (pinError) "Incorrect PIN. Try again." else "Or enter 4-digit PIN (default 7788)",
                fontSize = 12.sp,
                color = if (pinError) ElectricCrimson else TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                for (i in 0 until 4) {
                    val isFilled = i < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (pinError) ElectricCrimson
                                else if (isFilled) NeonEmerald
                                else CyberSurfaceBorder
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Numeric Keypad
            Column(
                modifier = Modifier.width(260.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                for (row in keypadRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(CyberSurfaceElevated)
                                    .border(1.dp, CyberSurfaceBorder, CircleShape)
                                    .clickable {
                                        pinError = false
                                        when (key) {
                                            "C" -> pinInput = ""
                                            "DEL" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                            else -> {
                                                if (pinInput.length < 4) {
                                                    val newPin = pinInput + key
                                                    pinInput = newPin
                                                    if (newPin.length == 4) {
                                                        val unlocked = onUnlockWithPin(newPin)
                                                        if (!unlocked) {
                                                            pinError = true
                                                            pinInput = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "DEL") {
                                    Icon(imageVector = Icons.Default.Backspace, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                } else {
                                    Text(text = key, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
