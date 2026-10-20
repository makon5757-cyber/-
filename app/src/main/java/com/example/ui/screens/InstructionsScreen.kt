package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ScreenState
import com.example.ui.TajSendViewModel
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.EmeraldNeon

@Composable
fun InstructionsScreen(
    viewModel: TajSendViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenState.DASHBOARD)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Инструкция", "Сравнение с Shareit", "О Kolid Company")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
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
                Column {
                    Text(
                        text = "Справка и Инструкция",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Руководство пользователя TajSend",
                        color = CyanNeon,
                        fontSize = 12.sp
                    )
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberBackground,
                contentColor = EmeraldNeon,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = EmeraldNeon,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) EmeraldNeon else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> InstructionsTab()
                1 -> ComparisonTab()
                2 -> AboutKolidTab()
            }
        }
    }
}

@Composable
fun InstructionsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            InstructionStepCard(
                stepNumber = "1",
                icon = Icons.Default.Wifi,
                title = "Установка и настройка подключения",
                description = "Для передачи файлов подключите оба устройства к одной и той же сети Wi-Fi. Если рядом нет Wi-Fi роутера, на одном из устройств включите «Точку доступа Wi-Fi (Hotspot)», а со второго телефона подключитесь к ней. Интернет-трафик не расходуется!",
                badge = "Быстрый старт"
            )
        }

        item {
            InstructionStepCard(
                stepNumber = "2",
                icon = Icons.AutoMirrored.Filled.Send,
                title = "Отправка файлов",
                description = "1. Нажмите «ОТПРАВИТЬ» на главном экране.\n2. Выберите фото, видео, музыку или документы (можно выбрать сразу несколько файлов).\n3. Нажмите зеленую кнопку «Отправить».\n4. Наведите камеру на QR-код на экране принимающего устройства (или введите IP и PIN вручную).\n5. Файлы мгновенно передадутся на высокой скорости до 45 МБ/с.",
                badge = "Передача"
            )
        }

        item {
            InstructionStepCard(
                stepNumber = "3",
                icon = Icons.Default.CallReceived,
                title = "Получение файлов",
                description = "1. Нажмите «ПОЛУЧИТЬ» на главном экране.\n2. Приложение запустит защищенный локальный сервер и отобразит уникальный QR-код и 6-значный PIN.\n3. Покажите экран отправителю для сканирования.\n4. Принятые файлы сохраняются в папку «Загрузки/TajSend» и доступны в журнале приложения.",
                badge = "Прием"
            )
        }

        item {
            InstructionStepCard(
                stepNumber = "4",
                icon = Icons.Default.Build,
                title = "Устранение неполадок",
                description = "• Устройство не видит QR-код: убедитесь, что предоставлен доступ к камере, или используйте кнопку «Ввести IP и PIN вручную».\n• Ошибка подключения: проверьте, отключены ли VPN и файерволы на устройствах, так как они могут блокировать локальные IP-пакеты.\n• Низкая скорость: используйте частоту Wi-Fi 5 ГГц или режим мобильной точки доступа Hotspot.",
                badge = "Советы"
            )
        }
    }
}

@Composable
fun InstructionStepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String,
    badge: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyberCardBorder, CyberCardBorder)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber,
                            color = EmeraldNeon,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = badge,
                    color = CyanNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyanNeon.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ComparisonTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(EmeraldNeon, CyanNeon))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Таблица сравнения TajSend vs Shareit",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TajSend сохраняет всю базовую функциональность Shareit, но предлагает уникальный чистый дизайн, абсолютную приватность и отсутствие назойливой рекламы.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            // Comparison Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyberCardBorder, CyberCardBorder)))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(text = "Функция", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text(text = "TajSend", color = EmeraldNeon, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text(text = "Shareit", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    ComparisonRow(
                        feature = "Отправка файлов",
                        tajSend = "Через QR-код",
                        shareIt = "Через QR-код",
                        isHighlight = false
                    )
                    ComparisonRow(
                        feature = "Получение файлов",
                        tajSend = "Через QR-код",
                        shareIt = "Через QR-код",
                        isHighlight = false
                    )
                    ComparisonRow(
                        feature = "Дизайн интерфейса",
                        tajSend = "Уникальный Cyber",
                        shareIt = "Стандартный",
                        isHighlight = true
                    )
                    ComparisonRow(
                        feature = "Реклама и спам",
                        tajSend = "0% (Полный отказ)",
                        shareIt = "Множество баннеров",
                        isHighlight = true
                    )
                    ComparisonRow(
                        feature = "Приватность данных",
                        tajSend = "100% Локально",
                        shareIt = "Сбор телеметрии",
                        isHighlight = true
                    )
                    ComparisonRow(
                        feature = "Скорость передачи",
                        tajSend = "До 45+ МБ/с (Чанки)",
                        shareIt = "До 20-30 МБ/с",
                        isHighlight = true
                    )
                    ComparisonRow(
                        feature = "APK-файл",
                        tajSend = "Доступен",
                        shareIt = "Доступен",
                        isHighlight = false
                    )
                    ComparisonRow(
                        feature = "Бренд разработчика",
                        tajSend = "Kolid Company",
                        shareIt = "Smart Media4U",
                        isHighlight = true
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF003824))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Вывод экспертной оценки",
                        color = EmeraldNeon,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Приложение TajSend является передовым аналогом Shareit с уникальным дизайном от Kolid Company. Оно позволяет пользователям отправлять и получать файлы через сканер QR-кодов, работает полностью автономно без интернета, оптимизировано для сверхбыстрой передачи крупных видео и архивов и не содержит отвлекающей рекламы.",
                        color = Color(0xFFA7F3D0),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(
    feature: String,
    tajSend: String,
    shareIt: String,
    isHighlight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = tajSend,
            color = if (isHighlight) EmeraldNeon else Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = shareIt,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AboutKolidTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Brand hero banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(EmeraldNeon, CyanNeon))
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tajsend_hero),
                    contentDescription = "TajSend Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyberCardBorder, CyberCardBorder)))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0B101E))
                            .border(1.5.dp, CyanNeon, RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_tajsend_logo),
                            contentDescription = "Kolid Company Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Kolid Company",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Технологии быстрой локальной связи",
                        color = CyanNeon,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Kolid Company специализируется на разработке высокопроизводительных утилит для мобильных устройств. Приложение TajSend создано как ответ перегруженным рекламой сервисам передачи файлов. Наша цель — максимальная скорость передачи файлов по беспроводному каналу, кристально чистый интерфейс и нулевой сбор персональных данных.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberSurfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Версия", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("v1.0 Release", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Платформа", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("Android APK", color = EmeraldNeon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Разработчик", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("Kolid Dev Team", color = CyanNeon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
