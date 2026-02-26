package com.el.studyflow.domain.model

data class StudyTechnique(
    val id: String,
    val name: String,
    val description: String,
    val whyItWorks: String,
    val icon: String, // emoji or drawable name
    val iconBackgroundColor: String, // hex color
    val howToApply: String,
    val videoUrl: String? = null, // for future integration
    val isExpanded: Boolean = false
)

// Predefined techniques based on learning styles
object StudyTechniques {
    val techniques = listOf(
        StudyTechnique(
            id = "1",
            name = "Feynman Technique",
            description = "Break down complex topics into simple explanations",
            whyItWorks = "Because you learn best by teaching others and have strong verbal communication skills. Breaking down complex topics into simple explanations helps solidify your understanding.",
            icon = "🧠",
            iconBackgroundColor = "#40916C",
            howToApply = "1. Pick a concept\n2. Explain it in simple terms\n3. Identify gaps in your explanation\n4. Refine and simplify"
        ),
        StudyTechnique(
            id = "2",
            name = "Active Recall",
            description = "Test yourself frequently to strengthen memory",
            whyItWorks = "Your learning style benefits from retrieval practice. Instead of passive reading, actively recalling information strengthens neural pathways.",
            icon = "🎯",
            iconBackgroundColor = "#0066CC",
            howToApply = "1. Study material normally\n2. Close the book\n3. Recall what you learned\n4. Check your answers\n5. Repeat"
        ),
        StudyTechnique(
            id = "3",
            name = "Spaced Repetition",
            description = "Review material at increasing intervals",
            whyItWorks = "This technique aligns with how memory works. Reviewing material just as you're about to forget it maximizes retention.",
            icon = "⏰",
            iconBackgroundColor = "#FF9500",
            howToApply = "1. Learn material on Day 1\n2. Review on Day 3\n3. Review on Day 7\n4. Review on Day 30\n5. Continue as needed"
        ),
        StudyTechnique(
            id = "4",
            name = "Mind Mapping",
            description = "Organize information visually with connections",
            whyItWorks = "Visual organization helps your brain see relationships between concepts and remember information hierarchically.",
            icon = "🗺️",
            iconBackgroundColor = "#FF3B30",
            howToApply = "1. Write central idea in center\n2. Add branches for subtopics\n3. Add details to each branch\n4. Draw connections between ideas"
        ),
        StudyTechnique(
            id = "5",
            name = "Pomodoro Technique",
            description = "Study in focused 25-minute intervals with breaks",
            whyItWorks = "Consistent, focused intervals prevent burnout and maintain attention. Regular breaks help consolidate memory.",
            icon = "🍅",
            iconBackgroundColor = "#E53935",
            howToApply = "1. Set timer for 25 minutes\n2. Study with full focus\n3. Take 5-minute break\n4. Repeat 4 times\n5. Take 15-minute break"
        )
    )
}