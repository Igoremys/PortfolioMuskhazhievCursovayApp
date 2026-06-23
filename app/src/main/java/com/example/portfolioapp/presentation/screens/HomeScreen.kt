package com.example.portfolioapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.factory.AppViewModelFactory 
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.presentation.components.ModernPhotoCard
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoUiState
import com.example.portfolioapp.viewModel.PhotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    photoViewModel: PhotoViewModel = viewModel(
        factory = AppViewModelFactory(LocalContext.current.applicationContext)
    ),
    authViewModel: AuthViewModel = viewModel(),
    onPhotoClick: (Photo) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDiscover: () -> Unit = {},
    onNavigateToAddPhoto: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val uiState by photoViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val uploadSuccess by photoViewModel.uploadSuccess.collectAsState()

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            photoViewModel.clearUploadState()
          
        }
    }

    GradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToAddPhoto,
                    containerColor = Color(0xFF7C4DFF)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить фото", tint = Color.White)
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF18181B)) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, null, tint = if (selectedTab == 0) Color(0xFF7C4DFF) else Color.Gray) },
                        label = { Text("Главная", color = if (selectedTab == 0) Color(0xFF7C4DFF) else Color.Gray) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onNavigateToDiscover()
                        },
                        icon = { Icon(Icons.Default.Search, null, tint = if (selectedTab == 1) Color(0xFF7C4DFF) else Color.Gray) },
                        label = { Text("Поиск", color = if (selectedTab == 1) Color(0xFF7C4DFF) else Color.Gray) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; onNavigateToProfile() },
                        icon = { Icon(Icons.Default.Person, null, tint = if (selectedTab == 2) Color(0xFF7C4DFF) else Color.Gray) },
                        label = { Text("Профиль", color = if (selectedTab == 2) Color(0xFF7C4DFF) else Color.Gray) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val state = uiState) {
                    is PhotoUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF7C4DFF))
                        }
                    }
                    is PhotoUiState.Error -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = Color(0xFFFF5252).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = " ${state.message}",
                                color = Color(0xFFFF5252),
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                    is PhotoUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Лента пуста", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                    is PhotoUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.photos, key = { it.id }) { photo ->
                                ModernPhotoCard(
                                    photo = photo,
                                    onLikeClick = {
                                        photoViewModel.toggleLike(photo.id, currentUser?.email)
                                    },
                                    onPhotoClick = onPhotoClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
