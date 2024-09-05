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
import com.example.cardgame.domain.repository.PokemonRepository
import com.example.cardgame.presentation.mapper.toPokemonList
import com.example.cardgame.util.Constants
import com.example.cardgame.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

data class GameState(
    val cards: List<PokemonCard> = emptyList(),
    val stage: Int = 1,
    val timeLeft: Int = 60,
    val isGameOver: Boolean = false,
    val firstSelectedCard: PokemonCard? = null
)

@HiltViewModel
class CardMainScreenViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    private var curPage = 0

    private val _gameState = mutableStateOf(GameState())
    val gameState: State<GameState> = _gameState

    private val _pokemonList = mutableStateOf<List<PokemonCard>>(emptyList())
    val pokemonList: State<List<PokemonCard>> = _pokemonList

    private val _loadError = mutableStateOf("")
    val loadError: State<String> = _loadError

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _endReached = mutableStateOf(false)
    val endReached: State<Boolean> = _endReached

    private var firstSelectedCard: PokemonCard? = null

    init {
        loadPokemonAndCreateCards(1)
    }

    /*fun loadPokemonList() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getPokemonList(Constants.PAGE_SIZE, curPage * Constants.PAGE_SIZE)

            when (result) {
                is Resource.Success -> {
                    val pokemonListResponse = result.data?.toPokemonList()?.results
                    _endReached.value = curPage * Constants.PAGE_SIZE >= result.data?.count ?: 0
                    curPage++

                    _loadError.value = ""
                    _isLoading.value = false

                    _pokemonList.value = pokemonListResponse?.mapIndexed { index, pokemon ->
                        PokemonCard(
                            id = index,
                            name = pokemon.name,
                            imageResId = pokemon.imageUrl
                        )
                    } ?: emptyList()
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
    }*/

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

    fun loadPokemonAndCreateCards(stage: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getPokemonList(Constants.PAGE_SIZE, curPage * Constants.PAGE_SIZE)

            when (result) {
                is Resource.Success -> {
                    val pokemonListResponse = result.data?.toPokemonList()?.results
                    curPage++
                    _loadError.value = ""
                    _isLoading.value = false

                    pokemonListResponse?.let { pokemonList ->
                        val pairsCount = minOf(6 + stage - 1, 12)
                        val cards = pokemonList.take(pairsCount)
                            .flatMap { pokemon ->
                                val pokemonId = 1
                                listOf(
                                    PokemonCard(
                                        id = pokemonId * 2,
                                        name = pokemon.name,
                                        imageResId = pokemon.imageUrl
                                    ),
                                    PokemonCard(
                                        id = pokemonId * 2 + 1,
                                        name = pokemon.name,
                                        imageResId = pokemon.imageUrl
                                    )
                                )
                            }.shuffled()

                        _gameState.value = _gameState.value.copy(
                            cards = cards,
                            stage = stage,
                            timeLeft = maxOf(60 - (stage - 1) * 5, 10) // 스테이지에 따라 시간 조절
                        )
                    }
                }

                is Resource.Error -> {
                    Log.d("test", "${result.message}")
                    _loadError.value = result.message ?: "An unexpected error occurred"
                    _isLoading.value = false
                }

                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }


    fun flipCard(card: PokemonCard) {
        val updatedCards = _gameState.value.cards.map {
            if (it.id == card.id) it.copy(isFlipped = !it.isFlipped) else it
        }
        _gameState.value = _gameState.value.copy(cards = updatedCards)
    }

    fun matchCards(card1: PokemonCard, card2: PokemonCard) {
        val updatedCards = _gameState.value.cards.map {
            when (it.id) {
                card1.id, card2.id -> it.copy(isMatched = true)
                else -> it
            }
        }
        _gameState.value = _gameState.value.copy(cards = updatedCards)
    }

    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palatte ->
            palatte?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }

    fun handleCardClick(clickedCard: PokemonCard) {
        if (clickedCard.isMatched || clickedCard.isFlipped) return

        flipCard(clickedCard)

        val firstSelectedCard = _gameState.value.firstSelectedCard
        if (firstSelectedCard == null) {
            _gameState.value = _gameState.value.copy(firstSelectedCard = clickedCard)
        } else {
            if (firstSelectedCard.imageResId == clickedCard.imageResId) {
                matchCards(firstSelectedCard, clickedCard)
                _gameState.value = _gameState.value.copy(firstSelectedCard = null)
            } else {
                viewModelScope.launch {
                    delay(1000)
                    flipCard(firstSelectedCard)
                    flipCard(clickedCard)
                    _gameState.value = _gameState.value.copy(firstSelectedCard = null)
                }
            }
        }
    }
}

