package com.noemi.streamer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun StreamerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    //    val useDynamicColour = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
//        useDynamicColour && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
//        useDynamicColour && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> darkScheme
        else -> lightScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}


private val lightScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    inversePrimary = Blue80,
    secondary = BlueDark40,
    onSecondary = Color.White,
    secondaryContainer = BlueDark90,
    onSecondaryContainer = BlueDark10,
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Teal10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Grey90,
    onBackground = Grey10,
    surface = PurpleGrey90,
    onSurface = PurpleGrey30,
    inverseSurface = Grey20,
    inverseOnSurface = Grey95,
    surfaceVariant = PurpleGrey90,
    onSurfaceVariant = PurpleGrey30,
    outline = PurpleGrey60
)

private val darkScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    inversePrimary = Blue40,
    secondary = BlueDark80,
    onSecondary = BlueDark20,
    secondaryContainer = BlueDark30,
    onSecondaryContainer = BlueDark90,
    tertiary = Teal80,
    onTertiary = Teal20,
    tertiaryContainer = Teal30,
    onTertiaryContainer = Teal90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Grey10,
    onBackground = Grey90,
    surface = PurpleGrey30,
    onSurface = PurpleGrey80,
    inverseSurface = Grey90,
    inverseOnSurface = Grey10,
    surfaceVariant = PurpleGrey30,
    onSurfaceVariant = PurpleGrey80,
    outline = PurpleGrey80
)