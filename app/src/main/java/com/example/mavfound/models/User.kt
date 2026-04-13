package com.example.mavfound.models

data class User(
    val userId: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val isAdmin: Boolean = false,
    val isActive: Boolean = true
)