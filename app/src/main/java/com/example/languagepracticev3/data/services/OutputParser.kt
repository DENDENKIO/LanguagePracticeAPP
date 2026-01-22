package com.example.languagepracticev3.data.services

import com.example.languagepracticev3.data.model.OperationKind  // models → model に修正

class OutputParser {

    data class ParsedWork(
        val title: String,
        val body: String,
        val writer: String,
        val theme: String,
        val emotion: String
    )

    data class ParsedStudyCard(
        val front: String,
        val back: String,
        val hint: String,
        val difficulty: String
    )

    data class ParsedPersona(
        val name: String,
        val bio: String,
        val style: String,
        val strengths: String
    )

    data class ParsedTopic(
        val title: String,
        val constraint: String,
        val hint: String
    )

    fun parseWorks(output: String): List<ParsedWork> {
        val works = mutableListOf<ParsedWork>()

        val title = extractTag(output, "TITLE")
        val body = extractTag(output, "BODY")
        val writer = extractTag(output, "WRITER")
        val theme = extractTag(output, "THEME")
        val emotion = extractTag(output, "EMOTION")

        if (body.isNotBlank()) {
            works.add(ParsedWork(
                title = title.ifBlank { "無題" },
                body = body,
                writer = writer,
                theme = theme,
                emotion = emotion
            ))
        }

        return works
    }

    fun parseStudyCards(output: String): List<ParsedStudyCard> {
        val cards = mutableListOf<ParsedStudyCard>()
        val cardBlocks = output.split("[CARD_START]")
            .filter { it.contains("[CARD_END]") }

        for (block in cardBlocks) {
            val front = extractTag(block, "FRONT")
            val back = extractTag(block, "BACK")
            val hint = extractTag(block, "HINT")
            val difficulty = extractTag(block, "DIFFICULTY")

            if (front.isNotBlank() && back.isNotBlank()) {
                cards.add(ParsedStudyCard(front, back, hint, difficulty.ifBlank { "MEDIUM" }))
            }
        }

        return cards
    }

    fun parsePersonas(output: String): List<ParsedPersona> {
        val personas = mutableListOf<ParsedPersona>()
        val blocks = output.split("[PERSONA_START]")
            .filter { it.contains("[PERSONA_END]") }

        for (block in blocks) {
            val name = extractTag(block, "NAME")
            val bio = extractTag(block, "BIO")
            val style = extractTag(block, "STYLE")
            val strengths = extractTag(block, "STRENGTHS")

            if (name.isNotBlank()) {
                personas.add(ParsedPersona(name, bio, style, strengths))
            }
        }

        return personas
    }

    fun parseTopics(output: String): List<ParsedTopic> {
        val topics = mutableListOf<ParsedTopic>()
        val blocks = output.split("[TOPIC_START]")
            .filter { it.contains("[TOPIC_END]") }

        for (block in blocks) {
            val title = extractTag(block, "TITLE")
            val constraint = extractTag(block, "CONSTRAINT")
            val hint = extractTag(block, "HINT")

            if (title.isNotBlank()) {
                topics.add(ParsedTopic(title, constraint, hint))
            }
        }

        return topics
    }

    fun parseGeneric(output: String, operation: OperationKind): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // 共通タグを抽出
        val commonTags = listOf(
            "TITLE", "BODY", "THEME", "EMOTION", "TAKEAWAY",
            "CORE_SENTENCE", "ANALYSIS", "SIGHT", "SOUND",
            "SMELL", "TOUCH", "TASTE", "METAPHOR", "CORE_CANDIDATES",
            "CONVERTED", "CONVERSION_NOTE",
            "REVISION_1", "REVISION_1_NOTE",
            "REVISION_2", "REVISION_2_NOTE",
            "REVISION_3", "REVISION_3_NOTE"
        )

        for (tag in commonTags) {
            val value = extractTag(output, tag)
            if (value.isNotBlank()) {
                result[tag] = value
            }
        }

        return result
    }

    private fun extractTag(text: String, tagName: String): String {
        // [TAG_NAME] 形式で囲まれた内容を抽出
        val pattern = "\\[$tagName\\]\\s*([\\s\\S]*?)(?=\\[|$)".toRegex()
        val match = pattern.find(text)
        return match?.groupValues?.getOrNull(1)?.trim() ?: ""
    }
}
