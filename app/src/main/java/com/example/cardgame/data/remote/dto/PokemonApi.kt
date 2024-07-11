package com.example.cardgame.data.remote.dto

import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET

interface PokemonApi {
    @FormUrlEncoded
    @GET()
    suspend fun getPokemonList(): List<PokemonResponse>
}