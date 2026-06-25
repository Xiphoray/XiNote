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
    primary = TerracottaPrimaryDark,
    onPrimary = TerracottaOnPrimaryDark,
    primaryContainer = TerracottaPrimaryContainerDark,
    onPrimaryContainer = TerracottaOnPrimaryContainerDark,
    background = HighDensityBgDark,
    surface = HighDensitySurfaceDark,
    onBackground = HighDensityOnSurfaceDark,
    onSurface = HighDensityOnSurfaceDark,
    onSurfaceVariant = HighDensityOnSurfaceVariantDark,
    outlineVariant = HighDensityOutlineVariantDark,
    outline = HighDensityOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaPrimaryContainer,
    onPrimaryContainer = TerracottaOnPrimaryContainer,
    background = HighDensityBgLight,
    surface = HighDensitySurfaceLight,
    onBackground = HighDensityOnSurfaceLight,
    onSurface = HighDensityOnSurfaceLight,
    onSurfaceVariant = HighDensityOnSurfaceVariantLight,
    outlineVariant = HighDensityOutlineVariantLight,
    outline = HighDensityOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to force show the brand new custom High Density color palette
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
