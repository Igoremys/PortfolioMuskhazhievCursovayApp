package com.example.portfolioapp.presentation.screens

import android.widget.Toast
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoScreen(
    onBack: () -> Unit,
    viewModelFactory: androidx.lifecycle.ViewModelProvider.Factory,
    photoViewModel: PhotoViewModel = viewModel(factory = viewModelFactory),
    authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") } 

    val context = LocalContext.current
    val isLoading by photoViewModel.isLoading.collectAsState()
    val error by photoViewModel.error.collectAsState()
    val uploadSuccess by photoViewModel.uploadSuccess.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    //  Реакция на успешную загрузку
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            Toast.makeText(context, "Фото успешно добавлено!", Toast.LENGTH_SHORT).show()
            photoViewModel.clearUploadState()
            onBack()
        }
    }

    //  Очистка ошибки
    LaunchedEffect(title, description, imageUrl) {
        if (error != null) photoViewModel.clearError()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //  Кнопка "Назад"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Add New Photo",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            //  Ошибка
            error?.let { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF5252).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = " $msg",
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            //  Предпросмотр по URL
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2A2A))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF7C4DFF), Color(0xFFFF6F91))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFF18181B)),
                        error = ColorPainter(Color(0xFF3F3F46))
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image",
                            tint = Color(0xFF7C4DFF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Preview will appear here",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            //  Поле "URL изображения"
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://example.com/photo.jpg", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF7C4DFF),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFF7C4DFF)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(Modifier.height(16.dp))

            //  Поле "Название"
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF7C4DFF),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFF7C4DFF)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            //  Поле "Описание"
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF7C4DFF),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFF7C4DFF)
                )
            )

            Spacer(Modifier.weight(1f))

            //  Кнопка добавления
            Button(
                onClick = {
                    if (imageUrl.isNotBlank() && title.isNotBlank()) {
                        val authorEmail = currentUser?.email ?: "unknown@example.com"
                        photoViewModel.createPhotoFromUrl(
                            imageUrl = imageUrl,
                            title = title,
                            description = description,
                            authorEmail = authorEmail
                        )
                    } else {
                        Toast.makeText(
                            context,
                            " Заполните URL и название",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C4DFF)
                ),
                enabled = !isLoading && imageUrl.isNotBlank() && title.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(" Add Photo", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
