package com.example.cardgame.presentation.component

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.cardgame.data.repository.PokemonRepository
import com.example.cardgame.presentation.mapper.toPokemon
import com.example.cardgame.presentation.mapper.toPokemonList
import com.example.cardgame.presentation.model.Pokemon
import com.example.cardgame.util.Constants
import com.example.cardgame.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokemonCard(
    val id: Int,
    val imageResId: String,
    var name: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

// GameState data class
data class GameState(
    val cards: List<PokemonCard> = emptyList(),
    val firstSelectedCardId: Int? = null,
    val stage: Int = 1,
    val timeLeft: Int = 60,
    val isGameOver: Boolean = false
)

@HiltViewModel
class CardMainScreenViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    private var curPage = 0
    private var pokemonList: List<Pokemon> = emptyList()

    private val _gameState = mutableStateOf(GameState())
    val gameState: State<GameState> = _gameState

    private val _loadError = mutableStateOf("")
    val loadError: State<String> = _loadError

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _endReached = mutableStateOf(false)
    val endReached: State<Boolean> = _endReached

    init {
        loadPokemonList()
    }

    fun decreaseTime() {
        _gameState.value = _gameState.value.copy(
            timeLeft = _gameState.value.timeLeft - 1
        )
    }

    fun setGameOver() {
        _gameState.value = _gameState.value.copy(
            isGameOver = true
        )
    }

    private fun loadPokemonList() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getPokemonList(150, 0)  // 한 번에 150개의 포켓몬을 가져옵니다.

            when (result) {
                is Resource.Success -> {
                    pokemonList = result.data?.toPokemonList()?.results ?: emptyList()
                    _loadError.value = ""
                    _isLoading.value = false
                    startNewStage(1)  // 초기 스테이지 시작
                }
                is Resource.Error -> {
                    _loadError.value = result.message ?: "An unexpected error occurred"
                    _isLoading.value = false
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    fun startNewStage(stage: Int) {
        val pairsCount = minOf(4 + stage, 12)  // 스테이지당 1쌍씩 증가, 최대 12쌍
        val shuffledPokemon = pokemonList.shuffled().take(pairsCount)
        val cards = shuffledPokemon.flatMapIndexed { index, pokemon ->
            listOf(
                PokemonCard(
                    id = index,
                    name = pokemon.name,
                    imageResId = pokemon.imageUrl
                ),
                PokemonCard(
                    id = index + pairsCount,
                    name = pokemon.name,
                    imageResId = pokemon.imageUrl
                )
            )
        }.shuffled()

        _gameState.value = _gameState.value.copy(
            cards = cards,
            stage = stage,
            timeLeft = maxOf(60 - (stage - 1) * 5, 30),  // 스테이지에 따라 시간 조절, 최소 30초
            isGameOver = false,
            firstSelectedCardId = null
        )
    }

    private fun flipCard(cardId: Int) {
        val updatedCards = _gameState.value.cards.map { card ->
            if (card.id == cardId) card.copy(isFlipped = !card.isFlipped) else card
        }
        _gameState.value = _gameState.value.copy(cards = updatedCards)
    }

    private fun matchCards(card1Id: Int, card2Id: Int) {
        val updatedCards = _gameState.value.cards.map { card ->
            when (card.id) {
                card1Id, card2Id -> card.copy(isMatched = true)
                else -> card
            }
        }
        _gameState.value = _gameState.value.copy(cards = updatedCards)
    }

    fun handleCardClick(clickedCardId: Int) {
        val clickedCard = _gameState.value.cards.find { it.id == clickedCardId } ?: return
        if (clickedCard.isMatched || clickedCard.isFlipped) return

        flipCard(clickedCardId)

        val firstSelectedCardId = _gameState.value.firstSelectedCardId
        if (firstSelectedCardId == null) {
            _gameState.value = _gameState.value.copy(firstSelectedCardId = clickedCardId)
        } else {
            val firstSelectedCard = _gameState.value.cards.find { it.id == firstSelectedCardId }
            if (firstSelectedCard != null) {
                if (firstSelectedCard.imageResId == clickedCard.imageResId) {
                    matchCards(firstSelectedCardId, clickedCardId)
                    checkStageCompletion()
                } else {
                    viewModelScope.launch {
                        delay(1000)
                        flipCard(firstSelectedCardId)
                        flipCard(clickedCardId)
                    }
                }
            }
            _gameState.value = _gameState.value.copy(firstSelectedCardId = null)
        }
    }

    private fun checkStageCompletion() {
        if (_gameState.value.cards.all { it.isMatched }) {
            startNewStage(_gameState.value.stage + 1)
        }
    }

    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palatte ->
            palatte?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }
}

