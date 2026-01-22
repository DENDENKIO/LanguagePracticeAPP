// app/src/main/java/com/example/languagepracticev3/data/services/OutputParser.kt
package com.example.languagepracticev3.data.services

/**
 * AI出力の解析結果データクラス
 */
data class TextGenParsed(val title: String, val bodyText: String)
data class StudyCardParsed(
    val focus: String,
    val level: String,
    val bestExpressions: String,
    val metaphorChains: String,
    val doNext: String,
    val tags: String
)
data class PersonaParsed(
    val name: String,
    val location: String,
    val bio: String,
    val style: String,
    val tags: String
)
data class TopicParsed(
    val title: String,
    val emotion: String,
    val scene: String,
    val tags: String,
    val fixConditions: String
)
data class ObservationParsed(
    val motif: String,
    val visual: String,
    val sound: String,
    val metaphors: String,
    val coreCandidates: String
)
data class CoreExtractParsed(
    val theme: String,
    val emotion: String,
    val takeaway: String,
    val coreSentence: String
)
data class GikoParsed(val title: String, val bodyText: String)
data class RevisionParsed(val bodyText: String, val comment: String)

/**
 * AI出力パーサー
 * WPF版 OutputParser.cs をKotlinに移植
 */
class OutputParser {

    companion object {
        // マーカー定義
        const val MARKER_BODY_BEGIN = "<<<LP_BODY_BEGIN>>>"
        const val MARKER_BODY_END = "<<<LP_BODY_END>>>"
        const val MARKER_TITLE_BEGIN = "<<<LP_TITLE_BEGIN>>>"
        const val MARKER_TITLE_END = "<<<LP_TITLE_END>>>"
        const val MARKER_ITEMS_BEGIN = "<<<LP_ITEMS_BEGIN>>>"
        const val MARKER_ITEMS_END = "<<<LP_ITEMS_END>>>"
        const val MARKER_ITEM_SEP = "<<<LP_ITEM_SEP>>>"

        const val DONE_SENTINEL = "<<<LP_DONE>>>"
    }

    /**
     * テキスト生成出力の解析
     */
    fun parseTextGenOutput(output: String): TextGenParsed? {
        val title = extractBetween(output, MARKER_TITLE_BEGIN, MARKER_TITLE_END)?.trim() ?: ""
        val body = extractBetween(output, MARKER_BODY_BEGIN, MARKER_BODY_END)?.trim()

        return if (body != null) {
            TextGenParsed(
                title = title.ifEmpty { "無題" },
                bodyText = body
            )
        } else {
            // マーカーがない場合、センチネルの前のテキストを本文として扱う
            val sentinelPos = output.indexOf(DONE_SENTINEL)
            if (sentinelPos > 0) {
                TextGenParsed(
                    title = "無題",
                    bodyText = output.substring(0, sentinelPos).trim()
                )
            } else {
                null
            }
        }
    }

    /**
     * 学習カード出力の解析
     */
    fun parseStudyCardOutput(output: String): StudyCardParsed? {
        val focus = extractTagContent(output, "FOCUS") ?: ""
        val level = extractTagContent(output, "LEVEL") ?: ""
        val bestExpressions = extractTagContent(output, "BEST_EXPRESSIONS") ?: ""
        val metaphorChains = extractTagContent(output, "METAPHOR_CHAINS") ?: ""
        val doNext = extractTagContent(output, "DO_NEXT") ?: ""
        val tags = extractTagContent(output, "TAGS") ?: ""

        return if (focus.isNotEmpty() || bestExpressions.isNotEmpty()) {
            StudyCardParsed(
                focus = focus,
                level = level,
                bestExpressions = bestExpressions,
                metaphorChains = metaphorChains,
                doNext = doNext,
                tags = tags
            )
        } else {
            // フォールバック: 全体をパースできない場合
            StudyCardParsed(
                focus = "解析結果",
                level = "",
                bestExpressions = output.take(500),
                metaphorChains = "",
                doNext = "",
                tags = ""
            )
        }
    }

    /**
     * ペルソナ生成出力の解析（複数）
     */
    fun parsePersonaGenOutput(output: String): List<PersonaParsed> {
        val result = mutableListOf<PersonaParsed>()

        val itemsBlock = extractBetween(output, MARKER_ITEMS_BEGIN, MARKER_ITEMS_END)
        if (itemsBlock != null) {
            val items = itemsBlock.split(MARKER_ITEM_SEP)
            for (item in items) {
                val name = extractTagContent(item, "NAME") ?: continue
                val location = extractTagContent(item, "LOCATION") ?: ""
                val bio = extractTagContent(item, "BIO") ?: ""
                val style = extractTagContent(item, "STYLE") ?: ""
                val tags = extractTagContent(item, "TAGS") ?: ""

                result.add(PersonaParsed(name, location, bio, style, tags))
            }
        } else {
            // フォールバック: 単一ペルソナ
            val name = extractTagContent(output, "NAME") ?: return emptyList()
            val location = extractTagContent(output, "LOCATION") ?: ""
            val bio = extractTagContent(output, "BIO") ?: ""
            val style = extractTagContent(output, "STYLE") ?: ""
            val tags = extractTagContent(output, "TAGS") ?: ""

            result.add(PersonaParsed(name, location, bio, style, tags))
        }

        return result
    }

