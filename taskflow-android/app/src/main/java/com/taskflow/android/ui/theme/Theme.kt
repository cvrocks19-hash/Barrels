package com.taskflow.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors  = darkColorScheme(primary = Blue80,  secondary = BlueGrey80, tertiary = Teal80)
private val LightColors = lightColorScheme(primary = Blue40, secondary = BlueGrey40, tertiary = Teal40)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean    = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,          // Pixel dynamic color (roadmap #11)
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else           dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
