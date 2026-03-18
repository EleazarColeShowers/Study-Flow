package com.el.planora.ui.dashboard

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val Green400 = Color(0xFF40916C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectSheet(
    onDismiss: () -> Unit,
    onSubjectAdded: () -> Unit,
    viewModel: AddSubjectViewModel = hiltViewModel()
) {
    val isDark      = isSystemInDarkTheme()
    val bgColor     = if (isDark) Color(0xFF121212) else Color(0xFFFFFFFF)
    val cardBg      = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF4FBF7)
    val textColor   = if (isDark) Color(0xFFEEEEEE) else Color(0xFF111111)
    val subtleText  = if (isDark) Color(0xFF888888) else Color(0xFF666666)
    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFDDEDE5)

    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = bgColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(borderColor)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Text(
                text = "Add a Subject",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "We'll build a personalised study plan around it",
                style = MaterialTheme.typography.bodySmall,
                color = subtleText,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // ── Subject Name ──────────────────────────────────────────────────
            FieldLabel("Subject Name", textColor)
            OutlinedTextField(
                value = state.subjectName,
                onValueChange = viewModel::onSubjectNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Constitutional Law", color = subtleText) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green400,
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = Green400
                )
            )

            Spacer(Modifier.height(20.dp))

            // ── Content Type ──────────────────────────────────────────────────
            FieldLabel("Content Type", textColor)
            OptionChipsRow(
                options = listOf(
                    "theory"      to "Theory",
                    "calculation" to "Calculation",
                    "mixed"       to "Mixed",
                    "practical"   to "Practical"
                ),
                selected = state.contentType,
                onSelect = viewModel::onContentTypeChange,
                cardBg = cardBg,
                borderColor = borderColor,
                textColor = textColor
            )

            Spacer(Modifier.height(20.dp))

            // ── Memory Load ───────────────────────────────────────────────────
            FieldLabel("Memory Load", textColor)
            OptionChipsRow(
                options = listOf(
                    "high"   to "High",
                    "medium" to "Medium",
                    "low"    to "Low"
                ),
                selected = state.memoryLoad,
                onSelect = viewModel::onMemoryLoadChange,
                cardBg = cardBg,
                borderColor = borderColor,
                textColor = textColor
            )

            Spacer(Modifier.height(20.dp))

            // ── Difficulty ────────────────────────────────────────────────────
            FieldLabel("Difficulty", textColor)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                (1..5).forEach { level ->
                    val isSelected = state.difficulty == level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Green400 else cardBg)
                            .border(
                                1.5.dp,
                                if (isSelected) Green400 else borderColor,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple()
                            ) { viewModel.onDifficultyChange(level) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else subtleText,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Easy", style = MaterialTheme.typography.labelSmall, color = subtleText)
                Text("Hard", style = MaterialTheme.typography.labelSmall, color = subtleText)
            }

            Spacer(Modifier.height(20.dp))

            // ── Deadline ──────────────────────────────────────────────────────
            FieldLabel("Deadline", textColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (state.hasDeadline) "I have an exam or deadline" else "No deadline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Switch(
                    checked = state.hasDeadline,
                    onCheckedChange = viewModel::onHasDeadlineChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Green400,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = borderColor
                    )
                )
            }

            if (state.hasDeadline) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.daysToExam,
                    onValueChange = viewModel::onDaysToExamChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("How many days until your exam?", color = subtleText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green400,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = Green400
                    ),
                    suffix = { Text("days", color = subtleText) }
                )
            }

            // ── Error ─────────────────────────────────────────────────────────
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE53935)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Submit button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.submit {
                        onSubjectAdded()
                        onDismiss()
                    }
                },
                enabled = state.canSubmit && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green400,
                    contentColor = Color.White,
                    disabledContainerColor = Green400.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Subject",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Field label ───────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String, textColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// ── Option chips row ──────────────────────────────────────────────────────────

@Composable
private fun OptionChipsRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    cardBg: Color,
    borderColor: Color,
    textColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Green400 else cardBg)
                    .border(
                        1.5.dp,
                        if (isSelected) Green400 else borderColor,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ) { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else textColor
                )
            }
        }
    }
}