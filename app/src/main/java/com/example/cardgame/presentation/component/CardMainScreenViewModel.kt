package com.example.cardgame.presentation.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardgame.domain.repository.PokemonRepository
import com.example.cardgame.util.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class CardMainScreenViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    private var curPage = 0
    init {
        loadPokemonList()
    }
    fun loadPokemonList() {
        viewModelScope.launch {
            val result = repository.getPokemonList(Constants.PAGE_SIZE, curPage * Constants.PAGE_SIZE)
        }
    }
}

