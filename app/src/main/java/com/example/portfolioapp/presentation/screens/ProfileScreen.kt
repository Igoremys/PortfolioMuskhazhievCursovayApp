package com.example.portfolioapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.factory.AppViewModelFactory 
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoUiState
import com.example.portfolioapp.viewModel.PhotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettings: () -> Unit,
    onPhotoClick: (Photo) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    photoViewModel: PhotoViewModel = viewModel(
        factory = AppViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by photoViewModel.uiState.collectAsState()

    val myPhotos = when (val state = uiState) {
        is PhotoUiState.Success -> state.photos.filter { it.authorEmail == currentUser?.email }
        else -> emptyList()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarUrl = currentUser?.avatarUrl
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (avatarUrl.isNullOrEmpty()) null else avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable { onSettings() },
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color(0xFF27272A)),
                    error = ColorPainter(Color(0xFF3F3F46))
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.fullName ?: "Пользователь",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.description ?: "Нет описания",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                }

                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Мои фото",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${myPhotos.size}",
                    color = Color(0xFF7C4DFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            when (uiState) {
                is PhotoUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                }
                is PhotoUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ошибка: ${(uiState as PhotoUiState.Error).message}",
                            color = Color(0xFFFF5252),
                            fontSize = 14.sp
                        )
                    }
                }
                is PhotoUiState.Empty, is PhotoUiState.Success -> {
                    if (myPhotos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Нет опубликованных фото",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Нажмите + в ленте, чтобы добавить первое фото",
                                    color = Color(0xFF7C4DFF),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(myPhotos, key = { it.id }) { photo ->
                                PhotoGridItem(
                                    photo = photo,
                                    onClick = { onPhotoClick(photo) },
                                    onLikeClick = {
                                        photoViewModel.toggleLike(photo.id, currentUser?.email)
                                    },
                                    onDelete = if (photo.authorEmail == currentUser?.email) {
                                        {
                                            photoViewModel.deletePhoto(
                                                photoId = photo.id,
                                                authorEmail = currentUser?.email ?: ""
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// PhotoGridItem остается без изменений (он идеален)
@Composable
private fun PhotoGridItem(
    photo: Photo,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uri.toString())
                .crossfade(true)
                .build(),
            contentDescription = photo.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFF18181B)),
            error = ColorPainter(Color(0xFF3F3F46))
        )

        if (onDelete != null) {
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color(0xCC000000), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete photo",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(
            onClick = onLikeClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .background(Color(0xCC000000), CircleShape)
        ) {
            Icon(
                imageVector = if (photo.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (photo.isLiked) Color(0xFFFF5252) else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(75.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xDD000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photo.authorAvatarUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Author avatar",
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color(0xFF7C4DFF)),
                    error = ColorPainter(Color(0xFF7C4DFF))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = photo.authorFullName,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = photo.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Likes count",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${photo.likes}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить фото?", color = Color.White) },
            text = { Text("Это действие нельзя отменить", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete?.invoke()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Удалить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)
                ) {
                    Text("Отмена")
                }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
