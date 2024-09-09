package com.example.cardgame.presentation.component

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cardgame.R
import com.example.cardgame.data.repository.FakePokemonRepository

@Composable
fun CardItem(pokemonCard: PokemonCard, allCardsRevealed: Boolean ,onClick: (index: Int) -> Unit) {
//    Log.d("Test","cardItem")

    val rotation = animateFloatAsState(
        targetValue = if (pokemonCard.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), label = ""
    )
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .graphicsLayer(
                rotationY = rotation.value,
                cameraDistance = 12f * LocalDensity.current.density
            )
            .clickable(onClick = { if (!allCardsRevealed) onClick(pokemonCard.id) })
    ) {
        // 카드 뒷면
        if (rotation.value <= 90f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(5.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
            )
        } else {
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
    val isPreview = LocalInspectionMode.current
    var dominantColor by remember { mutableStateOf(Color.LightGray) }

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
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        if (isPreview) {
            // Preview 모드일 때 고정 이미지 표시
            Image(
                painter = painterResource(id = R.drawable.pokemon_preview),
                contentDescription = "Preview Pokemon",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // 실제 앱에서 AsyncImage 사용
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
                    onLoading = { },
                    onSuccess = { },
                    onError = { },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardItemPreview() {
    val dummyPokemonCard = PokemonCard(
        id = 1,
        name = "Pikachu",
        imageResId = "",
        isFlipped = false,
        isMatched = false
    )
    CardItem(
        pokemonCard = dummyPokemonCard,
        true,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PokemonItemPreview() {
    val dummyPokemonCard = PokemonCard(
        id = 1,
        name = "Pikachu",
        imageResId = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
        isFlipped = false,
        isMatched = false
    )
    PokemonItem(
        pokemon = dummyPokemonCard,
        modifier = Modifier.size(200.dp),
        viewModel = CardMainScreenViewModel(FakePokemonRepository())
    )
}
