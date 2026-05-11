package com.example.quiz_game.ui.activity.onboard.destination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quiz_game.R
import com.example.quiz_game.ui.shared.animation.Orientation
import com.example.quiz_game.ui.shared.animation.shake
import com.example.quiz_game.ui.shared.component.AnimatedHtmlViewer
import com.example.quiz_game.ui.shared.component.ButtonPrimary
import com.example.quiz_game.ui.shared.component.IconButton
import com.example.quiz_game.ui.shared.component.ScreenHeader
import com.example.quiz_game.ui.shared.component.TextButton
import com.example.quiz_game.ui.shared.component.TextFieldPrimary
import com.example.quiz_game.ui.viewmodel.OnboardAction
import com.example.quiz_game.ui.viewmodel.SharedAction

@Composable
fun Form(
    modifier: Modifier = Modifier,
    sharedAction: (SharedAction) -> Unit = {},
    onboardAction: (OnboardAction) -> Unit = {},
    navController: NavController = rememberNavController(),
) {
    var usernameState by rememberSaveable { mutableStateOf("") }
    var isUsernameValid by rememberSaveable { mutableStateOf(false) }
    var hasAgreedToJuridicalStuffState by rememberSaveable { mutableStateOf(false) }
    // Single lock flag — flipped to true on submit, disables everything
    var locked by rememberSaveable { mutableStateOf(false) }
    var textfieldCleared by rememberSaveable { mutableStateOf(false) }

    // ── HTML viewer state ──────────────────────────────────────────────────
    // null  = viewer hidden; non-null = the raw resource to render
    var htmlViewerRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var htmlViewerTitle by rememberSaveable { mutableStateOf("") }

    val termsTitle   = stringResource(R.string.juridical_terms)
    val privacyTitle = stringResource(R.string.juridical_privacy)

    // Derived — never stored in state, always computed from authoritative sources
    val textfieldEnabled = !locked
    val checkboxEnabled  = !locked          // independent of username validity
    val buttonEnabled    = !locked && isUsernameValid && hasAgreedToJuridicalStuffState

    val onSubmit: () -> Unit = {
        locked = true
        onboardAction(OnboardAction.UpdateUsername(usernameState))
        onboardAction(OnboardAction.UpdateJuridicalAgreement)
    }

    val onClear: () -> Unit = {
        usernameState = ""
        textfieldCleared = true
    }

    val validationRules =
        arrayOf(
            Regex("^.{0,10}$") to
                    stringResource(R.string.onboard_name_textfield_max_characters),
            Regex("^.{4,}$") to
                    stringResource(R.string.onboard_name_textfield_min_characters),
            Regex("^[\\p{L}0-9]+$") to
                    stringResource(R.string.onboard_name_textfield_unallowed_characters)
        )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenHeightDp < configuration.screenWidthDp

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
    ) {
        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Edge-to-edge illustration (no horizontal padding) ──
            if (!isLandscape) {
                Image(
                    painter = painterResource(R.drawable.illustration_form),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            // ── Padded content below illustration ──
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(24.dp))

                // ── Header (title + subtitle only, no illustration) ──
                ScreenHeader(
                    title =
                        stringResource(
                            R.string.onboard_form_greet,
                            usernameState
                        ),
                    subtitle = stringResource(R.string.onboard_form_question),
                )

                Spacer(Modifier.height(36.dp))

                // ── Nickname text field ──
                TextFieldPrimary(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .shake(
                                !buttonEnabled,
                                Orientation.Horizontal
                            ),
                    enabled = textfieldEnabled,
                    placeholder = R.string.onboard_name_placeholder,
                    label = R.string.onboard_name_label,
                    trailingIcon =
                        if (usernameState.isEmpty()) null
                        else R.drawable.ic_erase,
                    isLast = true,
                    regex = validationRules,
                    onDone = { username -> onSubmit() },
                    onTrailingIconClicked = onClear,
                    cleared = textfieldCleared,
                    onValid = { isValid, username ->
                        isUsernameValid = isValid
                        usernameState = username
                        textfieldCleared = false
                    }
                )

                // ── Character counter hint ──
                if (usernameState.isNotEmpty() && usernameState.length < 4) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${usernameState.length}/4",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                JuridicalSection(
                    checkboxEnabled = checkboxEnabled,
                    onTermsClick = {
                        htmlViewerTitle = termsTitle
                        htmlViewerRes = R.raw.terms_of_use
                    },
                    onPrivacyClick = {
                        htmlViewerTitle = privacyTitle
                        htmlViewerRes = R.raw.privacy_policy
                    },
                    onCheckedChange = { checked ->
                        hasAgreedToJuridicalStuffState = checked
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Sticky bottom CTA ──
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it / 2 },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                ButtonPrimary(
                    onClick = onSubmit,
                    enabled = buttonEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        text = stringResource(R.string.onboard_form_submit)
                    )
                    IconButton(
                        painter =
                            painterResource(
                                R.drawable.ic_arrow_forward
                            ),
                    )
                }
            }
        }
    }

    // ── In-app HTML viewer overlay ─────────────────────────────────────────
    // Rendered on top of the form so no navigation is needed.
    AnimatedHtmlViewer(
        visible   = htmlViewerRes != null,
        rawRes    = htmlViewerRes ?: R.raw.terms_of_use,
        title     = htmlViewerTitle,
        onDismiss = { htmlViewerRes = null },
    )
}

