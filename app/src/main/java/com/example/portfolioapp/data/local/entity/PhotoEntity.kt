package com.example.portfolioapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val likes: Int,
    val isLiked: Boolean,
    val authorEmail: String,
    val authorFullName: String,
    val authorAvatarUrl: String?
)