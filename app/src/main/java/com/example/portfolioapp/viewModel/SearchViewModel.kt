package com.example.portfolioapp.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.network.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    companion object {
        private const val TAG = "SearchViewModel"
    }

    private val _allUsers = MutableStateFlow<List<UserDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val filteredUsers: StateFlow<List<UserDto>> = combine(_allUsers, _searchQuery) { users, query ->
        if (query.isBlank()) {
            users
        } else {
            users.filter {
                it.fullName.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedUser = MutableStateFlow<UserDto?>(null)
    val selectedUser: StateFlow<UserDto?> = _selectedUser

    private val _userPhotos = MutableStateFlow<List<PhotoDto>>(emptyList())
    val userPhotos: StateFlow<List<PhotoDto>> = _userPhotos

    fun loadAllUsers(context: Context) {
        viewModelScope.launch {
            if (_allUsers.value.isNotEmpty()) {
                Log.d(TAG, "⏭️ Users already loaded, skipping")
                return@launch
            }

            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "📥 Starting to load all users...")
                val response = RetrofitClient.api.getAllUsers()

                Log.d(TAG, "📡 Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    _allUsers.value = response.body()!!
                    Log.d(TAG, "✅ SUCCESS! Loaded ${_allUsers.value.size} users.")
                    if (_allUsers.value.isNotEmpty()) {
                        Log.d(TAG, "👤 First user: ${_allUsers.value.first().fullName} (${_allUsers.value.first().email})")
                    } else {
                        Log.w(TAG, "⚠️ SUCCESS, but list is EMPTY! Database has no users.")
                    }
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ FAILED! Code: ${response.code()}, Body: $errBody")
                    _error.value = "Ошибка загрузки: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ EXCEPTION: ${e.message}", e)
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadPublicProfile(context: Context, userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedUser.value = null
            _userPhotos.value = emptyList()

            try {
                val profileDeferred = async { RetrofitClient.api.getUserById(userId) }
                val photosDeferred = async { RetrofitClient.api.getPhotosByAuthorId(userId) }

                val profileResponse = profileDeferred.await()
                val photosResponse = photosDeferred.await()

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    _selectedUser.value = profileResponse.body()

                    if (photosResponse.isSuccessful && photosResponse.body() != null) {
                        // ✅ Сортируем по убыванию ID, чтобы самые новые фото были сверху
                        _userPhotos.value = photosResponse.body()!!.sortedByDescending { it.id }
                        Log.d(TAG, "✅ Loaded profile + ${_userPhotos.value.size} photos")
                    } else {
                        Log.w(TAG, "⚠️ Photos failed, but profile loaded")
                        _userPhotos.value = emptyList()
                    }
                } else {
                    val errBody = profileResponse.errorBody()?.string()
                    Log.e(TAG, "❌ Profile not found: ${profileResponse.code()}, $errBody")
                    _error.value = "Пользователь не найден"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Load profile exception: ${e.message}", e)
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ НОВОЕ: Метод для мгновенного обновления лайка в профиле пользователя
    fun toggleLikeInProfile(photoId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.toggleLike(photoId)
                if (response.isSuccessful && response.body() != null) {
                    val updatedPhoto = response.body()!!

                    // Мгновенно обновляем конкретное фото в локальном списке
                    _userPhotos.value = _userPhotos.value.map { photo ->
                        if (photo.id == updatedPhoto.id) {
                            updatedPhoto
                        } else {
                            photo
                        }
                    }
                    Log.d(TAG, "✅ Like toggled for photo $photoId in profile")
                } else {
                    Log.e(TAG, "❌ Failed to toggle like: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception toggling like: ${e.message}", e)
            }
        }
    }

    fun clearState() {
        _searchQuery.value = ""
        _selectedUser.value = null
        _userPhotos.value = emptyList()
        _error.value = null
    }
}