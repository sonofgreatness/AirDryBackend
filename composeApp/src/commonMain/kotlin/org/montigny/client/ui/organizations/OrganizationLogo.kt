package org.montigny.client.ui.organizations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image

@Composable
fun OrganizationLogo(logoBytes: ByteArray?) {
    logoBytes?.let { bytes ->
        val imageBitmap: ImageBitmap? = try {
            val skiaImage = Image.makeFromEncoded(bytes)
            skiaImage.toComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
        imageBitmap?.let { img ->
            Spacer(Modifier.height(8.dp))
            Image(
                bitmap = img,
                contentDescription = "Organization Logo",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}
