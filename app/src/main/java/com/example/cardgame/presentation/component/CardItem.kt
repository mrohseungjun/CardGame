package com.example.cardgame.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CardItem(pokemonCard: PokemonCard, onClick: () -> Unit) {
    val rotation = animateFloatAsState(
        targetValue = if (pokemonCard.isFlipped) 180f else 0f,
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
            // 카드 앞면 (포켓몬 이미지)
            PokemonItem(pokemon = pokemonCard, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun PokemonItem(
    pokemon: PokemonCard,
    modifier: Modifier,
    viewModel: CardMainScreenViewModel = hiltViewModel()
) {
    val defaultDominantColor = MaterialTheme.colorScheme.surface
    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor,
                        defaultDominantColor
                    )
                )
            )
    ) {
        Column {
            var isLoading by remember { mutableStateOf(true) }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pokemon.imageResId)
                    .listener(
                        onSuccess = { _, result ->
                            val drawable = result.drawable
                            viewModel.calcDominantColor(drawable) { color ->
                                dominantColor = color
                            }
                        }
                    )
                    .build(),
                contentDescription = "", // 적절한 설명을 추가하세요
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                onLoading = { isLoading = true },
                onSuccess = { isLoading = false },
                onError = { isLoading = false }
            )
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(0.5f)
                )
            }
        }
    }
}