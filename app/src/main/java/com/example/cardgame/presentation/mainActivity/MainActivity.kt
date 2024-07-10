package com.example.cardgame.presentation.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cardgame.R
import com.example.cardgame.ui.theme.CardGameTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Card(val id: Int, val imageResId: Int, var isFlipped: Boolean = false, var isMatched: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardGameTheme {
                MatchingGame()
            }
        }
    }
}

fun createCards(stage: Int): List<Card> {
    val imageResources = listOf(
        R.drawable.ic_launcher_background, R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background, R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background, R.drawable.ic_launcher_background, R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background, R.drawable.ic_launcher_background, R.drawable.ic_launcher_background
        // 필요에 따라 더 많은 이미지 추가
    )

    // 스테이지에 따라 사용할 이미지 수 결정 (최소 12개, 최대 24개)
    val pairsCount = minOf(12 + stage - 1, 12)

    return imageResources.take(pairsCount)
        .flatMap { resId ->
            listOf(
                Card(id = resId * 2, imageResId = resId),
                Card(id = resId * 2 + 1, imageResId = resId)
            )
        }.shuffled()
}

@Composable
fun MatchingGame() {
    var stage by remember { mutableStateOf(1) }
    var timeLeft by remember { mutableStateOf(60) }
    var cards by remember { mutableStateOf(createCards(stage)) }
    var isGameOver by remember { mutableStateOf(false) }
    var firstSelectedCard by remember { mutableStateOf<Card?>(null) }

    LaunchedEffect(stage) {
        while (timeLeft > 0 && !isGameOver) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) {
            isGameOver = true
        }
    }

    Column {
        Text("Stage: $stage", modifier = Modifier.padding(16.dp))
        Text("Time Left: $timeLeft", modifier = Modifier.padding(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            content = {
                items(cards) { card ->
                    CardItem(
                        card = card,
                        onClick = {
                            if (!isGameOver) {
                                handleCardClick(card, cards, firstSelectedCard) { updatedCards, updatedFirstCard ->
                                    cards = updatedCards
                                    firstSelectedCard = updatedFirstCard
                                    if (cards.all { it.isMatched }) {
                                        stage++
                                        timeLeft = maxOf(60 - (stage - 1) * 5, 10)
                                        cards = createCards(stage)
                                        firstSelectedCard = null
                                    }
                                }
                            }
                        }
                    )
                }
            }
        )

        if (isGameOver) {
            Text("Game Over! You reached Stage $stage",
                modifier = Modifier.padding(16.dp))
        }
    }
}


@Composable
fun CardItem(card: Card, onClick: () -> Unit) {
    val rotation = animateFloatAsState(
        targetValue = if (card.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .graphicsLayer(
                rotationY = rotation.value,
                cameraDistance = 12f * LocalDensity.current.density
            )
            .clickable(onClick = onClick)
    ) {
        if (rotation.value <= 90f) {
            // 카드 뒷면
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            )
        } else {
            // 카드 앞면
            Image(
                painter = painterResource(id = card.imageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        rotationY = 180f
                    )
            )
        }
    }
}
fun handleCardClick(
    clickedCard: Card,
    allCards: List<Card>,
    firstSelectedCard: Card?,
    onUpdate: (List<Card>, Card?) -> Unit
) {
    if (clickedCard.isMatched || clickedCard.isFlipped) return

    val updatedCards = allCards.toMutableList()
    val clickedCardIndex = updatedCards.indexOfFirst { it.id == clickedCard.id }

    if (firstSelectedCard == null) {
        // 첫 번째 카드 선택
        clickedCard.isFlipped = true
        updatedCards[clickedCardIndex] = clickedCard
        onUpdate(updatedCards, clickedCard)
    } else {
        // 두 번째 카드 선택
        clickedCard.isFlipped = true
        updatedCards[clickedCardIndex] = clickedCard

        if (firstSelectedCard.imageResId == clickedCard.imageResId) {
            // 짝이 맞는 경우
            val firstCardIndex = updatedCards.indexOfFirst { it.id == firstSelectedCard.id }
            updatedCards[firstCardIndex].isMatched = true
            updatedCards[clickedCardIndex].isMatched = true
            onUpdate(updatedCards, null)
        } else {
            // 짝이 맞지 않는 경우
            onUpdate(updatedCards, null)
            // 잠시 후 카드를 다시 뒤집습니다
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                updatedCards[clickedCardIndex].isFlipped = false
                val firstCardIndex = updatedCards.indexOfFirst { it.id == firstSelectedCard.id }
                updatedCards[firstCardIndex].isFlipped = false
                onUpdate(updatedCards, null)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CardGameTheme {
        MatchingGame()
    }
}