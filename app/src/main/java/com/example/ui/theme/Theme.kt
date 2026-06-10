package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentLavender,
    secondary = AccentLavender,
    tertiary = MutedLavender,
    background = DeepDarkBg,
    surface = CardDarkBg,
    onPrimary = TextContrastDark,
    onSecondary = TextLightHigh,
    onTertiary = TextLightHigh,
    onBackground = TextLightHigh,
    onSurface = TextLightHigh
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AccentLavender,
    secondary = AccentLavender,
    tertiary = MutedLavender,
    background = DeepDarkBg,
    surface = CardDarkBg,
    onPrimary = TextContrastDark,
    onSecondary = TextLightHigh,
    onTertiary = TextLightHigh,
    onBackground = TextLightHigh,
    onSurface = TextLightHigh
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to preserve the spiritual Emerald-Gold aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
