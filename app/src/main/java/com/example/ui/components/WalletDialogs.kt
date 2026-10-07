package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
fun CurrencySelectorDialog(
    selectedCurrency: CryptoCurrency,
    balances: Map<CryptoCurrency, Double>,
    onSelectCurrency: (CryptoCurrency) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("currency_selector_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Crypto Wallet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                CryptoCurrency.values().forEach { coin ->
                    val isSelected = coin == selectedCurrency
                    val balance = balances[coin] ?: 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonEmerald.copy(alpha = 0.12f) else CyberSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) NeonEmerald else CyberSurfaceBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectCurrency(coin)
                                onDismiss()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(coin.color.copy(alpha = 0.2f))
                                    .border(1.dp, coin.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = coin.symbol.take(1),
                                    color = coin.color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = coin.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = coin.network,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = coin.formatAmount(balance),
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = coin.toUsdString(balance),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepositDialog(
    initialCurrency: CryptoCurrency,
    onDeposit: (CryptoCurrency, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CyberSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("deposit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Deposit Crypto",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Instant 0-Confirmation Node",
                            fontSize = 12.sp,
                            color = NeonEmerald
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Currency Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CryptoCurrency.values().forEach { coin ->
                        val isSel = coin == selectedCurrency
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) coin.color.copy(alpha = 0.25f) else CyberSurfaceElevated)
                                .border(1.dp, if (isSel) coin.color else CyberSurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedCurrency = coin }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = coin.symbol,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) coin.color else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code Matrix Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            val pixelSize = size.width / 15f
                            // Deterministic QR pattern based on currency symbol
                            val seed = selectedCurrency.symbol.hashCode()
                            for (x in 0 until 15) {
                                for (y in 0 until 15) {
                                    val isCorner = (x < 4 && y < 4) || (x > 10 && y < 4) || (x < 4 && y > 10)
                                    val isInnerCorner = (x in 1..2 && y in 1..2) || (x in 12..13 && y in 1..2) || (x in 1..2 && y in 12..13)
                                    val isDot = ((x * 31 + y * 17 + seed) % 3 == 0)

                                    if (isCorner || isInnerCorner || isDot) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(x * pixelSize, y * pixelSize),
                                            size = Size(pixelSize, pixelSize)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scan to Deposit ${selectedCurrency.symbol}",
                            color = Color(0xFF1E293B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Network & Address
                Text(text = "Deposit Network", fontSize = 12.sp, color = TextMuted)
                Text(
                    text = selectedCurrency.network,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = selectedCurrency.color
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Your Dedicated Deposit Address", fontSize = 12.sp, color = TextMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurfaceElevated)
                        .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedCurrency.defaultAddress,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Deposit Address", selectedCurrency.defaultAddress))
                            Toast.makeText(context, "Address copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonEmerald, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Instant Testnet Faucet Quick Buttons
                Text(
                    text = "Instant Test Faucet (One-Tap Deposit)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGold
                )
                Text(
                    text = "Instantly credit your testnet balance for seamless zero-risk testing:",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickAmount1 = when (selectedCurrency) {
                        CryptoCurrency.BTC -> 0.01
                        CryptoCurrency.ETH -> 0.25
                        CryptoCurrency.SOL -> 2.5
                        CryptoCurrency.USDT -> 100.0
                        CryptoCurrency.DOGE -> 500.0
                    }
                    val quickAmount2 = when (selectedCurrency) {
                        CryptoCurrency.BTC -> 0.05
                        CryptoCurrency.ETH -> 1.0
                        CryptoCurrency.SOL -> 10.0
                        CryptoCurrency.USDT -> 500.0
                        CryptoCurrency.DOGE -> 2000.0
                    }

                    Button(
                        onClick = {
                            onDeposit(selectedCurrency, quickAmount1)
                            Toast.makeText(context, "Credited +${selectedCurrency.formatAmount(quickAmount1)}!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald.copy(alpha = 0.2f), contentColor = NeonEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, NeonEmerald, RoundedCornerShape(10.dp))
                            .testTag("faucet_quick_1")
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+${selectedCurrency.formatAmount(quickAmount1)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onDeposit(selectedCurrency, quickAmount2)
                            Toast.makeText(context, "Credited +${selectedCurrency.formatAmount(quickAmount2)}!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold.copy(alpha = 0.2f), contentColor = NeonGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, NeonGold, RoundedCornerShape(10.dp))
                            .testTag("faucet_quick_2")
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+${selectedCurrency.formatAmount(quickAmount2)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawDialog(
    initialCurrency: CryptoCurrency,
    balances: Map<CryptoCurrency, Double>,
    isBiometricsEnabled: Boolean,
    onWithdraw: (CryptoCurrency, Double, String) -> Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var addressText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val currentBalance = balances[selectedCurrency] ?: 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CyberSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("withdraw_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Instant Withdrawal",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Automated High-Speed Payout",
                            fontSize = 12.sp,
                            color = NeonEmerald
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Currency selector tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CryptoCurrency.values().forEach { coin ->
                        val isSel = coin == selectedCurrency
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) coin.color.copy(alpha = 0.25f) else CyberSurfaceElevated)
                                .border(1.dp, if (isSel) coin.color else CyberSurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedCurrency = coin }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = coin.symbol,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) coin.color else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Available Balance Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Available to Withdraw:", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = selectedCurrency.formatAmount(currentBalance),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Destination Address Input
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Destination ${selectedCurrency.symbol} Address") },
                    placeholder = { Text("e.g. 0x... / bc1q... / sol...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = CyberSurfaceBorder,
                        focusedLabelColor = NeonEmerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_address_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${selectedCurrency.symbol})") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    trailingIcon = {
                        Row(modifier = Modifier.padding(end = 8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberSurfaceBorder)
                                    .clickable {
                                        amountText = String.format(Locale.US, "%.${selectedCurrency.decimals}f", currentBalance)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "MAX", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = CyberSurfaceBorder,
                        focusedLabelColor = NeonEmerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fee info
                val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                val gasFee = parsedAmount * 0.005
                val receiveAmount = (parsedAmount - gasFee).coerceAtLeast(0.0)

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Network Gas Fee (0.5%)", fontSize = 11.sp, color = TextMuted)
                            Text(text = selectedCurrency.formatAmount(gasFee), fontSize = 11.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "You Will Receive", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = selectedCurrency.formatAmount(receiveAmount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security badge
                if (isBiometricsEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Protected by Biometric Security Enclave", fontSize = 11.sp, color = NeonEmerald)
                    }
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amount > currentBalance) {
                            Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val destAddress = if (addressText.isBlank()) selectedCurrency.defaultAddress else addressText.trim()
                        val success = onWithdraw(selectedCurrency, amount, destAddress)
                        if (success) {
                            Toast.makeText(context, "Withdrawal dispatched instantly!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Withdrawal failed: insufficient balance including fee", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonEmerald,
                        contentColor = Color(0xFF00210B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_withdraw_button")
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Authorize & Withdraw", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
