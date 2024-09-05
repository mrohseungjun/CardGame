package com.example.cardgame.data.repository

import com.example.cardgame.data.remote.dto.PokemonApi
import com.example.cardgame.data.remote.dto.PokemonListResponse
import com.example.cardgame.util.Resource
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val api: PokemonApi
) : PokemonRepository {
    override suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonListResponse> {
        api.getPokemonList(limit, offset).let { response ->
            return Resource.Success(response)
        }
    }

}