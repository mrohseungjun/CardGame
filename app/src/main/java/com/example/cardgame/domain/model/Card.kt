package com.example.cardgame.domain.model

data class Card(
    val id: Int,
    val imageUrl: String,
    val name: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
) {
}