package com.example.cardgame.presentation.model

data class PokemonList(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Pokemon>
) {
}