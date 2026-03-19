package com.el.planora.ui.subject

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val Green400  = Color(0xFF40916C)
private val Green100  = Color(0xFFD8F3DC)
private val Blue400   = Color(0xFF3B82F6)
private val Amber400  = Color(0xFFFBBF24)

@Composable
fun SubjectDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubjectDetailViewModel = hiltViewModel()
) {
    val isDark      = isSystemInDarkTheme()
    val bgColor     = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg      = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val textColor   = if (isDark) Color(0xFFEEEEEE) else Color(0xFF111111)
    val subtleText  = if (isDark) Color(0xFF888888) else Color(0xFF666666)
    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE2F0E8)
    val surfaceBg   = if (isDark) Color(0xFF161616) else Color(0xFFEAF4EE)

    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            // ── Top bar ───────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.subjectName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = if (state.daysToExam > 0 && state.daysToExam < 999)
                                "${state.daysToExam} days to exam" else "No deadline",
                            style = MaterialTheme.typography.labelSmall,
                            color = Green400
                        )
                    }
                }
            }

            // ── Loading ───────────────────────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Green400)
                            Text(
                                text = "Building your study plan...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtleText
                            )
                        }
                    }
                }
                return@LazyColumn
            }

            // ── Stats row ─────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard(
                        modifier    = Modifier.weight(1f),
                        icon        = Icons.Default.Schedule,
                        value       = state.sessionLength.replace(" per session", ""),
                        label       = "Per session",
                        iconTint    = Green400,
                        bg          = surfaceBg,
                        textColor   = textColor,
                        subtleText  = subtleText
                    )
                    MiniStatCard(
                        modifier    = Modifier.weight(1f),
                        icon        = Icons.Default.LightMode,
                        value       = state.dailySessions.replace(" per day", ""),
                        label       = "Daily sessions",
                        iconTint    = Amber400,
                        bg          = surfaceBg,
                        textColor   = textColor,
                        subtleText  = subtleText
                    )
                    MiniStatCard(
                        modifier    = Modifier.weight(1f),
                        icon        = Icons.Default.CheckCircle,
                        value       = "${state.totalCompleted}",
                        label       = "Completed",
                        iconTint    = Blue400,
                        bg          = surfaceBg,
                        textColor   = textColor,
                        subtleText  = subtleText
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            // ── Today's Schedule ──────────────────────────────────────────────
            item {
                SectionHeader(
                    title      = "Today's Focus",
                    subtitle   = state.today,
                    textColor  = textColor,
                    subtleText = subtleText
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.todaySessions.isEmpty()) {
                item {
                    EmptyCard("No sessions scheduled yet", subtleText, cardBg, borderColor)
                }
            } else {
                items(state.todaySessions) { session ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }
                    ) {
                        SessionCard(
                            session     = session,
                            onToggle    = { viewModel.toggleSession(session) },
                            cardBg      = cardBg,
                            borderColor = borderColor,
                            textColor   = textColor,
                            subtleText  = subtleText
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }

            // ── Study Techniques ──────────────────────────────────────────────
            item {
                SectionHeader(
                    title      = "Study Techniques",
                    subtitle   = "AI recommended for you",
                    textColor  = textColor,
                    subtleText = subtleText
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.techniques.isEmpty()) {
                item {
                    EmptyCard("No techniques loaded yet", subtleText, cardBg, borderColor)
                }
            } else {
                items(state.techniques) { technique ->
                    TechniqueCard(
                        //TODO: remember to check out name and description
                        name        = technique.name ?: "",
                        description = technique.howTo ?: "",
                        cardBg      = cardBg,
                        borderColor = borderColor,
                        textColor   = textColor,
                        subtleText  = subtleText
                    )
                }
            }

            item { Spacer(Modifier.height(28.dp)) }

            // ── Flashcards Due ────────────────────────────────────────────────
            if (state.dueFlashcards.isNotEmpty()) {
                item {
                    SectionHeader(
                        title      = "Flashcards Due",
                        subtitle   = "${state.dueFlashcards.size} cards to review",
                        textColor  = textColor,
                        subtleText = subtleText
                    )
                    Spacer(Modifier.height(12.dp))
                }

                items(state.dueFlashcards) { card ->
                    FlashcardPreviewCard(
                        card        = card,
                        cardBg      = cardBg,
                        borderColor = borderColor,
                        textColor   = textColor,
                        subtleText  = subtleText
                    )
                }

                item { Spacer(Modifier.height(28.dp)) }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String, subtitle: String,
    textColor: Color, subtleText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Green400
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = subtleText
        )
    }
}

// ── Mini Stat Card ────────────────────────────────────────────────────────────

@Composable
private fun MiniStatCard(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color,
    bg: Color,
    textColor: Color,
    subtleText: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 13.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = subtleText,
            fontSize = 10.sp
        )
    }
}

// ── Session Card ──────────────────────────────────────────────────────────────

@Composable
private fun SessionCard(
    session: StudySession,
    onToggle: () -> Unit,
    cardBg: Color,
    borderColor: Color,
    textColor: Color,
    subtleText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onToggle() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Icon(
            imageVector = if (session.isCompleted)
                Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (session.isCompleted) Green400 else subtleText,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Time range
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${session.startTime} – ${session.endTime}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Green400,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Green400.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${session.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = Green400,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.height(3.dp))

            // Session title
            Text(
                text = session.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (session.isCompleted) subtleText else textColor
            )

            Spacer(Modifier.height(4.dp))

            // Technique tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Green400.copy(alpha = 0.08f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = session.technique,
                    style = MaterialTheme.typography.labelSmall,
                    color = Green400,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Technique Card ────────────────────────────────────────────────────────────

@Composable
private fun TechniqueCard(
    name: String,
    description: String,
    cardBg: Color,
    borderColor: Color,
    textColor: Color,
    subtleText: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { expanded = !expanded }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Green400.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Style,
                    contentDescription = null,
                    tint = Green400,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "▲" else "▼",
                style = MaterialTheme.typography.labelSmall,
                color = subtleText
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = borderColor, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtleText,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ── Flashcard Preview Card ────────────────────────────────────────────────────

@Composable
private fun FlashcardPreviewCard(
    card: DueFlashcard,
    cardBg: Color,
    borderColor: Color,
    textColor: Color,
    subtleText: Color
) {
    var flipped by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { flipped = !flipped }
            .padding(16.dp)
    ) {
        Text(
            text = if (flipped) "Answer" else "Question",
            style = MaterialTheme.typography.labelSmall,
            color = Green400,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (flipped) card.back else card.front,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
        if (!flipped) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap to reveal answer",
                style = MaterialTheme.typography.labelSmall,
                color = subtleText
            )
        }
    }
}

// ── Empty Card ────────────────────────────────────────────────────────────────

@Composable
private fun EmptyCard(message: String, subtleText: Color, cardBg: Color, borderColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = subtleText)
    }
}