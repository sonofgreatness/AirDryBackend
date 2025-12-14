package org.montigny.client.ui.Theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Timber-inspired colors
val GreenNile = Color(0xFFA4C639)
val GreenKelly = Color(0xFF4CBB17)
val GreenLight = Color(0xFF8FD694)
val GreenMain = Color(0xFF355E3B)

val BrownDriftwood = Color(0xFFAF8751)
val BrownDark = Color(0xFF4E3629)

val TextDark = Color(0xFF060606)
val TextMuted = Color(0xFF999999)
val BgLight = Color(0xFFF8F9FA)
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)


val Success = Color(0xFF2E7D32)   // deep green
val Warning = Color(0xFFF9A825)   // amber
val Info = Color(0xFF0277BD)      // blue
val Danger = Color(0xFFD32F2F)    // strong red
val Neutral = Color(0xFF616161)   // grey

// Light Theme
val TimberLightColors = lightColorScheme(
    primary = GreenMain,
    onPrimary = White,
    secondary = BrownDriftwood,
    onSecondary = White,
    background = BgLight,
    onBackground = TextDark,
    surface = White,
    onSurface = TextDark,
)

// Dark Theme
val TimberDarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Black,
    secondary = BrownDark,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = BrownDark,
    onSurface = White
)
