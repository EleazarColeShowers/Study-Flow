package com.el.studyflow.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Palette ────────────────────────────────────────────────────────────────────
private val Green400 = Color(0xFF40916C)
private val Green600 = Color(0xFF1B4332)
private val Green100 = Color(0xFFD8F3DC)

// ── Questions data ─────────────────────────────────────────────────────────────
private data class Question(
    val number: Int,
    val title: String,
    val subtitle: String,
    val options: List<String>,
    val isMultiSelect: Boolean = false
)

private val questions = listOf(
    Question(
        number = 1,
        title = "Do you have any learning differences?",
        subtitle = "Select all that apply — we'll tailor your experience",
        options = listOf("ADHD", "Dyslexia", "Autism", "None"),
        isMultiSelect = true
    ),
    Question(
        number = 2,
        title = "What's your typical attention span?",
        subtitle = "We'll structure your sessions around this",
        options = listOf("15 minutes", "30 minutes", "45 minutes", "60+ minutes")
    ),
    Question(
        number = 3,
        title = "When do you study best?",
        subtitle = "We'll send reminders at your peak time",
        options = listOf("Morning", "Afternoon", "Evening", "Night")
    ),
    Question(
        number = 4,
        title = "How much time can you dedicate daily?",
        subtitle = "Your study plan will be built around this",
        options = listOf("30 mins", "1 hour", "2 hours", "3+ hours")
    ),
    Question(
        number = 5,
        title = "What are you studying for?",
        subtitle = "We'll focus your content on what matters most",
        options = listOf("JAMB", "WAEC", "University exams", "Professional cert", "Personal growth")
    )
)

// ── Screen ─────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    onSetupComplete: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val bgColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onSetupComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {

        Spacer(Modifier.height(24.dp))

        // Header
        Text(
            text = "Set up your profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Question ${state.currentPage + 1} of ${questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = subtleText
        )

        Spacer(Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { (state.currentPage + 1) / questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = Green400,
            trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Green100
        )

        Spacer(Modifier.height(36.dp))

        // Animated question content
        AnimatedContent(
            targetState = state.currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                }
            },
            modifier = Modifier.weight(1f),
            label = "question"
        ) { page ->
            val question = questions[page]
            QuestionContent(
                question = question,
                selectedSingle = when (page) {
                    1 -> state.selectedAttentionSpan
                    2 -> state.selectedStudyTime
                    3 -> state.selectedDailyDedication
                    4 -> state.selectedStudyGoal
                    else -> ""
                },
                selectedMulti = state.selectedLearningDiffs,
                onSingleSelect = { option ->
                    when (page) {
                        1 -> viewModel.selectAttentionSpan(option)
                        2 -> viewModel.selectStudyTime(option)
                        3 -> viewModel.selectDailyDedication(option)
                        4 -> viewModel.selectStudyGoal(option)
                    }
                },
                onMultiToggle = viewModel::toggleLearningDiff,
                isDark = isDark,
                textColor = textColor,
                subtleText = subtleText
            )
        }

        // Back / Next buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.currentPage > 0) {
                OutlinedButton(
                    onClick = viewModel::previousPage,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Green400),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green400)
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Button(
                onClick = viewModel::nextPage,
                enabled = state.canGoNext,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green400,
                    contentColor = Color.White,
                    disabledContainerColor = Green400.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (state.currentPage == questions.lastIndex) "Finish ✓" else "Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Question content ───────────────────────────────────────────────────────────
@Composable
private fun QuestionContent(
    question: Question,
    selectedSingle: String,
    selectedMulti: Set<String>,
    onSingleSelect: (String) -> Unit,
    onMultiToggle: (String) -> Unit,
    isDark: Boolean,
    textColor: Color,
    subtleText: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = question.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
            lineHeight = 32.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = question.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = subtleText
        )

        Spacer(Modifier.height(28.dp))

        // Use a 2-column grid for all questions
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(question.options) { option ->
                val isSelected = if (question.isMultiSelect) {
                    option in selectedMulti
                } else {
                    option == selectedSingle
                }

                OptionCard(
                    text = option,
                    isSelected = isSelected,
                    isDark = isDark,
                    onClick = {
                        if (question.isMultiSelect) onMultiToggle(option)
                        else onSingleSelect(option)
                    }
                )
            }
        }
    }
}

// ── Option card ────────────────────────────────────────────────────────────────
@Composable
private fun OptionCard(
    text: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgSelected = Green400
    val bgUnselected = if (isDark) Color(0xFF1A1A1A) else Color.White
    val borderColor = if (isSelected) Green400
    else if (isDark) Color.White.copy(alpha = 0.1f)
    else Color(0xFFDDDDDD)
    val textColor = when {
        isSelected -> Color.White
        isDark -> Color.White
        else -> Color(0xFF1A1A1A)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) bgSelected else bgUnselected)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}