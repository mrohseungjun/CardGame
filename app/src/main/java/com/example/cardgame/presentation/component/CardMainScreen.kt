package com.example.cardgame.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cardgame.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CardMainScreen(
    viewModel: CardMainScreenViewModel = hiltViewModel()
) {
    MatchingGame(viewModel)
}

@Composable
fun MatchingGame(viewModel: CardMainScreenViewModel) {

    val gameState by remember { viewModel.gameState }
    val isLoading by remember { viewModel.isLoading }
    val loadError by remember { viewModel.loadError }

    LaunchedEffect(Unit) {
        viewModel.loadPokemonAndCreateCards(1)
    }

    LaunchedEffect(gameState.stage) {
        while (gameState.timeLeft > 0 && !gameState.isGameOver) {
            delay(1000)
            viewModel.decreaseTime()
        }
        if (gameState.timeLeft == 0) {
            viewModel.setGameOver()
        }
    }

    Column {
        Text("Stage: ${gameState.stage}", modifier = Modifier.padding(16.dp))
        Text("Time Left: ${gameState.timeLeft}", modifier = Modifier.padding(16.dp))

        if (isLoading) {
            // 로딩 인디케이터 표시
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (loadError.isNotEmpty()) {
            // 에러 메시지 표시
            Text(
                text = "error",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                content = {
                    items(gameState.cards) { card ->
                        CardItem(
                            pokemonCard = card,
                            onClick = {
                                if (!gameState.isGameOver) {
                                    viewModel.handleCardClick(card)
                                }
                            }
                        )
                    }
                }
            )
        }

        if (gameState.isGameOver) {
            Text(
                "Game Over! You reached Stage ${gameState.stage}",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
