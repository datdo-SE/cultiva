package com.yaycommerce.cultiva.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CultivaColors = darkColorScheme(
    primary = ChuSa,
    onPrimary = OnChuSa,
    secondary = HoangChi,
    onSecondary = MucCham,
    tertiary = LinhKhi,
    background = MucCham,
    onBackground = TextPrimary,
    surface = MucCham,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    error = ChuSaSang,
)

@Composable
fun CultivaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CultivaColors, content = content)
}
