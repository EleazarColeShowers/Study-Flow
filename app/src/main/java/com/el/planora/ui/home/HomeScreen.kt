package com.el.planora.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
private val Green100 = Color(0xFFD8F3DC)

// ── Question types ─────────────────────────────────────────────────────────────
enum class QuestionType {
    OPTIONS,       // 2-column grid cards
    OPTIONS_FLOW,  // wrapping chips (for longer or more options)
    TEXT           // free text input
}

// ── Question model ─────────────────────────────────────────────────────────────
data class Question(
    val id: String,
    val title: String,
    val subtitle: String,
    val options: List<String> = emptyList(),
    val isMultiSelect: Boolean = false,
    val type: QuestionType = QuestionType.OPTIONS,
    val isOptional: Boolean = false
)

// ── Question definitions ───────────────────────────────────────────────────────

// General — shown to everyone
val GENERAL_QUESTIONS = listOf(
    Question(
        id = "user_type",
        title = "What best describes you?",
        subtitle = "We'll personalise your experience around your goals",
        options = listOf(
            "Uni Student",
            "Language Learner",
            "Certification Candidate",
            "Studying a specific topic"
        )
    ),
    Question(
        id = "age",
        title = "How old are you?",
        subtitle = "Helps us tailor content and pacing",
        options = listOf("Under 16", "16–18", "19–24", "25–34", "35+")
    ),
    Question(
        id = "hours_per_day",
        title = "How many hours a day can you realistically study?",
        subtitle = "We'll build your plan around what's actually achievable",
        options = listOf("Less than 1 hour", "1–2 hours", "2–3 hours", "3+ hours")
    ),
    Question(
        id = "best_time",
        title = "What time of day do you focus best?",
        subtitle = "We'll schedule sessions and reminders around this",
        options = listOf("Morning", "Afternoon", "Evening", "Late Night")
    ),
    Question(
        id = "sleep",
        title = "How many hours of sleep do you usually get?",
        subtitle = "Sleep affects how well you retain what you study",
        options = listOf("Less than 5 hrs", "5–6 hrs", "7–8 hrs", "8+ hrs")
    ),
    Question(
        id = "study_location",
        title = "Where do you usually study?",
        subtitle = "Knowing your environment helps us set realistic expectations",
        options = listOf(
            "Home – quiet & dedicated",
            "Home – noisy or distracting",
            "Library or campus",
            "Cafés or public spaces",
            "It varies"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "learning_diffs",
        title = "Do any of these sound like you?",
        subtitle = "Select all that apply — we'll tailor your experience",
        options = listOf(
            "Hard to focus or sit still",
            "Struggle with reading or spelling",
            "Prefer clear structure; changes stress me",
            "Often anxious or overwhelmed studying",
            "None of these",
            "I'd rather not say"
        ),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    )
)

// Conditional follow-ups based on learning differences
val FOCUS_FOLLOWUP = Question(
    id = "focus_detail",
    title = "When you lose focus, what usually happens?",
    subtitle = "This helps us design better session structures for you",
    options = listOf(
        "My mind jumps to other thoughts",
        "I start tasks but struggle to finish",
        "I get bored quickly",
        "I get distracted by my surroundings"
    ),
    type = QuestionType.OPTIONS_FLOW
)

val READING_FOLLOWUP = Question(
    id = "reading_detail",
    title = "Which of these feels hardest for you?",
    subtitle = "We'll adjust how we present content",
    options = listOf(
        "Reading long blocks of text",
        "Spelling and writing clearly",
        "Remembering sequences of info",
        "Processing info at the same speed as others"
    ),
    type = QuestionType.OPTIONS_FLOW
)

val STRUCTURE_FOLLOWUP = Question(
    id = "structure_detail",
    title = "How do you prefer your study routine to be structured?",
    subtitle = "We'll build your plan to match",
    options = listOf(
        "I like knowing exactly what I'll do and when",
        "I prefer going deep on one topic at a time",
        "Unexpected changes really throw me off",
        "I need clear goals to feel progress"
    ),
    isMultiSelect = true,
    type = QuestionType.OPTIONS_FLOW
)

val ANXIETY_FOLLOWUP = Question(
    id = "anxiety_detail",
    title = "When does studying feel most overwhelming?",
    subtitle = "We'll design your plan to reduce these moments",
    options = listOf(
        "Too much to cover, don't know where to start",
        "Feeling behind compared to others",
        "Right before exams or deadlines",
        "Almost always, regardless of the situation"
    ),
    type = QuestionType.OPTIONS_FLOW
)

// Uni Student branch
val UNI_QUESTIONS = listOf(
    Question(
        id = "uni_field",
        title = "What's your field of study?",
        subtitle = "We'll make sure resources and techniques are relevant",
        options = listOf(
            "Science & Engineering",
            "Medicine & Health",
            "Law & Social Sciences",
            "Business & Economics",
            "Arts & Humanities",
            "Other"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "uni_year",
        title = "What year are you in?",
        subtitle = "Helps us understand your workload and level",
        options = listOf("Year 1", "Year 2", "Year 3", "Year 4+", "Postgraduate")
    ),
    Question(
        id = "uni_subjects",
        title = "List the subjects you're currently taking",
        subtitle = "Type each subject — we'll tag them for you",
        type = QuestionType.TEXT
    ),
    Question(
        id = "uni_content_type",
        title = "What type of content are your subjects mostly?",
        subtitle = "Select the best fit for your main subjects",
        options = listOf(
            "Theory-heavy (essays, concepts, memorisation)",
            "Calculation-heavy (maths, problem solving)",
            "Mixed (theory + application)",
            "Practical / skill-based"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "uni_memory_load",
        title = "How much do your subjects require you to memorise?",
        subtitle = "We'll adjust your revision strategy accordingly",
        options = listOf(
            "High – lots of definitions, dates, formulas",
            "Medium",
            "Low – understanding matters more"
        )
    ),
    Question(
        id = "uni_exams",
        title = "Do you have exams coming up?",
        subtitle = "We'll prioritise accordingly",
        options = listOf("Yes, very soon", "Yes, in a few months", "No exams right now")
    ),
    Question(
        id = "uni_struggles",
        title = "What's your biggest study struggle?",
        subtitle = "Select all that apply",
        options = listOf(
            "Staying focused",
            "Remembering information",
            "Understanding concepts",
            "Managing time",
            "Motivation",
            "I know what to study but not how"
        ),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "uni_attention",
        title = "How long can you focus before your mind wanders?",
        subtitle = "Your sessions will be broken into chunks around this",
        options = listOf("Under 10 mins", "10–20 mins", "20–45 mins", "45+ mins")
    ),
    Question(
        id = "uni_learning_style",
        title = "When you understood something really well, how did it happen?",
        subtitle = "We'll lean into what works for you",
        options = listOf(
            "I read it carefully in my own time",
            "Someone explained it or I watched it explained",
            "I heard it discussed or talked it through",
            "I tried it and figured it out through practice"
        ),
        type = QuestionType.OPTIONS_FLOW
    )
)

// Language Learner branch
val LANGUAGE_QUESTIONS = listOf(
    Question(
        id = "lang_language",
        title = "Which language are you learning?",
        subtitle = "Type the language name",
        type = QuestionType.TEXT
    ),
    Question(
        id = "lang_level",
        title = "What is your current level?",
        subtitle = "Be honest — we'll meet you where you are",
        options = listOf(
            "Complete beginner",
            "Basic understanding",
            "Conversational",
            "Advanced",
            "Near fluent"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "lang_goal",
        title = "What is your goal?",
        subtitle = "This shapes everything we recommend",
        options = listOf(
            "Travel & basic communication",
            "Career & professional use",
            "Academic use",
            "Full fluency",
            "Passing a language exam (IELTS, DELF…)"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "lang_skills",
        title = "Which skills do you most want to improve?",
        subtitle = "Select all that apply",
        options = listOf("Speaking", "Listening", "Reading", "Writing", "Vocabulary", "Grammar"),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "lang_challenges",
        title = "What's your biggest challenge with this language?",
        subtitle = "Select all that apply",
        options = listOf(
            "Vocabulary doesn't stick",
            "I understand it but can't produce it",
            "Grammar rules confuse me",
            "Not enough time to practise",
            "I get nervous actually using it"
        ),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "lang_method",
        title = "How are you currently learning?",
        subtitle = "Select all that apply",
        options = listOf("Self-study", "Classes", "App (Duolingo etc.)", "Immersion / Mix"),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "lang_prev_language",
        title = "Have you ever learned a second language before?",
        subtitle = "Helps us understand your language-learning background",
        options = listOf(
            "No",
            "Yes – basic phrases only",
            "Yes – conversational",
            "Yes – fluent or near fluent"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "lang_attention",
        title = "How long can you focus before your mind wanders?",
        subtitle = "Your sessions will be broken into chunks around this",
        options = listOf("Under 10 mins", "10–20 mins", "20–45 mins", "45+ mins")
    )
)

// Certification Candidate branch
val CERT_QUESTIONS = listOf(
    Question(
        id = "cert_name",
        title = "Which certification are you preparing for?",
        subtitle = "Type the exam name (e.g. AWS, PMP, CFA, ACCA…)",
        type = QuestionType.TEXT
    ),
    Question(
        id = "cert_date",
        title = "What is your exam date or target month?",
        subtitle = "We'll work backwards to build your plan",
        options = listOf("Within 1 month", "1–3 months", "3–6 months", "6+ months", "Not set yet"),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "cert_attempts",
        title = "Have you taken this exam before?",
        subtitle = "We'll adjust your plan based on your history",
        options = listOf(
            "No – first attempt",
            "Yes – didn't pass, retrying",
            "Yes – passed, recertifying"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "cert_stage",
        title = "How far into your studying are you?",
        subtitle = "We'll pick up from where you are",
        options = listOf("Not yet started", "Just started", "Midway through", "Final revision stage"),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "cert_materials",
        title = "How are you studying for this exam?",
        subtitle = "Select all that apply",
        options = listOf("Official materials", "A structured course", "Self-study", "A mix of these"),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "cert_concerns",
        title = "What's your biggest concern about the exam?",
        subtitle = "We'll focus your plan to address this",
        options = listOf(
            "Running out of time",
            "Covering all the content",
            "Exam technique & timing",
            "Staying consistent",
            "All of the above"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "cert_attention",
        title = "How long can you focus before your mind wanders?",
        subtitle = "Your sessions will be structured around this",
        options = listOf("Under 10 mins", "10–20 mins", "20–45 mins", "45+ mins")
    ),
    Question(
        id = "cert_learning_style",
        title = "When you understood something really well, how did it happen?",
        subtitle = "We'll lean into what works for you",
        options = listOf(
            "I read it carefully in my own time",
            "Someone explained it or I watched it explained",
            "I heard it discussed or talked it through",
            "I tried it and figured it out through practice"
        ),
        type = QuestionType.OPTIONS_FLOW
    )
)

// Self-study / Specific topic branch
val SELF_STUDY_QUESTIONS = listOf(
    Question(
        id = "self_topic",
        title = "What are you studying?",
        subtitle = "Tell us in your own words",
        type = QuestionType.TEXT
    ),
    Question(
        id = "self_method",
        title = "How are you learning it?",
        subtitle = "Select all that apply",
        options = listOf(
            "Online course (Udemy, Coursera, YouTube…)",
            "A book or textbook",
            "Official docs / articles",
            "A mentor or tutor",
            "Completely self-directed"
        ),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "self_why",
        title = "Why are you learning this?",
        subtitle = "This shapes how we motivate and structure your plan",
        options = listOf(
            "Career change or new job skill",
            "Personal interest or hobby",
            "Start or grow a business",
            "Build a specific project",
            "Just exploring for now"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "self_deadline",
        title = "Do you have a deadline or target date?",
        subtitle = "We'll build urgency into your plan if needed",
        options = listOf("Within 1 month", "1–3 months", "3–6 months", "No deadline")
    ),
    Question(
        id = "self_stage",
        title = "How far into it are you?",
        subtitle = "We'll pick up from where you are",
        options = listOf(
            "Haven't started yet",
            "Just getting started",
            "Partway through",
            "Nearly done, just consolidating"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "self_content_type",
        title = "What's the nature of what you're learning?",
        subtitle = "Helps us choose the right study techniques",
        options = listOf(
            "Mostly concepts and theory",
            "Mostly practical skills to apply",
            "A mix of both",
            "Creative or artistic"
        ),
        type = QuestionType.OPTIONS_FLOW
    ),
    Question(
        id = "self_memory_load",
        title = "What does this subject involve most?",
        subtitle = "We'll tailor memory and recall strategies",
        options = listOf(
            "Lots of things to memorise",
            "Building understanding and thinking",
            "Hands-on repetition and muscle memory"
        )
    ),
    Question(
        id = "self_challenges",
        title = "What's your biggest challenge with self-studying?",
        subtitle = "Select all that apply",
        options = listOf(
            "Staying consistent without external pressure",
            "Knowing if I'm making progress",
            "Getting stuck with no one to ask",
            "The material is hard to understand",
            "I start strong but lose motivation"
        ),
        isMultiSelect = true,
        type = QuestionType.OPTIONS_FLOW
    )
)

// Final question — everyone
val SUCCESS_QUESTION = Question(
    id = "success_goal",
    title = "What does success look like for you in 3 months?",
    subtitle = "Tell us in your own words — we'll keep you on track",
    type = QuestionType.TEXT
)

// ── Dynamic sequence builder ───────────────────────────────────────────────────
fun buildQuestionSequence(state: OnboardingState): List<Question> {
    val sequence = mutableListOf<Question>()

    // Always start with general questions
    sequence.addAll(GENERAL_QUESTIONS)

    // Conditional follow-ups based on learning differences selection
    val diffs = (state.answers["learning_diffs"] as? Set<*>)
        ?.filterIsInstance<String>() ?: emptyList()

    if (diffs.contains("Hard to focus or sit still")) sequence.add(FOCUS_FOLLOWUP)
    if (diffs.contains("Struggle with reading or spelling")) sequence.add(READING_FOLLOWUP)
    if (diffs.contains("Prefer clear structure; changes stress me")) sequence.add(STRUCTURE_FOLLOWUP)
    if (diffs.contains("Often anxious or overwhelmed studying")) sequence.add(ANXIETY_FOLLOWUP)

    // Branch questions based on user type
    when (state.answers["user_type"] as? String) {
        "Uni Student" -> sequence.addAll(UNI_QUESTIONS)
        "Language Learner" -> sequence.addAll(LANGUAGE_QUESTIONS)
        "Certification Candidate" -> sequence.addAll(CERT_QUESTIONS)
        "Studying a specific topic" -> sequence.addAll(SELF_STUDY_QUESTIONS)
    }

    // Final question only once a branch is chosen
    if (state.answers["user_type"] != null) {
        sequence.add(SUCCESS_QUESTION)
    }

    return sequence
}

// ── Can-advance check ──────────────────────────────────────────────────────────
fun canAdvance(question: Question, state: OnboardingState): Boolean {
    if (question.isOptional) return true
    val answer = state.answers[question.id]
    return when (question.type) {
        QuestionType.TEXT -> (answer as? String)?.isNotBlank() == true
        QuestionType.OPTIONS, QuestionType.OPTIONS_FLOW -> {
            if (question.isMultiSelect) (answer as? Set<*>)?.isNotEmpty() == true
            else (answer as? String)?.isNotBlank() == true
        }
    }
}

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

    val questionSequence = buildQuestionSequence(state)
    val totalSteps = questionSequence.size
    val currentIndex = state.currentPage.coerceIn(0, (totalSteps - 1).coerceAtLeast(0))
    val currentQuestion = questionSequence.getOrNull(currentIndex)

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

        Text(
            text = "Set up your profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Question ${currentIndex + 1} of $totalSteps",
            style = MaterialTheme.typography.bodyMedium,
            color = subtleText
        )

        Spacer(Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { (currentIndex + 1) / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = Green400,
            trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Green100
        )

        Spacer(Modifier.height(36.dp))

        if (currentQuestion != null) {
            AnimatedContent(
                targetState = currentIndex,
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
            ) { pageIndex ->
                val question = questionSequence.getOrNull(pageIndex) ?: return@AnimatedContent
                QuestionContent(
                    question = question,
                    selectedSingle = state.answers[question.id] as? String ?: "",
                    selectedMulti = (state.answers[question.id] as? Set<*>)
                        ?.filterIsInstance<String>()?.toSet() ?: emptySet(),
                    textAnswer = state.answers[question.id] as? String ?: "",
                    onSingleSelect = { option -> viewModel.setSingleAnswer(question.id, option) },
                    onMultiToggle = { option -> viewModel.toggleMultiAnswer(question.id, option) },
                    onTextChange = { text -> viewModel.setTextAnswer(question.id, text) },
                    isDark = isDark,
                    textColor = textColor,
                    subtleText = subtleText
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = viewModel::previousPage,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Green400),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green400)
                ) {
                    Text("Back", style = MaterialTheme.typography.titleMedium)
                }
            }

            val canGoNext = currentQuestion?.let { canAdvance(it, state) } ?: false

            Button(
                onClick = {
                    if (currentIndex >= totalSteps - 1) viewModel.completeSetup()
                    else viewModel.nextPage()
                },
                enabled = canGoNext,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green400,
                    contentColor = Color.White,
                    disabledContainerColor = Green400.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (currentIndex >= totalSteps - 1) "Finish ✓" else "Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Question content ───────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionContent(
    question: Question,
    selectedSingle: String,
    selectedMulti: Set<String>,
    textAnswer: String,
    onTextChange: (String) -> Unit,
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

        when (question.type) {
            QuestionType.OPTIONS -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(question.options) { option ->
                        val isSelected = if (question.isMultiSelect) option in selectedMulti
                        else option == selectedSingle

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

            QuestionType.OPTIONS_FLOW -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            question.options.forEach { option ->
                                val isSelected = if (question.isMultiSelect) option in selectedMulti
                                else option == selectedSingle

                                OptionChip(
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
            }

            QuestionType.TEXT -> {
                OutlinedTextField(
                    value = textAnswer,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = { Text("Type your answer…") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = false
                )
            }
        }
    }
}

// ── Option card (2-column grid) ────────────────────────────────────────────────
@Composable
private fun OptionCard(
    text: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgUnselected = if (isDark) Color(0xFF1A1A1A) else Color.White
    val borderColor = if (isSelected) Green400
    else if (isDark) Color.White.copy(alpha = 0.1f)
    else Color(0xFFDDDDDD)
    val textCol = when {
        isSelected -> Color.White
        isDark -> Color.White
        else -> Color(0xFF1A1A1A)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Green400 else bgUnselected)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textCol,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

// ── Option chip (flow row) ─────────────────────────────────────────────────────
@Composable
private fun OptionChip(
    text: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgUnselected = if (isDark) Color(0xFF1A1A1A) else Color.White
    val borderColor = if (isSelected) Green400
    else if (isDark) Color.White.copy(alpha = 0.12f)
    else Color(0xFFDDDDDD)
    val textCol = when {
        isSelected -> Color.White
        isDark -> Color.White
        else -> Color(0xFF1A1A1A)
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(if (isSelected) Green400 else bgUnselected)
            .border(1.5.dp, borderColor, RoundedCornerShape(23.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textCol
        )
    }
}