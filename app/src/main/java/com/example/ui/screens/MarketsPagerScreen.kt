package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.games.CrashGameView
import com.example.ui.screens.games.DiceGameView
import com.example.ui.screens.games.MinesGameView
import com.example.ui.screens.games.PredictionMarketsView
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.CasinoUiState
import com.example.viewmodel.CasinoViewModel
import kotlinx.coroutines.launch

@Composable
fun MarketsPagerScreen(
    state: CasinoUiState,
    viewModel: CasinoViewModel,
    modifier: Modifier = Modifier
) {
    val tabTitles = listOf("Crash", "Mines", "Crypto Dice", "Live Markets")
    val tabIcons = listOf(
        Icons.Default.RocketLaunch,
        Icons.Default.Diamond,
        Icons.Default.Casino,
        Icons.Default.TrendingUp
    )
    val pagerState = rememberPagerState(initialPage = 0) { tabTitles.size }
    val coroutineScope = rememberCoroutineScope()
    val currentBalance = state.balances[state.selectedCurrency] ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("markets_pager_screen")
    ) {
        // Tab row with gestures
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = CyberSurface,
            contentColor = NeonEmerald,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = NeonEmerald,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) NeonEmerald else TextMuted
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tabIcons[index],
                            contentDescription = title,
                            tint = if (isSelected) NeonEmerald else TextMuted
                        )
                    },
                    modifier = Modifier.testTag("tab_$title")
                )
            }
        }

        // Horizontal Pager with effortless swipe gestures between active markets
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                when (page) {
                    0 -> CrashGameView(
                        crashState = state.crashState,
                        betAmount = state.crashBetAmount,
                        autoCashout = state.crashAutoCashout,
                        isBetActive = state.isCrashBetActive,
                        hasCashedOut = state.crashCashedOut,
                        cashoutMultiplier = state.crashCashoutMultiplier,
                        crashHistory = state.crashHistory,
                        livePlayers = state.crashLivePlayers,
                        selectedCurrency = state.selectedCurrency,
                        balance = currentBalance,
                        onSetBetAmount = viewModel::setCrashBetAmount,
                        onSetAutoCashout = viewModel::setCrashAutoCashout,
                        onPlaceBet = viewModel::placeCrashBet,
                        onCashout = viewModel::cashoutCrash
                    )
                    1 -> MinesGameView(
                        minesCount = state.minesCount,
                        betAmount = state.minesBetAmount,
                        isActive = state.isMinesActive,
                        revealedIndices = state.minesRevealed,
                        minePositions = state.minePositions,
                        isExploded = state.isMinesExploded,
                        explodedIndex = state.explodedMineIndex,
                        currentMultiplier = state.currentMinesMultiplier,
                        selectedCurrency = state.selectedCurrency,
                        balance = currentBalance,
                        onSetMinesCount = viewModel::setMinesCount,
                        onSetBetAmount = viewModel::setMinesBetAmount,
                        onStartGame = viewModel::startMinesGame,
                        onPickTile = viewModel::pickMinesTile,
                        onCashout = viewModel::cashoutMines
                    )
                    2 -> DiceGameView(
                        betAmount = state.diceBetAmount,
                        target = state.diceTarget,
                        isRollUnder = state.isDiceRollUnder,
                        isRolling = state.isDiceRolling,
                        lastRoll = state.lastDiceRoll,
                        lastWon = state.lastDiceWon,
                        selectedCurrency = state.selectedCurrency,
                        balance = currentBalance,
                        onSetBetAmount = viewModel::setDiceBetAmount,
                        onSetTarget = viewModel::setDiceTarget,
                        onToggleMode = viewModel::toggleDiceRollMode,
                        onRollDice = viewModel::rollDice
                    )
                    3 -> PredictionMarketsView(
                        markets = state.bettingMarkets,
                        selectedCurrency = state.selectedCurrency,
                        balance = currentBalance,
                        onPlaceBet = viewModel::placeMarketBet
                    )
                }
            }
        }
    }
}
