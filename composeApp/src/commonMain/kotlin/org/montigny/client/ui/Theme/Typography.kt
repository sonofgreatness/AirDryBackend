package org.montigny.client.ui.Theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

val Inter = FontFamily(
    Font("fonts/Inter_24pt-Regular.ttf"),
    Font("fonts/Inter_24pt-Bold.ttf")
)

val Merriweather = FontFamily(
    Font("fonts/MerriweatherSans-Regular.ttf"),
    Font("fonts/MerriweatherSans-Bold.ttf")
)

val RobotoMono = FontFamily(
    Font("fonts/Roboto_Condensed-Regular.ttf")
)

val TimberTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Merriweather,
        fontSize = 70.sp,
        color = TextDark
    ),
    headlineMedium = TextStyle(
        fontFamily = Merriweather,
        fontSize = 24.sp,
        color = TextDark
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontSize = 16.sp,
        color = TextDark
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontSize = 14.sp,
        color = TextMuted
    ),
    labelSmall = TextStyle(
        fontFamily = RobotoMono,
        fontSize = 12.sp
    )
)