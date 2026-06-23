package com.example.portfolioapp.data.local.mapper

import android.net.Uri
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.data.local.entity.PhotoEntity
import com.example.portfolioapp.network.PhotoDto

fun PhotoDto.toEntity(): PhotoEntity {
    return PhotoEntity(
        id = this.id.toInt(),
        title = this.title,
        description = this.description ?: "",
        imageUrl = this.imageUrl,
        likes = this.likesCount,
        isLiked = this.likedByCurrentUser,
        authorEmail = this.author,
        authorFullName = this.authorFullName ?: this.author.substringBefore("@", "Пользователь"),
        authorAvatarUrl = this.authorAvatarUrl
    )
}

fun PhotoEntity.toDomain(): Photo {
    return Photo(
        id = this.id,
        title = this.title,
        description = this.description,
        uri = Uri.parse(this.imageUrl),
        likes = this.likes,
        isLiked = this.isLiked,
        authorEmail = this.authorEmail,
        authorFullName = this.authorFullName,
        authorAvatarUri = this.authorAvatarUrl?.let { Uri.parse(it) },
        isLocal = false
    )
}

fun Photo.toEntity(): PhotoEntity {
    return PhotoEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        imageUrl = this.uri?.toString() ?: "",
        likes = this.likes,
        isLiked = this.isLiked,
        authorEmail = this.authorEmail,
        authorFullName = this.authorFullName,
        authorAvatarUrl = this.authorAvatarUri?.toString()
    )
}