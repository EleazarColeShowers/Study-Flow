package com.el.planora.ui.techniques

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.planora.domain.model.StudyTechnique

@Composable
fun TechniquesScreen(
    viewModel: TechniquesViewModel = hiltViewModel()
) {
    val isDark      = isSystemInDarkTheme()
    val bgColor     = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg      = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8F3ED)
    val textColor   = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText  = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)
    val accentGreen = Color(0xFF40916C)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when {
            // ── Loading ───────────────────────────────────────────────────────
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = accentGreen)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Getting your personalised techniques...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtleText
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        top = 16.dp,
                        bottom = 80.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                ) {
                    item {
                        // Header
                        Text(
                            text = "Your Study Techniques",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentGreen
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (uiState.isFromApi)
                                "Personalised for ${uiState.subjectName.ifEmpty { "you" }}"
                            else
                                "Why these work for you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtleText
                        )

                        // Session info banner — only shown when API data loaded
                        if (uiState.isFromApi &&
                            (uiState.sessionLength.isNotEmpty() || uiState.dailySessions.isNotEmpty())) {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (uiState.sessionLength.isNotEmpty()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = uiState.sessionLength,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accentGreen
                                        )
                                        Text(
                                            text = "per session",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = subtleText
                                        )
                                    }
                                }
                                if (uiState.dailySessions.isNotEmpty()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = uiState.dailySessions,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accentGreen
                                        )
                                        Text(
                                            text = "per day",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = subtleText
                                        )
                                    }
                                }
                            }
                        }

                        // Error banner — shown when fallback is active
                        if (!uiState.isFromApi && uiState.error != null) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE53935).copy(alpha = 0.08f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Showing general techniques — AI unavailable",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE53935),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { viewModel.retry() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentGreen
                                    ),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .padding(start = 8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 12.dp, vertical = 0.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }

                    items(uiState.techniques) { technique ->
                        TechniqueCard(
                            technique = technique,
                            cardBg = cardBg,
                            textColor = textColor,
                            subtleText = subtleText,
                            accentGreen = accentGreen,
                            onToggle = { viewModel.toggleTechnique(technique.id) },
                            onPlayVideo = { viewModel.playVideo(technique.id) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TechniqueCard(
    technique: StudyTechnique,
    cardBg: Color,
    textColor: Color,
    subtleText: Color,
    accentGreen: Color,
    onToggle: () -> Unit,
    onPlayVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onToggle() }
            .animateContentSize()
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Color(
                                technique.iconBackgroundColor
                                    .removePrefix("#")
                                    .toLong(16)
                                    .toInt()
                            ).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = technique.icon, style = MaterialTheme.typography.headlineSmall)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = technique.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = technique.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtleText,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = if (technique.isExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = "Toggle",
                    tint = accentGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (technique.isExpanded) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = technique.whyItWorks,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Color(
                                technique.iconBackgroundColor
                                    .removePrefix("#")
                                    .toLong(16)
                                    .toInt()
                            ).copy(alpha = 0.1f)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "How to apply",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = technique.howToApply,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                        )
                    }
                }

                if (technique.videoUrl != null) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color(
                                    technique.iconBackgroundColor
                                        .removePrefix("#")
                                        .toLong(16)
                                        .toInt()
                                ).copy(alpha = 0.2f)
                            )
                            .clickable { onPlayVideo() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(
                                    technique.iconBackgroundColor
                                        .removePrefix("#")
                                        .toLong(16)
                                        .toInt()
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Watch: Mastering ${technique.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(
                                    technique.iconBackgroundColor
                                        .removePrefix("#")
                                        .toLong(16)
                                        .toInt()
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}