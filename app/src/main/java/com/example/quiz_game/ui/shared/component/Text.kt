package com.example.quiz_game.ui.shared.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.example.quiz_game.R

@Composable
fun TextFancy(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal,
    style: TextStyle = MaterialTheme.typography.headlineMedium
) {
    Text(
        modifier = modifier,
        textAlign = textAlign,
        text = text,
        color = color,
        style = style,
        fontWeight = fontWeight,
        fontFamily = FontFamily(Font(R.font.fancy))
    )
}

@Composable
fun TextBig(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        color = color,
        style = MaterialTheme.typography.titleLarge,
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = fontWeight
    )
}

@Composable
fun TextRegular(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        color = color,
        style = MaterialTheme.typography.bodyLarge,
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = fontWeight
    )
}

@Composable
fun TextSmol(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.outline,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily(Font(R.font.regular))
    )
}

@Composable
fun TextBerySmol(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.outline,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        textAlign = textAlign,
        style = MaterialTheme.typography.titleSmall,
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = fontWeight
    )
}

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    text: String,
    textDecoration: TextDecoration? = TextDecoration.None,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = fontWeight,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.labelLarge,
    )
}