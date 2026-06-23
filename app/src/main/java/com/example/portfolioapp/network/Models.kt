package com.example.portfolioapp.network

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    val email: String,
    val password: String,
    val fullName: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val user: UserDto  
)


data class PhotoCreateRequest(
    val title: String,
    val description: String,
    val imageUrl: String
)

data class ProfileUpdateRequest(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("description") val description: String? = null
)
