package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.data.model.SocialEvent

@Composable
fun SocialEventImage(
    event: SocialEvent,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(modifier = modifier) {
        // Base guaranteed offline vector art from source res/drawable
        Image(
            painter = painterResource(id = event.localDrawableRes),
            contentDescription = event.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )
        // If web URL is provided, try loading with Coil with smooth fallback to the local vector
        if (event.coverImg.startsWith("http")) {
            AsyncImage(
                model = event.coverImg,
                placeholder = painterResource(id = event.localDrawableRes),
                error = painterResource(id = event.localDrawableRes),
                fallback = painterResource(id = event.localDrawableRes),
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}
