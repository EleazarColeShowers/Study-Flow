package com.el.planora.ui.techniques

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8F3ED)
    val textColor = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)
    val accentGreen = Color(0xFF40916C)

    val techniques by viewModel.techniques.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 16.dp,
                bottom = 80.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                Text(
                    text = "Your Study Techniques",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Why these work for you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtleText
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(techniques) { technique ->
                TechniqueCard(
                    technique = technique,
                    cardBg = cardBg,
                    textColor = textColor,
                    subtleText = subtleText,
                    accentGreen = accentGreen,
                    onToggle = { viewModel.toggleTechnique(technique.id) },
                    onPlayVideo = { viewModel.playVideo(technique.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
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
            .clickable { onToggle() }
            .animateContentSize()
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(technique.iconBackgroundColor.removePrefix("#").toLong(16).toInt()).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = technique.icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // Title and description
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

                // Expand icon
                Icon(
                    imageVector = if (technique.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle",
                    tint = accentGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Expanded content
            if (technique.isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = technique.whyItWorks,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(16.dp))

                // How to apply section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Color(technique.iconBackgroundColor.removePrefix("#").toLong(16).toInt()).copy(alpha = 0.1f)
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = technique.howToApply,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video button (if available)
                if (technique.videoUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color(technique.iconBackgroundColor.removePrefix("#").toLong(16).toInt()).copy(alpha = 0.2f)
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
                                tint = Color(technique.iconBackgroundColor.removePrefix("#").toLong(16).toInt()),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Mastering the ${technique.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(technique.iconBackgroundColor.removePrefix("#").toLong(16).toInt()),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}