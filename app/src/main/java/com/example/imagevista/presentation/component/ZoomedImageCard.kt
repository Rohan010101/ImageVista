package com.example.imagevista.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.example.imagevista.domain.model.UnsplashImage

@Composable
fun ZoomedImageCard(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    image: UnsplashImage?
) {
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(image?.imageUrlRegular)
        .crossfade(true)
        .placeholderMemoryCacheKey(MemoryCache.Key(image?.imageUrlSmall ?: ""))
        .build()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {


        // FORMAT NOT IN USE
//        if (isVisible) {
//            Cloudy(modifier = Modifier.fillMaxSize(), radius = 25) {}
//        }



//        if (isVisible) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .graphicsLayer {
//                        // Apply blur using RenderEffect
//                        renderEffect = RenderEffect.createBlurEffect(
//                            25f, // Blur radius X
//                            25f, // Blur radius Y
//                            Shader.TileMode.CLAMP // Clamps the effect at the edge
//                        )
//                    }
//            )
//        }

        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Card(modifier = modifier) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image of Photographer
                    AsyncImage(
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(CircleShape)
                            .size(25.dp),
                        model = image?.photographerProfileImgUrl,
                        contentDescription = null
                    )

                    // Profile Name of Photographer
                    Text(
                        text = image?.photographerName ?: "Anonymous",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                AsyncImage(
                    modifier = Modifier.fillMaxWidth(),
                    model = imageRequest,
                    contentDescription = null
                )
            }
        }
    }
}