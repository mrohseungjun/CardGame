package com.example.cardgame.domain.repository

import com.example.cardgame.data.remote.dto.PokemonListResponse
import com.example.cardgame.util.Resource

interface PokemonRepository {
    suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonListResponse>
}