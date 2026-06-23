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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.network.PhotoDto
import com.example.portfolioapp.network.UserDto
import com.example.portfolioapp.presentation.components.GradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    user: UserDto,
    photos: List<PhotoDto>,
    onPhotoClick: (Long) -> Unit,
    onLikeClick: (Long) -> Unit, 
    onBack: () -> Unit
) {
    val context = LocalContext.current

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Профиль", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // === ШАПКА ПРОФИЛЯ ===
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (user.avatarUrl.isNullOrEmpty()) null else user.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Аватар",
                        modifier = Modifier.size(100.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFF27272A)),
                        error = ColorPainter(Color(0xFF3F3F46))
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = user.fullName,
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    if (!user.description.isNullOrEmpty()) {
                        Text(
                            text = user.description,
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                Text(
                    text = "Фотографии (${photos.size})",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                // === СЕТКА ФОТОГРАФИЙ ===
                if (photos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет опубликованных фото", color = Color.Gray, fontSize = 16.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            PublicPhotoGridItem(
                                photo = photo,
                                onPhotoClick = { onPhotoClick(photo.id) },
                                onLikeClick = { onLikeClick(photo.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun PublicPhotoGridItem(
    photo: PhotoDto,
    onPhotoClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onPhotoClick)
    ) {
        // Изображение
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = photo.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFF18181B)),
            error = ColorPainter(Color(0xFF3F3F46))
        )

        //  Кнопка лайка (в правом нижнем углу)
        IconButton(
            onClick = onLikeClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .background(Color(0xCC000000), CircleShape)
        ) {
            Icon(
                imageVector = if (photo.likedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (photo.likedByCurrentUser) Color(0xFFFF5252) else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Затемнение внизу для читаемости
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
        )

        // Название и счетчик лайков
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = photo.title.take(15) + if (photo.title.length > 15) "..." else "",
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
                    text = "${photo.likesCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
