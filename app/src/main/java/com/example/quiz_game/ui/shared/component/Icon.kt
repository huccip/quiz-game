package com.example.quiz_game.ui.shared.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import coil3.compose.AsyncImage
import com.example.quiz_game.R

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    model: Any? = null,
    contentDescription: String? = null,
    style: TextStyle = LocalTextStyle.current,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val iconSize = with(LocalDensity.current) { style.fontSize.toDp() }

    painter?.let {
        Icon(
            painter = it,
            tint = tint,
            contentDescription = contentDescription,
            modifier = modifier.size(iconSize)
        )
    }

    imageVector?.let {
        Icon(
            imageVector = it,
            tint = tint,
            contentDescription = contentDescription,
            modifier = modifier.size(iconSize)
        )
    }

    model?.let {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            error = painterResource(R.drawable.default_image_placeholder),
            placeholder = painterResource(R.drawable.default_image_placeholder),
            contentScale = ContentScale.Crop,
            modifier = modifier.size(iconSize)
        )
    }
}
