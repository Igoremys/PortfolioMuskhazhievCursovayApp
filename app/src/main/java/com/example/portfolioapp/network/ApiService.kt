package com.example.portfolioapp.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<UserDto>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("users")
    suspend fun getAllUsers(): Response<List<UserDto>>

    @GET("photos")
    suspend fun getAllPhotos(): Response<List<PhotoDto>>

    @POST("photos")
    suspend fun createPhoto(@Body photo: PhotoCreateRequest): Response<PhotoDto>

    @GET("photos/{id}")
    suspend fun getPhotoById(@Path("id") id: Long): Response<PhotoDto>

    @DELETE("photos/{id}")
    suspend fun deletePhoto(@Path("id") id: Long): Response<Unit>

    @POST("photos/{id}/like")
    suspend fun toggleLike(@Path("id") id: Long): Response<PhotoDto>

    @Multipart
    @POST("photos/upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): Response<PhotoDto>

    @Multipart
    @POST("users/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<Map<String, String>>

    @GET("users/search")
    suspend fun searchUsers(@Query("keyword") keyword: String): Response<List<UserDto>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserDto>

    @GET("photos/by-author")
    suspend fun getPhotosByAuthorId(@Query("authorId") authorId: Long): Response<List<PhotoDto>>
}
