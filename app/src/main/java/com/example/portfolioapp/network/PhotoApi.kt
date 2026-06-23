package com.example.portfolioapp.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface PhotoApi {

    @GET("photos")
    suspend fun getAllPhotos(): Response<List<PhotoDto>>

    @GET("photos/{id}")
    suspend fun getPhotoById(@Path("id") id: Long): Response<PhotoDto>

    @POST("photos")
    suspend fun createPhoto(@Body photo: PhotoCreateRequest): Response<PhotoDto>

    @DELETE("photos/{id}")
    suspend fun deletePhoto(@Path("id") id: Long): Response<Unit>

    @POST("photos/{id}/like")
    suspend fun toggleLike(@Path("id") id: Long): Response<PhotoDto>

    @Multipart
    @POST("photos/upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): Response<PhotoDto>

    @GET("photos")
    suspend fun getPhotosByAuthorId(@Query("authorId") authorId: Long): Response<List<PhotoDto>>
}