/*
* COMPOSABLES
* */
@Composable
fun JuridicalSection(
    modifier: Modifier = Modifier,
    checkboxEnabled: Boolean = false,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onCheckedChange: (isChecked: Boolean) -> Unit = {}
) {
    var isChecked by rememberSaveable { mutableStateOf(false) }

    val toggle: () -> Unit = {
        if (checkboxEnabled) {
            isChecked = !isChecked
            onCheckedChange(isChecked)
        }
    }

    val prefix  = stringResource(R.string.juridical_consent_prefix)
    val terms   = stringResource(R.string.juridical_terms)
    val privacy = stringResource(R.string.juridical_privacy)

    // ── Animated colors ────────────────────────────────────────────────────
    val primary    = MaterialTheme.colorScheme.primary
    val outline    = MaterialTheme.colorScheme.outline
    val onSurface  = MaterialTheme.colorScheme.onSurface

    val cardBorder by animateColorAsState(
        targetValue = if (isChecked) primary else outline,
        label = "cardBorder",
    )
    val cardBg by animateColorAsState(
        targetValue = if (isChecked) primary.copy(alpha = 0.08f)
                      else MaterialTheme.colorScheme.surfaceVariant,
        label = "cardBg",
    )
    val toggleBg by animateColorAsState(
        targetValue = if (isChecked) primary else Color.Transparent,
        label = "toggleBg",
    )
    val toggleBorder by animateColorAsState(
        targetValue = if (isChecked) primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "toggleBorder",
    )
    // Spring-pop scale on the toggle icon when checked
    val toggleScale by animateFloatAsState(
        targetValue = if (isChecked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "toggleScale",
    )

    // ── Annotated consent string ───────────────────────────────────────────
    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = onSurface)) {
            append("$prefix ")
        }
        pushStringAnnotation(tag = "TERMS", annotation = "terms")
        withStyle(SpanStyle(
            color          = primary,
            fontWeight     = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
        )) { append(terms) }
        pop()
        withStyle(SpanStyle(color = onSurface)) {
            append(" & ")
        }
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(SpanStyle(
            color          = primary,
            fontWeight     = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
        )) { append(privacy) }
        pop()
    }

    // ── Card container ────────────────────────────────────────────────────
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = cardBg,
        border = BorderStroke(1.5.dp, cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick           = toggle,
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Row(
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // ── Custom circular toggle ─────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .background(toggleBg, CircleShape)
                    .border(1.5.dp, toggleBorder, CircleShape),
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimary,
                    modifier           = Modifier
                        .size(14.dp)
                        .scale(toggleScale),
                )
            }

            // ── Consent text with tappable links ──────────────────────────
            ClickableText(
                text  = annotated,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                onClick = { offset ->
                    when {
                        annotated.getStringAnnotations("TERMS",   offset, offset).isNotEmpty() -> onTermsClick()
                        annotated.getStringAnnotations("PRIVACY", offset, offset).isNotEmpty() -> onPrivacyClick()
                        else -> toggle()
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormPreview() {
    com.example.quiz_game.ui.shared.component.Preview { Form() }
}
