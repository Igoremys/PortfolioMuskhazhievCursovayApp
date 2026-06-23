package com.example.portfolioapp.network

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String,

    @SerializedName("author") val author: String = "", // email
    @SerializedName("authorFullName") val authorFullName: String? = null, // ✅ НОВОЕ
    @SerializedName("authorAvatarUrl") val authorAvatarUrl: String? = null, // ✅ НОВОЕ

    val createdAt: String,
    val updatedAt: String,

    @SerializedName("likesCount") val likesCount: Int = 0,
    @SerializedName("likedByCurrentUser") val likedByCurrentUser: Boolean = false
)