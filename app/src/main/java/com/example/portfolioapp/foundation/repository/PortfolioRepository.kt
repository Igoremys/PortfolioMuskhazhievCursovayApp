package com.example.portfolioapp.foundation.repository

import android.content.Context
import android.util.Log
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.data.local.dao.PhotoDao 
import com.example.portfolioapp.foundation.local.mapper.toDomain  
import com.example.portfolioapp.foundation.local.mapper.toEntity  
import com.example.portfolioapp.network.PhotoApi
import com.example.portfolioapp.network.PhotoCreateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PortfolioRepository(
    private val photoApi: PhotoApi,
    private val photoDao: PhotoDao  
) {
    val photos: Flow<List<Photo>> = photoDao.getAllPhotos().map { entities ->
        entities.map { it.toDomain() }.sortedByDescending { it.id }
    }

    suspend fun refreshPhotos() {
        try {
            Log.d("PortfolioRepository", "Попытка обновления с сервера...")
            val response = photoApi.getAllPhotos()
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { it.toEntity() }
                photoDao.insertPhotos(entities)
                Log.d("PortfolioRepository", "Данные успешно закэшированы")
            }
        } catch (e: Exception) {
            Log.w("PortfolioRepository", "Оффлайн-режим: используем кэш. Причина: ${e.message}")
        }
    }

    suspend fun createPhotoFromUrl(request: PhotoCreateRequest, authorEmail: String): Photo? {
        return try {
            val response = photoApi.createPhoto(request)
            if (response.isSuccessful && response.body() != null) {
                val newEntity = response.body()!!.toEntity()
                photoDao.insertPhotos(listOf(newEntity))
                newEntity.toDomain()
            } else null
        } catch (e: Exception) {
            Log.e("PortfolioRepository", "Ошибка создания фото", e)
            null
        }
    }

    suspend fun uploadAndCreatePhoto(context: Context, file: File, title: String, description: String, authorEmail: String): Photo? {
        return try {
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = photoApi.uploadPhoto(body)
            if (response.isSuccessful && response.body() != null) {
                val newEntity = response.body()!!.toEntity()
                photoDao.insertPhotos(listOf(newEntity))
                newEntity.toDomain()
            } else null
        } catch (e: Exception) {
            Log.e("PortfolioRepository", "Ошибка загрузки фото", e)
            null
        }
    }

    suspend fun toggleLike(photoId: Int) {
        try {
            val currentPhoto = photoDao.getPhotoById(photoId)
            if (currentPhoto != null) {
                val updatedLocal = currentPhoto.copy(
                    likes = if (currentPhoto.isLiked) currentPhoto.likes - 1 else currentPhoto.likes + 1,
                    isLiked = !currentPhoto.isLiked
                )
                photoDao.insertPhotos(listOf(updatedLocal))
            }

            val response = photoApi.toggleLike(photoId.toLong())
            if (response.isSuccessful && response.body() != null) {
                photoDao.insertPhotos(listOf(response.body()!!.toEntity()))
            }
        } catch (e: Exception) {
            Log.w("PortfolioRepository", "Лайк не отправлен на сервер, но локально обновлен", e)
        }
    }

    suspend fun deletePhoto(photoId: Int) {
        try {
            val response = photoApi.deletePhoto(photoId.toLong())
            if (response.isSuccessful) {
                photoDao.deletePhotoById(photoId)
            }
        } catch (e: Exception) {
            Log.e("PortfolioRepository", "Ошибка удаления фото", e)
        }
    }
}
