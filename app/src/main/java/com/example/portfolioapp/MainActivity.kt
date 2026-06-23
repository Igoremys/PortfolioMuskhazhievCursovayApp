package com.example.portfolioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portfolioapp.factory.AppViewModelFactory
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.presentation.screens.*
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoViewModel
import com.example.portfolioapp.viewModel.SearchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = AppViewModelFactory(applicationContext)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF7C4DFF),
                    secondary = Color(0xFFFF6F91),
                    surface = Color(0xFF18181B),
                    background = Color(0xFF09090B)
                )
            ) {
                PortfolioApp(viewModelFactory = viewModelFactory)
            }
        }
    }
}

@Composable
fun PortfolioApp(
    viewModelFactory: AppViewModelFactory,
    authViewModel: AuthViewModel = viewModel(factory = viewModelFactory),
    photoViewModel: PhotoViewModel = viewModel(factory = viewModelFactory)
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val currentUser by authViewModel.currentUser.collectAsState()
    val photos by photoViewModel.photos.collectAsState()
    val isLoading by photoViewModel.isLoading.collectAsState()
    val uploadSuccess by photoViewModel.uploadSuccess.collectAsState()

    LaunchedEffect(Unit) {
        try {
            val token = com.example.portfolioapp.network.TokenManager.getToken()
            if (token != null) {
                authViewModel.loadCurrentUser(context)
                photoViewModel.loadPhotos()
            }
        } catch (e: Exception) {
            android.util.Log.w("PortfolioApp", "TokenManager не доступен: ${e.message}")
        }
    }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            photoViewModel.clearUploadState()
            photoViewModel.loadPhotos()
        }
    }

    GradientBackground {
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        photoViewModel.loadPhotos()
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    viewModel = authViewModel
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("home") { popUpTo("register") { inclusive = true } }
                    },
                    onBackToLogin = { navController.popBackStack() },
                    viewModel = authViewModel
                )
            }

            composable("home") {
                HomeScreen(
                    photoViewModel = photoViewModel,
                    authViewModel = authViewModel,
                    onPhotoClick = { photo -> navController.navigate("photo_detail/${photo.id}") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToDiscover = { navController.navigate("search") },
                    onNavigateToAddPhoto = { navController.navigate("add") }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onSettings = { navController.navigate("settings") },
                    onPhotoClick = { photo ->
                        navController.navigate("photo_detail/${photo.id}")
                    },
                    authViewModel = authViewModel,
                    photoViewModel = photoViewModel
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        authViewModel.logout(context)
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    },
                    viewModelFactory = viewModelFactory,
                    authViewModel = authViewModel
                )
            }

            composable("search") {
                val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
                SearchScreen(
                    onUserSelected = { userId -> navController.navigate("public_profile/$userId") },
                    onBack = { navController.popBackStack() },
                    viewModel = searchViewModel
                )
            }

            composable("add") {
                AddPhotoScreen(
                    onBack = { navController.popBackStack() },
                    viewModelFactory = viewModelFactory
                )
            }

            composable(
                route = "photo_detail/{photoId}",
                arguments = listOf(navArgument("photoId") { type = NavType.LongType })
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getLong("photoId")
                val photo = photos.firstOrNull { it.id.toLong() == photoId }

                if (photo != null) {
                    PhotoDetailScreen(
                        photo = photo,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    navController.popBackStack()
                }
            }

            composable(
                route = "public_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
                val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
                val user by searchViewModel.selectedUser.collectAsState()
                val userPhotos by searchViewModel.userPhotos.collectAsState()
                val isSearching by searchViewModel.isLoading.collectAsState()

                LaunchedEffect(userId) {
                    searchViewModel.loadPublicProfile(context, userId)
                }

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                } else if (user != null) {
                    PublicProfileScreen(
                        user = user!!,
                        photos = userPhotos,
                        onPhotoClick = { photoId ->
                            navController.navigate("photo_detail/$photoId")
                        },
                        onLikeClick = { photoId ->
                            searchViewModel.toggleLikeInProfile(photoId)
                        },
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Не удалось загрузить профиль", color = Color(0xFFFF5252))
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7C4DFF))
            }
        }
    }
}