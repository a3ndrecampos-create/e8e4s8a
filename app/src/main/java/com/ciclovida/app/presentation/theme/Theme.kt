package com.ciclovida.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val RoseDeep = Color(0xFFE85D75)
private val Plum = Color(0xFF8E5B9F)
private val Sand = Color(0xFFF6B26B)
private val Background = Color(0xFFFFF7F8)

private val LightColors = lightColorScheme(
    primary = RoseDeep,
    secondary = Plum,
    tertiary = Sand,
    background = Background
)

private val DarkColors = darkColorScheme(
    primary = RoseDeep,
    secondary = Plum,
    tertiary = Sand
)

@Composable
fun CicloVidaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // paleta própria tem prioridade sobre Material You
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(colorScheme = colorScheme, typography = CicloVidaTypography, content = content)
}
