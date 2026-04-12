package com.example.mavfound.domain.exchange

import kotlin.random.Random

class ExchangeCodeGenerator {

    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generate(length: Int = 6): String {
        require(length > 0) { "Exchange code length must be positive." }
        return buildString(length) {
            repeat(length) {
                append(alphabet[Random.nextInt(alphabet.length)])
            }
        }
    }
}
