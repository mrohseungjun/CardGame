package com.example.cardgame.presentation.mapper

import com.example.cardgame.data.remote.dto.PokemonListResponse
import com.example.cardgame.data.remote.dto.PokemonResponse
import com.example.cardgame.presentation.model.Pokemon
import com.example.cardgame.presentation.model.PokemonList

fun PokemonListResponse.toPokemonList() = PokemonList(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toPokemon() }
)

fun PokemonResponse.toPokemon() = Pokemon(
    name = name,
    imageUrl = imageUrl
)