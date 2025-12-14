package org.montigny.client.ui.organizations

import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image


@Composable
fun  LogoDisplayCard(
    logoBytes: ByteArray?
)
{
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(logoBytes) {
        logoBytes?.let { bytes ->
            try {
                val skiaImage = Image.makeFromEncoded(bytes)
                imageBitmap = skiaImage.toComposeImageBitmap()
            } catch (e: Exception) {
                imageBitmap = null
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Logo Preview",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )}
    else
    {
        //Load default image from resources , drawable
        Text("Image here ")
    }
}