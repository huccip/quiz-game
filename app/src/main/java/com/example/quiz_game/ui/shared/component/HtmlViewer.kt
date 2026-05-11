package com.example.quiz_game.ui.shared.component

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Full-screen in-app HTML renderer.
 *
 * Reads [rawRes] from res/raw/, loads it into a [WebView], and presents it
 * with a minimal top bar containing a back button and [title].
 *
 * Wrap in [AnimatedVisibility] at the call site to control show/hide with
 * a slide animation (see usage in Form.kt).
 */
@Composable
fun HtmlViewer(
    @RawRes rawRes: Int,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Read the raw HTML file once; raw resources don't change at runtime.
    val htmlContent = remember(rawRes) {
        context.resources.openRawResource(rawRes)
            .bufferedReader()
            .use { it.readText() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            TextBig(text = title, fontWeight = FontWeight.SemiBold)
        }

        // ── WebView ──────────────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    // Keep all links inside the WebView — no external browser.
                    webViewClient = WebViewClient()
                    settings.apply {
                        // Allow relative links within the loaded HTML to resolve
                        // but no need for JavaScript for static legal documents.
                        javaScriptEnabled = false
                        builtInZoomControls = false
                    }
                }
            },
            update = { webView ->
                // loadDataWithBaseURL lets relative CSS/asset paths resolve
                // correctly if added later; "null" base is fine for plain HTML.
                webView.loadDataWithBaseURL(
                    null,
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        )
    }
}

/**
 * Animated wrapper that slides [HtmlViewer] in from the bottom when [visible]
 * is true and slides it back out when false.
 *
 * Usage:
 * ```
 * AnimatedHtmlViewer(
 *     visible  = openHtmlRes != null,
 *     rawRes   = openHtmlRes ?: R.raw.terms_of_use,
 *     title    = openHtmlTitle,
 *     onDismiss = { openHtmlRes = null },
 * )
 * ```
 */
@Composable
fun AnimatedHtmlViewer(
    visible: Boolean,
    @RawRes rawRes: Int,
    title: String,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit  = fadeOut() + slideOutVertically { it },
    ) {
        HtmlViewer(
            rawRes    = rawRes,
            title     = title,
            onDismiss = onDismiss,
        )
    }
}
