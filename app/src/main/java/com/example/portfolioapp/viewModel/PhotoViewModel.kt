package com.example.portfolioapp.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.foundation.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PhotoViewModel(
    private val repository: PortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoUiState>(PhotoUiState.Loading)
    val uiState: StateFlow<PhotoUiState> = _uiState.asStateFlow()

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = PhotoUiState.Loading
            _isLoading.value = true

            repository.refreshPhotos()

            repository.photos
                .catch { e ->
                    _uiState.value = PhotoUiState.Error("Ошибка чтения данных: ${e.message}")
                    _error.value = e.message
                }
                .collect { photosList ->
                    _photos.value = photosList 
                    if (photosList.isEmpty()) {
                        _uiState.value = PhotoUiState.Empty
                    } else {
                        _uiState.value = PhotoUiState.Success(photosList)
                    }
                    _isLoading.value = false
                }
        }
    }

    fun createPhotoFromUrl(imageUrl: String, title: String, description: String, authorEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val request = com.example.portfolioapp.network.PhotoCreateRequest(
                title = title,
                description = description,
                imageUrl = imageUrl
            )
            val result = repository.createPhotoFromUrl(request, authorEmail)

            if (result != null) {
                _uploadSuccess.value = true
            } else {
                _error.value = "Ошибка создания фото"
                _uiState.value = PhotoUiState.Error("Ошибка создания фото")
            }
            _isLoading.value = false
        }
    }

    fun uploadAndCreatePhoto(context: Context, uri: Uri, title: String, description: String, authorEmail: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val localFile = savePhotoLocally(context, uri)
                if (!localFile.exists() || localFile.length() == 0L) {
                    _error.value = "Файл пуст или не создан"
                    _uiState.value = PhotoUiState.Error("Файл пуст или не создан")
                    _isLoading.value = false
                    return@launch
                }

                val result = repository.uploadAndCreatePhoto(context, localFile, title, description, authorEmail)
                if (result != null) {
                    _uploadSuccess.value = true
                } else {
                    _error.value = "Ошибка загрузки на сервер"
                    _uiState.value = PhotoUiState.Error("Ошибка загрузки на сервер")
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
                _uiState.value = PhotoUiState.Error("Ошибка: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(photoId: Int, currentUserEmail: String?) {
        if (currentUserEmail == null) {
            _error.value = "Пользователь не авторизован"
            return
        }
        viewModelScope.launch {
            repository.toggleLike(photoId)
        }
    }

    fun deletePhoto(photoId: Int, authorEmail: String) {
        viewModelScope.launch {
            _uiState.value = PhotoUiState.Loading
            _isLoading.value = true
            repository.deletePhoto(photoId)
            _isLoading.value = false
        }
    }

    private fun savePhotoLocally(context: Context, uri: Uri): File {
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUploadState() {
        _uploadSuccess.value = false
    }
}

sealed class PhotoUiState {
    object Loading : PhotoUiState()
    object Empty : PhotoUiState()
    data class Success(val photos: List<com.example.portfolioapp.entity.Photo>) : PhotoUiState()
    data class Error(val message: String) : PhotoUiState()
}
