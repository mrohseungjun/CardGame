package com.example.cardgame.data.repository

import com.example.cardgame.data.remote.dto.PokemonListResponse
import com.example.cardgame.util.Resource

class FakePokemonRepository:PokemonRepository {
    override suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonListResponse> {
        return Resource.Success(PokemonListResponse(count = 1, next = null, previous = null, results = emptyList()))
    }
}
