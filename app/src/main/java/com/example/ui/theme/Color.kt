package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// TajSend Signature Colors
val EmeraldNeon = Color(0xFF00E5A3)
val EmeraldDark = Color(0xFF00A876)
val CyanNeon = Color(0xFF00D4FF)
val CyanDark = Color(0xFF0284C7)
val VioletNeon = Color(0xFFA855F7)
val AmberSpeed = Color(0xFFFFB020)

// Dark Cyber Palette (Primary)
val CyberBackground = Color(0xFF0B101E)
val CyberSurface = Color(0xFF131A2E)
val CyberSurfaceVariant = Color(0xFF1C253E)
val CyberCardBorder = Color(0xFF283556)
val CyberTextPrimary = Color(0xFFF1F5F9)
val CyberTextSecondary = Color(0xFF94A3B8)

// Light Clean Palette
val CleanBackground = Color(0xFFF6F8FC)
val CleanSurface = Color(0xFFFFFFFF)
val CleanSurfaceVariant = Color(0xFFEDF2F9)
val CleanCardBorder = Color(0xFFD8E2EE)
val CleanTextPrimary = Color(0xFF0F172A)
val CleanTextSecondary = Color(0xFF64748B)

// Gradients
val TajSendBrandBrush = Brush.horizontalGradient(
    colors = listOf(EmeraldNeon, CyanNeon)
)
val TajSendGlowBrush = Brush.radialGradient(
    colors = listOf(CyanNeon.copy(alpha = 0.25f), Color.Transparent)
)
val CyberHeaderBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF131A2E), Color(0xFF0B101E))
)
