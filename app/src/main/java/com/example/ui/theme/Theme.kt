package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = LotrProgressDark,
    secondary = LotrMilestoneBgDark,
    tertiary = LotrMilestoneTextDark,
    background = LotrBgDark,
    surface = LotrBgDark,
    surfaceVariant = LotrCardBgDark,
    onPrimary = Color.Black,
    onSecondary = LotrMilestoneTextDark,
    onTertiary = LotrTextPrimaryDark,
    onBackground = LotrTextPrimaryDark,
    onSurface = LotrTextPrimaryDark,
    onSurfaceVariant = LotrTextSecondaryDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LotrProgressLight,
    secondary = LotrMilestoneBgLight,
    tertiary = LotrMilestoneTextLight,
    background = LotrBgLight,
    surface = LotrBgLight,
    surfaceVariant = LotrCardBgLight,
    onPrimary = Color.White,
    onSecondary = LotrMilestoneTextLight,
    onTertiary = LotrTextPrimaryLight,
    onBackground = LotrTextPrimaryLight,
    onSurface = LotrTextPrimaryLight,
    onSurfaceVariant = LotrTextSecondaryLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is bypassed to maintain our immersive Lord of the Rings journey aesthetics!
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
