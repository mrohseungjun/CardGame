package com.example.cardgame.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cardgame.ui.theme.CardGameTheme
import kotlinx.coroutines.delay

@Composable
fun CardMainScreen(
    viewModel: CardMainScreenViewModel = hiltViewModel()
) {
    val gameState by remember { viewModel.gameState }
    val isLoading by remember { viewModel.isLoading }
    val loadError by remember { viewModel.loadError }

    LaunchedEffect(gameState.stage) {
        while (gameState.timeLeft > 0 && !gameState.isGameOver) {
            delay(1000)
            viewModel.decreaseTime()
        }
        if (gameState.timeLeft == 0) {
            viewModel.setGameOver()
        }
    }


    MatchingGame(gameState = gameState, isLoading = isLoading, loadError = loadError) {
        viewModel.handleCardClick(it)
    }
}

@Composable
fun MatchingGame(gameState: GameState, isLoading: Boolean, loadError: String, onCardClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(10.dp))
            GameHeader(gameState.stage, gameState.timeLeft)

            when {
                isLoading -> LoadingIndicator()
                loadError.isNotEmpty() -> ErrorMessage(loadError)
                gameState.isGameOver -> GameOverMessage(gameState.stage)
                else -> GameContent(gameState) { id ->
                    onCardClick(id)
                }
            }
        }
    }
}

@Composable
fun GameHeader(stage: Int, timeLeft: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Stage: $stage",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            "Time: $timeLeft s",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorMessage(error: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Error: $error",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun GameOverMessage(stage: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Game Over!",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You reached Stage $stage",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun GameContent(gameState: GameState, onCardClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(8.dp),
        content = {
            items(gameState.cards) { card ->
                CardItem(
                    pokemonCard = card,
                    onClick = { id -> if (!gameState.isGameOver) onCardClick(id) },
                )
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CardGameTheme {

    }
}
