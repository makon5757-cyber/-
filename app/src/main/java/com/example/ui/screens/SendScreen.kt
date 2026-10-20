package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.model.LocalFileItem
import com.example.model.TransferStatus
import com.example.model.formatFileSize
import com.example.ui.ScreenState
import com.example.ui.TajSendViewModel
import com.example.ui.components.ScannerDialog
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.EmeraldNeon

@Composable
fun SendScreen(
    viewModel: TajSendViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        val p = viewModel.senderProgress.value
        if (p.status == TransferStatus.TRANSFERRING) {
            viewModel.cancelSending()
        }
        viewModel.navigateTo(ScreenState.DASHBOARD)
    }

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableFiles by viewModel.availableFiles.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val isLoading by viewModel.isLoadingFiles.collectAsState()
    val senderProgress by viewModel.senderProgress.collectAsState()
    val isScannerOpen by viewModel.isScannerOpen.collectAsState()

    // System File Picker (supports any mime type)
    val systemPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addCustomUris(uris)
        }
    }

    // Active Transfer HUD
    if (senderProgress.status == TransferStatus.TRANSFERRING ||
        senderProgress.status == TransferStatus.CONNECTING ||
        senderProgress.status == TransferStatus.COMPLETED ||
        senderProgress.status == TransferStatus.ERROR
    ) {
        SendingProgressView(
            progress = senderProgress,
            onCancel = { viewModel.cancelSending() },
            onDone = {
                viewModel.resetSending()
                viewModel.clearSelection()
                viewModel.navigateTo(ScreenState.DASHBOARD)
            },
            onRetry = {
                viewModel.resetSending()
                viewModel.openScanner()
            }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.DASHBOARD) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(CyberSurfaceVariant, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Отправка файлов",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Выберите файлы для передачи",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                // System file picker button
                OutlinedButton(
                    onClick = { systemPicker.launch(arrayOf("*/*")) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyanNeon,
                        containerColor = CyberSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(CyanNeon, EmeraldNeon))
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Обзор", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Categories Tab Row
            ScrollableTabRow(
                selectedTabIndex = FileCategory.entries.indexOf(selectedCategory),
                containerColor = CyberBackground,
                contentColor = EmeraldNeon,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[FileCategory.entries.indexOf(selectedCategory)]),
                        color = EmeraldNeon,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                FileCategory.entries.forEach { category ->
                    val isSelected = category == selectedCategory
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(category) },
                        text = {
                            Text(
                                text = category.title,
                                color = if (isSelected) EmeraldNeon else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // File List
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldNeon)
                }
            } else if (availableFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Файлы не найдены в этой категории",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Используйте кнопку «Обзор» вверху для выбора любых файлов с устройства",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = if (selectedFiles.isNotEmpty()) 100.dp else 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableFiles, key = { it.id }) { file ->
                        FileSelectRow(
                            item = file,
                            isSelected = selectedFiles.any { it.id == file.id },
                            onToggle = { viewModel.toggleFileSelection(file) }
                        )
                    }
                }
            }
        }

        // Sticky Bottom Send Bar
        AnimatedVisibility(
            visible = selectedFiles.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            val totalBytes = selectedFiles.sumOf { it.size }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(EmeraldNeon, CyanNeon))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Выбрано: ${selectedFiles.size} ${getFilesWord(selectedFiles.size)}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Общий объем: ${formatFileSize(totalBytes)}",
                            color = EmeraldNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.clearSelection() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(CyberSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.openScanner() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldNeon),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF003824), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Отправить",
                                color = Color(0xFF003824),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Scanner Dialog
        ScannerDialog(
            isOpen = isScannerOpen,
            onDismiss = { viewModel.closeScanner() },
            onPayloadDetected = { payload ->
                viewModel.connectAndSend(payload)
            }
        )
    }
}

@Composable
fun FileSelectRow(
    item: LocalFileItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF003B28) else CyberSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isSelected) {
                Brush.horizontalGradient(listOf(EmeraldNeon, CyanNeon))
            } else {
                Brush.horizontalGradient(listOf(CyberCardBorder, CyberCardBorder))
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (item.category) {
                FileCategory.IMAGES -> Icons.Default.Image
                FileCategory.VIDEOS -> Icons.Default.Videocam
                FileCategory.AUDIO -> Icons.Default.MusicNote
                FileCategory.DOCUMENTS -> Icons.Default.Description
                FileCategory.APPS -> Icons.Default.FolderZip
                FileCategory.ALL -> Icons.Default.Folder
            }
            val iconColor = when (item.category) {
                FileCategory.IMAGES -> Color(0xFF38BDF8)
                FileCategory.VIDEOS -> Color(0xFFA855F7)
                FileCategory.AUDIO -> Color(0xFFF43F5E)
                FileCategory.DOCUMENTS -> Color(0xFF10B981)
                FileCategory.APPS -> Color(0xFFF59E0B)
                FileCategory.ALL -> EmeraldNeon
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.formattedSize} • ${item.category.title}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldNeon,
                    checkmarkColor = Color(0xFF003824),
                    uncheckedColor = Color(0xFF64748B)
                )
            )
        }
    }
}

@Composable
fun SendingProgressView(
    progress: com.example.model.TransferProgress,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    val animatedPercent by animateFloatAsState(
        targetValue = progress.overallPercent,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (progress.status) {
                TransferStatus.CONNECTING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        color = CyanNeon,
                        strokeWidth = 5.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Подключение к получателю...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Установка высокоскоростного прямого канала передачи",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedButton(onClick = onCancel) {
                        Text("Отмена", color = Color(0xFF94A3B8))
                    }
                }

                TransferStatus.TRANSFERRING -> {
                    // Speedometer Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberSurface)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = AmberSpeed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = progress.formattedSpeed,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Скорость передачи",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Передача файла ${progress.currentFileIndex} из ${progress.totalFilesCount}",
                        color = EmeraldNeon,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = progress.currentFileName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = EmeraldNeon,
                        trackColor = CyberSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(animatedPercent * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = progress.formattedTransferred,
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E))
                    ) {
                        Text("Остановить передачу")
                    }
                }

                TransferStatus.COMPLETED -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldNeon,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Передача завершена!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Все выбранные файлы успешно доставлены на устройство.",
                        color = Color(0xFFA7F3D0),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldNeon)
                    ) {
                        Text("Готово", color = Color(0xFF003824), fontWeight = FontWeight.Bold)
                    }
                }

                TransferStatus.ERROR -> {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ошибка при передаче",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progress.errorMessage ?: "Не удалось передать данные. Проверьте подключение к Wi-Fi.",
                        color = Color(0xFFFDA4AF),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDone) {
                            Text("Закрыть", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
                        ) {
                            Text("Повторить", color = Color(0xFF003544), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

fun getFilesWord(count: Int): String {
    val rem10 = count % 10
    val rem100 = count % 100
    return when {
        rem100 in 11..19 -> "файлов"
        rem10 == 1 -> "файл"
        rem10 in 2..4 -> "файла"
        else -> "файлов"
    }
}
