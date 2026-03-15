package com.el.planora.ui.checkin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.planora.data.remote.model.CheckInSummaryDto

private val Green400 = Color(0xFF40916C)

@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    val isDark      = isSystemInDarkTheme()
    val bgColor     = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg      = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val inputBg     = if (isDark) Color(0xFF252525) else Color(0xFFF0F0F0)
    val textColor   = if (isDark) Color(0xFFEEEEEE) else Color(0xFF111111)
    val subtleText  = Color(0xFF888888)
    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFDDEDE5)
    val userBubble  = Green400
    val botBubble   = if (isDark) Color(0xFF1E1E1E) else Color(0xFFEAF4EE)

    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.isBotTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                state.messages.size + if (state.isBotTyping) 1 else 0
            )
        }
    }

    // imePadding on the outermost container — pushes everything up when keyboard opens
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .imePadding()
    ) {

        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.onDismiss()
                onDismiss()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Green400),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Planora Coach",
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
                            .background(
                                when (state.checkInStage) {
                                    CheckInStage.PINGING -> Color(0xFFFFA500)
                                    else -> Green400
                                }
                            )
                    )
                    Text(
                        text = when {
                            state.isLoadingProfile -> "Loading..."
                            state.mode == ChatMode.CHECKIN &&
                                    state.checkInStage == CheckInStage.PINGING -> "Connecting..."
                            state.mode == ChatMode.CHECKIN -> "Check-in mode"
                            state.subjectName.isNotEmpty() -> state.subjectName
                            else -> "Study Q&A"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = subtleText
                    )
                }
            }

            if (state.mode == ChatMode.QA && !state.isLoadingProfile) {
                OutlinedButton(
                    onClick = { viewModel.startCheckIn() },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green400),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Green400),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Check In",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (state.mode == ChatMode.CHECKIN &&
                state.checkInStage != CheckInStage.PINGING) {
                OutlinedButton(
                    onClick = {
                        viewModel.onDismiss()
                        viewModel.reset()
                    },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE53935)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Mode banner
        if (state.mode == ChatMode.CHECKIN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Green400.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📋 Check-in in progress — logging your session",
                    style = MaterialTheme.typography.labelSmall,
                    color = Green400,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── Message list — takes all remaining space ───────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
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
                        subtleText = subtleText
                    )
                }
            }

            if (state.isBotTyping) {
                item(key = "typing") {
                    TypingIndicator(botBubble = botBubble, dotColor = subtleText)
                }
            }

            if (state.checkInStage == CheckInStage.COMPLETE && state.summary != null) {
                item(key = "summary") {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
                    ) {
                        SummaryCard(
                            summary = state.summary!!,
                            cardBg = cardBg,
                            textColor = textColor,
                            subtleText = subtleText,
                            borderColor = borderColor
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Input bar — pinned at bottom, always visible ───────────────────────
        HorizontalDivider(color = borderColor, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = state.inputText,
                onValueChange = viewModel::updateInput,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = {
                    Text(
                        text = when {
                            state.mode == ChatMode.CHECKIN &&
                                    state.checkInStage == CheckInStage.QUIZ -> "Type your answer..."
                            state.mode == ChatMode.CHECKIN -> "Reply to coach..."
                            else -> "Ask about ${state.subjectName.ifEmpty { "your subject" }}..."
                        },
                        color = subtleText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = inputBg,
                    focusedContainerColor = inputBg,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.inputText.isNotBlank() && !state.isBotTyping)
                            Green400 else Green400.copy(alpha = 0.35f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = state.inputText.isNotBlank() && !state.isBotTyping
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Summary Card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    summary: CheckInSummaryDto,
    cardBg: Color,
    textColor: Color,
    subtleText: Color,
    borderColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Session Summary",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Green400
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryStatItem("Quiz Score", "${summary.quizScore}%", textColor, subtleText)
            SummaryStatItem("Correct", "${summary.correctAnswers}/${summary.totalQuestions}", textColor, subtleText)
            SummaryStatItem("Understanding", summary.understanding.replaceFirstChar { it.uppercase() }, textColor, subtleText)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Green400.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Next Review", style = MaterialTheme.typography.bodySmall, color = subtleText)
            Text(
                summary.nextReviewDate,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Green400
            )
        }
        Text(
            summary.encouragement,
            style = MaterialTheme.typography.bodySmall,
            color = subtleText,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String, textColor: Color, subtleText: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Green400)
        Text(label, style = MaterialTheme.typography.labelSmall, color = subtleText)
    }
}

// ── Chat Bubble ───────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
    userBubble: Color,
    botBubble: Color,
    textColor: Color,
    subtleText: Color
) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Green400),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
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

// ── Typing Indicator ──────────────────────────────────────────────────────────

@Composable
private fun TypingIndicator(botBubble: Color, dotColor: Color) {
    Row(verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Green400),
            contentAlignment = Alignment.Center
        ) {
            Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .background(botBubble)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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