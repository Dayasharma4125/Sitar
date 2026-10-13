package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Track

/**
 * High-fidelity artwork display with graceful online & geometric fallback.
 * Displays local cover, online URL, or an expressive gradient avatar with artist monogram.
 */
@Composable
fun ArtworkImageOrFallback(
    track: Track,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    contentDescription: String? = "Album artwork for ${track.title}",
    targetSizePx: Int? = null
) {
    val artModel = track.coverUrl ?: track.albumArtUri

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF151922)),
        contentAlignment = Alignment.Center
    ) {
        if (track.coverResId != null) {
            Image(
                painter = painterResource(id = track.coverResId),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (artModel != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val imageRequest = remember(artModel, targetSizePx) {
                coil.request.ImageRequest.Builder(context)
                    .data(artModel)
                    .dispatcher(kotlinx.coroutines.Dispatchers.IO)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .allowHardware(true)
                    .apply {
                        if (targetSizePx != null && targetSizePx > 0) {
                            size(targetSizePx, targetSizePx)
                        }
                    }
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                filterQuality = androidx.compose.ui.graphics.FilterQuality.Medium,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High-performance clean gradient avatar fallback
            val gradientColors = remember(track.primaryAmbientColor, track.secondaryAmbientColor) {
                if (track.primaryAmbientColor != Color.Unspecified && track.secondaryAmbientColor != Color.Unspecified) {
                    listOf(track.primaryAmbientColor, track.secondaryAmbientColor)
                } else {
                    listOf(Color(0xFF3F82F8), Color(0xFF1E293B))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                val monogram = track.artist.firstOrNull()?.uppercase() ?: "♪"
                Text(
                    text = monogram,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
