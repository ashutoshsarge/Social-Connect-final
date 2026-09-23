package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SocialLogoNavy

/**
 * Brand Logo component rendering the user's uploaded SocialConnect identity:
 * - Circular emblem featuring the gentle hand holding and nurturing a growing plant sprout with checkmark leaf
 * - Modern sans-serif navy typography: "social" and "Connect"
 */
@Composable
fun SocialConnectLogo(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 100.dp,
    showText: Boolean = true,
    textColor: Color = SocialLogoNavy,
    textFontSize: Int = 28
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Elevated circular emblem container without restrictive edge clipping
        Box(
            modifier = Modifier
                .size(emblemSize)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = Color(0x3310B981),
                    ambientColor = Color(0x1A000000)
                )
                .background(Color.White, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_social_connect_logo),
                contentDescription = "SocialConnect Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(emblemSize - 8.dp)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "social",
                    fontSize = textFontSize.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = (-0.5).sp,
                    lineHeight = (textFontSize * 0.95).sp
                )
                Text(
                    text = "Connect",
                    fontSize = textFontSize.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = (-0.5).sp,
                    lineHeight = (textFontSize * 0.95).sp
                )
            }
        }
    }
}

/**
 * Compact horizontal variant for App Bars and compact headers
 */
@Composable
fun SocialConnectLogoCompact(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 36.dp,
    textColor: Color = SocialLogoNavy
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(emblemSize)
                .background(Color.White, CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_social_connect_logo),
                contentDescription = "SocialConnect Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(emblemSize - 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "social",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = "Connect",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = (-0.3).sp
            )
        }
    }
}
