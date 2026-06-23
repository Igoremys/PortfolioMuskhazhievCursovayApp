package com.example.portfolioapp.network

data class UserDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: String,
    val avatarUrl: String? = null,
    val description: String? = null
)