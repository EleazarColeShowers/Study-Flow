package com.el.planora.ui.checkin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.planora.domain.model.SessionQuality

@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val bgColor     = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg      = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val inputBg     = if (isDark) Color(0xFF161616) else Color(0xFFF0F0F0)
    val textColor   = if (isDark) Color(0xFFEEEEEE) else Color(0xFF111111)
    val subtleText  = if (isDark) Color(0xFF888888) else Color(0xFF888888)
    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFDDEDE5)
    val accentGreen = Color(0xFF40916C)
    val userBubble  = accentGreen
    val botBubble   = if (isDark) Color(0xFF1E1E1E) else Color(0xFFEAF4EE)

    val state by viewModel.checkInState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Scroll to bottom whenever messages change
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ── Top bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
            }

            // Bot avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentGreen),
                contentAlignment = Alignment.Center
            ) {
                Text("SF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(Modifier.width(10.dp))

            Column {
                Text(
                    text = "Planora",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentGreen)
                    )
                    Text(
                        text = "Daily Check-in",
                        style = MaterialTheme.typography.labelSmall,
                        color = subtleText
                    )
                }
            }
        }

        // ── Message list ─────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
                ) {
                    ChatBubble(
                        message = message,
                        userBubble = userBubble,
                        botBubble = botBubble,
                        textColor = textColor,
                        subtleText = subtleText,
                        accentGreen = accentGreen
                    )
                }
            }

            // Typing indicator
            if (state.isBotTyping) {
                item {
                    TypingIndicator(botBubble = botBubble, dotColor = subtleText)
                }
            }
        }

        // ── Quick reply chips ────────────────────────────────────────
        if (!state.isCompleted && !state.isBotTyping) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose your response",
                    style = MaterialTheme.typography.labelSmall,
                    color = subtleText,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QualityChip(
                        label = "Great! 🎉",
                        modifier = Modifier.weight(1f),
                        selected = state.selectedQuality == SessionQuality.GREAT,
                        accentGreen = accentGreen,
                        borderColor = borderColor,
                        textColor = textColor,
                        onClick = { viewModel.submitCheckIn(SessionQuality.GREAT) }
                    )
                    QualityChip(
                        label = "Okay 👍",
                        modifier = Modifier.weight(1f),
                        selected = state.selectedQuality == SessionQuality.OKAY,
                        accentGreen = accentGreen,
                        borderColor = borderColor,
                        textColor = textColor,
                        onClick = { viewModel.submitCheckIn(SessionQuality.OKAY) }
                    )
                    QualityChip(
                        label = "Struggled 😓",
                        modifier = Modifier.weight(1f),
                        selected = state.selectedQuality == SessionQuality.STRUGGLED,
                        accentGreen = accentGreen,
                        borderColor = borderColor,
                        textColor = textColor,
                        onClick = { viewModel.submitCheckIn(SessionQuality.STRUGGLED) }
                    )
                }
            }
        }

        // Saving indicator at very bottom
        if (state.isSubmitting) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = accentGreen,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Saving...", style = MaterialTheme.typography.labelSmall, color = subtleText)
            }
        }
    }
}

// ── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
    userBubble: Color,
    botBubble: Color,
    textColor: Color,
    subtleText: Color,
    accentGreen: Color
) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Bot avatar on left
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentGreen),
                contentAlignment = Alignment.Center
            ) {
                Text("SF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) userBubble else botBubble)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isUser) Color.White else textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = subtleText,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TypingIndicator(botBubble: Color, dotColor: Color) {
    Row(verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF40916C)),
            contentAlignment = Alignment.Center
        ) {
            Text("SF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .background(botBubble)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityChip(
    label: String,
    selected: Boolean,
    accentGreen: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) accentGreen else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (selected) accentGreen else borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else textColor,
            fontSize = 12.sp
        )
    }
}