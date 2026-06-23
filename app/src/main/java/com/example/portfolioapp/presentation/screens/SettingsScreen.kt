package com.example.portfolioapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModelFactory: androidx.lifecycle.ViewModelProvider.Factory,
    authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
) {
    val context = LocalContext.current

    
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val uploadSuccess by authViewModel.avatarUploadSuccess.collectAsState()

    var name by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var description by remember { mutableStateOf(currentUser?.description ?: "") }
    var avatarUrl by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }  

    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.fullName
            description = it.description ?: ""
            avatarUrl = it.avatarUrl ?: ""
        }
    }

    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            Toast.makeText(context, "Аватар обновлён", Toast.LENGTH_SHORT).show()
            authViewModel.clearAvatarUploadState()
        }
    }


    LaunchedEffect(name, description, avatarUrl) {
        if (error != null) authViewModel.clearError()
    }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Настройки", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 👤 Аватар с предпросмотром по URL
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27272A))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (avatarUrl.isNotBlank()) avatarUrl else null)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFF3F3F46)),
                        error = ColorPainter(Color(0xFF3F3F46))
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit avatar URL",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .background(Color(0xFF7C4DFF), CircleShape)
                            .padding(6.dp)
                    )
                }
                Text(
                    "URL аватара в поле ниже",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    label = { Text("Avatar URL", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.com/avatar.jpg", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.LightGray,
                        cursorColor = Color(0xFF7C4DFF)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    trailingIcon = {
                        if (avatarUrl.isNotBlank()) {
                            IconButton(onClick = { avatarUrl = "" }) {
                                Icon(Icons.Default.Edit, "Clear", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("О себе", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(16.dp))

                error?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFF5252).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = " $msg",
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            authViewModel.updateProfile(
                                context = context,
                                newFullName = name,
                                newDescription = description,
                                newAvatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    enabled = !isLoading && name.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Сохранить изменения", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        authViewModel.logout(context)
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Выйти из аккаунта", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
