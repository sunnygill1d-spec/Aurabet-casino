package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BetRecord
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.components.CurrencySelectorDialog
import com.example.ui.components.DepositDialog
import com.example.ui.components.LiveTickerBar
import com.example.ui.components.NotificationBanner
import com.example.ui.components.ProvablyFairVerifyDialog
import com.example.ui.components.TopCasinoBar
import com.example.ui.components.WithdrawDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MarketsPagerScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.CasinoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val casinoViewModel: CasinoViewModel = viewModel()
                CasinoAppRoot(viewModel = casinoViewModel)
            }
        }
    }
}

enum class CasinoNavTab(val title: String) {
    DASHBOARD("Home"),
    MARKETS("Markets"),
    HISTORY("History"),
    REFERRAL("Affiliate"),
    SECURITY("Security"),
    SUPPORT("Support")
}

@Composable
fun CasinoAppRoot(viewModel: CasinoViewModel) {
    val state by viewModel.uiState.collectAsState()

    var currentTab by remember { mutableStateOf(CasinoNavTab.DASHBOARD) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var verifyBetRecord by remember { mutableStateOf<BetRecord?>(null) }

    // Android back gesture handling
    BackHandler(enabled = currentTab != CasinoNavTab.DASHBOARD) {
        currentTab = CasinoNavTab.DASHBOARD
    }

    val currentBalance = state.balances[state.selectedCurrency] ?: 0.0

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBackground),
            containerColor = CyberBackground,
            topBar = {
                Column {
                    TopCasinoBar(
                        selectedCurrency = state.selectedCurrency,
                        balance = currentBalance,
                        onCurrencyClick = { showCurrencyDialog = true },
                        onDepositClick = { showDepositDialog = true },
                        onWithdrawClick = { showWithdrawDialog = true },
                        onSupportClick = { currentTab = CasinoNavTab.SUPPORT },
                        onLockClick = { viewModel.lockApp() },
                        isBiometricsEnabled = state.isBiometricsEnabled
                    )
                    LiveTickerBar(liveFeed = state.liveFeed)
                    NotificationBanner(
                        bannerText = state.activeNotificationBanner,
                        onDismiss = viewModel::dismissNotificationBanner
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CyberSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("main_navigation_bar")
                ) {
                    val navItems = listOf(
                        CasinoNavTab.DASHBOARD to Icons.Default.Insights,
                        CasinoNavTab.MARKETS to Icons.Default.Casino,
                        CasinoNavTab.HISTORY to Icons.Default.History,
                        CasinoNavTab.REFERRAL to Icons.Default.GroupAdd,
                        CasinoNavTab.SECURITY to Icons.Default.Security
                    )

                    navItems.forEach { (tab, icon) ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) NeonEmerald else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonEmerald else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonEmerald.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    CasinoNavTab.DASHBOARD -> DashboardScreen(
                        state = state,
                        onNavigateToGame = { gameIndex ->
                            currentTab = CasinoNavTab.MARKETS
                        },
                        onOpenProvablyFair = {
                            currentTab = CasinoNavTab.SECURITY
                        }
                    )
                    CasinoNavTab.MARKETS -> MarketsPagerScreen(
                        state = state,
                        viewModel = viewModel
                    )
                    CasinoNavTab.HISTORY -> HistoryScreen(
                        betHistory = state.betHistory,
                        onVerifyBet = { record ->
                            verifyBetRecord = record
                        }
                    )
                    CasinoNavTab.REFERRAL -> ReferralScreen(
                        referralData = state.referralData,
                        leaderboard = state.referralLeaderboard,
                        onClaimRewards = viewModel::claimReferralRewards
                    )
                    CasinoNavTab.SECURITY -> SecurityScreen(
                        state = state,
                        viewModel = viewModel
                    )
                    CasinoNavTab.SUPPORT -> SupportScreen(
                        messages = state.supportMessages,
                        tickets = state.supportTickets,
                        onSendMessage = viewModel::sendSupportMessage,
                        onCreateTicket = viewModel::createSupportTicket
                    )
                }
            }
        }

        // Biometric Security Lock Overlay (if app is locked)
        if (state.isAppLocked) {
            BiometricLockOverlay(
                onUnlockWithBiometrics = { viewModel.unlockAppWithBiometrics() },
                onUnlockWithPin = { pin -> viewModel.unlockAppWithPin(pin) }
            )
        }

        // Wallet & Provably Fair Modals
        if (showDepositDialog) {
            DepositDialog(
                initialCurrency = state.selectedCurrency,
                onDeposit = viewModel::depositFunds,
                onDismiss = { showDepositDialog = false }
            )
        }

        if (showWithdrawDialog) {
            WithdrawDialog(
                initialCurrency = state.selectedCurrency,
                balances = state.balances,
                isBiometricsEnabled = state.isBiometricsEnabled,
                onWithdraw = viewModel::withdrawFunds,
                onDismiss = { showWithdrawDialog = false }
            )
        }

        if (showCurrencyDialog) {
            CurrencySelectorDialog(
                selectedCurrency = state.selectedCurrency,
                balances = state.balances,
                onSelectCurrency = viewModel::selectCurrency,
                onDismiss = { showCurrencyDialog = false }
            )
        }

        if (verifyBetRecord != null) {
            ProvablyFairVerifyDialog(
                record = verifyBetRecord,
                onDismiss = { verifyBetRecord = null }
            )
        }
    }
}
