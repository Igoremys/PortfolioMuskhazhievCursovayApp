package com.example.portfolioapp.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.portfolioapp.data.local.AppDatabase
import com.example.portfolioapp.foundation.repository.PortfolioRepository
import com.example.portfolioapp.network.RetrofitClient
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoViewModel
import com.example.portfolioapp.viewModel.SearchViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val photoApi = RetrofitClient.getPhotoApi()
    private val photoDao = AppDatabase.getDatabase(context.applicationContext).photoDao()
    private val repository = PortfolioRepository(photoApi, photoDao)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PhotoViewModel::class.java) -> {
                PhotoViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel() as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}