    /**
     * トピック生成出力の解析（複数）
     */
    fun parseTopicGenOutput(output: String): List<TopicParsed> {
        val result = mutableListOf<TopicParsed>()

        val itemsBlock = extractBetween(output, MARKER_ITEMS_BEGIN, MARKER_ITEMS_END)
        if (itemsBlock != null) {
            val items = itemsBlock.split(MARKER_ITEM_SEP)
            for (item in items) {
                val title = extractTagContent(item, "TITLE") ?: continue
                val emotion = extractTagContent(item, "EMOTION") ?: ""
                val scene = extractTagContent(item, "SCENE") ?: ""
                val tags = extractTagContent(item, "TAGS") ?: ""
                val fix = extractTagContent(item, "FIX_CONDITIONS") ?: ""

                result.add(TopicParsed(title, emotion, scene, tags, fix))
            }
        }

        return result
    }

    /**
     * 観察ノート出力の解析
     */
    fun parseObservationOutput(output: String): ObservationParsed? {
        val motif = extractTagContent(output, "MOTIF") ?: ""
        val visual = extractTagContent(output, "VISUAL") ?: ""
        val sound = extractTagContent(output, "SOUND") ?: ""
        val metaphors = extractTagContent(output, "METAPHORS") ?: ""
        val coreCandidates = extractTagContent(output, "CORE_CANDIDATES") ?: ""

        return if (motif.isNotEmpty() || visual.isNotEmpty()) {
            ObservationParsed(motif, visual, sound, metaphors, coreCandidates)
        } else {
            null
        }
    }

    /**
     * 核抽出出力の解析
     */
    fun parseCoreExtractOutput(output: String): CoreExtractParsed? {
        val theme = extractTagContent(output, "THEME") ?: ""
        val emotion = extractTagContent(output, "EMOTION") ?: ""
        val takeaway = extractTagContent(output, "TAKEAWAY") ?: ""
        val coreSentence = extractTagContent(output, "CORE_SENTENCE") ?: ""

        return if (theme.isNotEmpty() || coreSentence.isNotEmpty()) {
            CoreExtractParsed(theme, emotion, takeaway, coreSentence)
        } else {
            null
        }
    }

    /**
     * 擬古文出力の解析
     */
    fun parseGikoOutput(output: String): GikoParsed? {
        val title = extractBetween(output, MARKER_TITLE_BEGIN, MARKER_TITLE_END)?.trim() ?: "擬古文"
        val body = extractBetween(output, MARKER_BODY_BEGIN, MARKER_BODY_END)?.trim()

        return if (body != null) {
            GikoParsed(title, body)
        } else {
            null
        }
    }

    /**
     * 推敲出力の解析（複数案）
     */
    fun parseRevisionOutput(output: String): List<RevisionParsed> {
        val result = mutableListOf<RevisionParsed>()

        val itemsBlock = extractBetween(output, MARKER_ITEMS_BEGIN, MARKER_ITEMS_END)
        if (itemsBlock != null) {
            val items = itemsBlock.split(MARKER_ITEM_SEP)
            for (item in items) {
                val body = extractTagContent(item, "BODY") ?: extractBetween(item, MARKER_BODY_BEGIN, MARKER_BODY_END) ?: continue
                val comment = extractTagContent(item, "COMMENT") ?: ""
                result.add(RevisionParsed(body.trim(), comment.trim()))
            }
        } else {
            // 単一案
            val body = extractBetween(output, MARKER_BODY_BEGIN, MARKER_BODY_END)
            if (body != null) {
                result.add(RevisionParsed(body.trim(), ""))
            }
        }

        return result
    }

    // ====================
    // ヘルパー
    // ====================

    private fun extractBetween(text: String, beginMarker: String, endMarker: String): String? {
        val beginPos = text.indexOf(beginMarker)
        if (beginPos == -1) return null

        val startPos = beginPos + beginMarker.length
        val endPos = text.indexOf(endMarker, startPos)
        if (endPos == -1) return null

        return text.substring(startPos, endPos)
    }

    private fun extractTagContent(text: String, tagName: String): String? {
        // 形式: [TAG_NAME]内容[/TAG_NAME] または <TAG_NAME>内容</TAG_NAME>
        val patterns = listOf(
            "\\[$tagName\\](.*?)\\[/$tagName\\]",
            "<$tagName>(.*?)</$tagName>",
            "<<<LP_${tagName}_BEGIN>>>(.*?)<<<LP_${tagName}_END>>>"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }

        return null
    }
}
