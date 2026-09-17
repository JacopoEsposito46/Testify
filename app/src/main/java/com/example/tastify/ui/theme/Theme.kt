package com.example.tastify.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryOrange,
    onPrimary = Black,

    secondary = DarkSecondaryOrange,
    onSecondary = DarkPrimaryOrange,

    tertiary = DarkTertiaryOrange,
    onTertiary = Black,

    error = PrimaryRed,
    onError = Black,

    background = DarkBasicBackground,
    surface = DarkBackground,

    onSurface = DarkPageTitle,
    onSurfaceVariant = DarkSubTitle,

    surfaceVariant = DarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = White,

    secondary = SecondaryOrange,
    onSecondary = PrimaryOrange,

    tertiary = TertiaryOrange,
    onTertiary = White,

    error = PrimaryRed,
    onError = White,

    background = BasicBackground,
    surface = White,

    onSurface = PageTitle,
    onSurfaceVariant = SubTitle,

    surfaceVariant = GrayBackground
)

@Composable
fun TastifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}