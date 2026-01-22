// app/src/main/java/com/example/languagepracticev3/data/services/OutputParser.kt
package com.example.languagepracticev3.data.services

import com.example.languagepracticev3.data.model.LpConstants
import com.example.languagepracticev3.data.model.OperationKind

/**
 * AI出力の解析結果データクラス
 */
data class TextGenParsed(val title: String, val bodyText: String, val writerName: String = "", val readerNote: String = "", val toneLabel: String = "")
data class StudyCardParsed(
    val focus: String,
    val level: String,
    val bestExpressions: String,
    val metaphorChains: String,
    val doNext: String,
    val tags: String,
    val fullParsedContent: String = ""
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
 * WPF版 OutputParser.cs の解析ロジックをKotlinに移植
 */
class OutputParser {

    companion object {
        // 旧マーカー定義（互換性維持）
        const val MARKER_BODY_BEGIN = "<<<LP_BODY_BEGIN>>>"
        const val MARKER_BODY_END = "<<<LP_BODY_END>>>"
        const val MARKER_TITLE_BEGIN = "<<<LP_TITLE_BEGIN>>>"
        const val MARKER_TITLE_END = "<<<LP_TITLE_END>>>"
        const val MARKER_ITEMS_BEGIN = "<<<LP_ITEMS_BEGIN>>>"
        const val MARKER_ITEMS_END = "<<<LP_ITEMS_END>>>"
        const val MARKER_ITEM_SEP = "<<<LP_ITEM_SEP>>>"
        const val DONE_SENTINEL = "<<<LP_DONE>>>"
    }

    // ==========================================
    // 汎用ヘルパー: マーカー範囲抽出
    // ==========================================
    private fun extractMarkerRange(rawOutput: String, beginMarker: String, endMarker: String): String {
        val start = rawOutput.lastIndexOf(beginMarker)
        val end = rawOutput.lastIndexOf(endMarker)

        if (start == -1 || end == -1 || start >= end) return ""

        val contentStart = start + beginMarker.length
        return rawOutput.substring(contentStart, end).trim()
    }

    // ==========================================
    // 改行崩れに強い KEY:VALUE パース（WPF版移植）
    // ==========================================
    private fun parseKeyValueBlock(block: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        if (block.isBlank()) return result

        // 正規化: 全角・半角コロン対応
        val s = normalizeText(block)

        // KEY: VALUE 形式を検出
        val regex = Regex("""(?<k>[A-Z][A-Z0-9_]{1,40})\s*[：:]\s*""", RegexOption.IGNORE_CASE)
        val matches = regex.findAll(s).toList()
        if (matches.isEmpty()) return result

        for (i in matches.indices) {
            val m = matches[i]
            val key = m.groups["k"]?.value?.trim()?.uppercase() ?: continue

            val valueStart = m.range.last + 1
            val valueEnd = if (i + 1 < matches.size) matches[i + 1].range.first else s.length
            if (valueEnd < valueStart) continue

            val value = s.substring(valueStart, valueEnd).trim()

            if (result.containsKey(key)) {
                result[key] = result[key] + "\n" + value
            } else {
                result[key] = value
            }
        }

        // 「READERにTOPICが飲まれる」等の軽補正
        fixShiftedFields(result)

        return result
    }

    private fun fixShiftedFields(dict: MutableMap<String, String>) {
        val readerVal = dict["READER"] ?: return
        val mTopic = Regex("""^\s*TOPIC\s*[：:]\s*(?<v>[\s\S]+)$""", RegexOption.IGNORE_CASE).find(readerVal)
        if (mTopic != null) {
            val topicVal = dict["TOPIC"]
            if (topicVal.isNullOrBlank()) {
                dict["TOPIC"] = mTopic.groups["v"]?.value?.trim() ?: ""
            }
            dict["READER"] = ""
        }
    }

    private fun normalizeText(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("　", " ")  // 全角スペース→半角
    }

    // ==========================================
    // TOPICブロックから FIX セクションを丸ごと抽出
    // ==========================================
    private fun extractFixSection(topicBlock: String): String {
        if (topicBlock.isBlank()) return ""

        val s = normalizeText(topicBlock)

        // FIX行の開始
        val mFix = Regex("""(?im)^\s*FIX\s*[：:]\s*(?<rest>.*)$""").find(s) ?: return ""

        val rest = mFix.groups["rest"]?.value?.trim() ?: ""

        // FIX行の次行から
        val nl = s.indexOf('\n', mFix.range.first)
        val afterLineStart = if (nl == -1) (mFix.range.last + 1) else (nl + 1)

        val tail = if (afterLineStart <= s.length) s.substring(afterLineStart) else ""

        // 終端（@@@TOPIC_END@@@ まで）
        val endMatch = Regex("""(?im)^\s*@@@TOPIC_END@@@\s*$""").find(tail)
        val endPos = endMatch?.range?.first ?: tail.length

        val body = if (endPos > 0) tail.substring(0, endPos).trim() else ""

        // FIX: が同一行で内容を持つ形式も吸収
        return if (rest.isNotBlank()) {
            if (body.isBlank()) rest else "$rest\n$body".trim()
        } else {
            body
        }
    }

    // ==========================================
    // TEXT_GEN 解析（WPF版 ParseWorks 移植）
    // ==========================================
    fun parseTextGenOutput(output: String): TextGenParsed? {
        if (output.isBlank()) return null

        val beginMarker = LpConstants.MarkerBegin[OperationKind.TEXT_GEN] ?: "<<<TEXT_GEN_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.TEXT_GEN] ?: "<<<TEXT_GEN_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        // @@@WORK|n@@@ で複数ブロックに分割
        val blocks = targetArea.split(Regex("""@@@\s*WORK\s*\|\s*\d+\s*@@@""", RegexOption.IGNORE_CASE))

        for (block in blocks) {
            if (block.isBlank()) continue
            if (!block.contains(Regex("""(TEXT|GIKO_TEXT|REVISED_TEXT)\s*[：:]""", RegexOption.IGNORE_CASE))) continue

            val dict = parseKeyValueBlock(block)
            if (dict.isEmpty()) continue

            val body = (dict["TEXT"] ?: "") + (dict["GIKO_TEXT"] ?: "") + (dict["REVISED_TEXT"] ?: "")
            if (body.isBlank()) continue

            return TextGenParsed(
                title = dict["TOPIC"] ?: "無題",
                bodyText = normalizeBodyText(body),
                writerName = dict["WRITER"] ?: "",
                readerNote = dict["READER"] ?: "",
                toneLabel = dict["TONE"] ?: "なし"
            )
        }

        // ブロック区切りがない場合（単一出力）
        val dict = parseKeyValueBlock(targetArea)
        val body = (dict["TEXT"] ?: "") + (dict["GIKO_TEXT"] ?: "") + (dict["REVISED_TEXT"] ?: "")

        if (dict.isNotEmpty() && body.isNotBlank()) {
            return TextGenParsed(
                title = dict["TOPIC"] ?: "無題",
                bodyText = normalizeBodyText(body),
                writerName = dict["WRITER"] ?: "",
                readerNote = dict["READER"] ?: "",
                toneLabel = dict["TONE"] ?: "なし"
            )
        }

        // 旧形式フォールバック
        return parseTextGenOutputLegacy(output)
    }

    // 旧形式パーサー（互換性維持）
    private fun parseTextGenOutputLegacy(output: String): TextGenParsed? {
        val title = extractBetween(output, MARKER_TITLE_BEGIN, MARKER_TITLE_END)?.trim() ?: ""
        val body = extractBetween(output, MARKER_BODY_BEGIN, MARKER_BODY_END)?.trim()

        return if (body != null) {
            TextGenParsed(
                title = title.ifEmpty { "無題" },
                bodyText = body
            )
        } else {
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

    // ==========================================
    // STUDY_CARD 解析（WPF版 ParseStudyCards 移植）
    // ==========================================
    fun parseStudyCardOutput(output: String): StudyCardParsed? {
        if (output.isBlank()) return null

        val beginMarker = LpConstants.MarkerBegin[OperationKind.STUDY_CARD] ?: "<<<STUDY_CARD_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.STUDY_CARD] ?: "<<<STUDY_CARD_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        // @@@CARD|n@@@ で複数ブロックに分割
        val blocks = targetArea.split(Regex("""@@@\s*CARD\s*\|\s*\d+\s*@@@""", RegexOption.IGNORE_CASE))

        for (block in blocks) {
            if (block.isBlank()) continue
            if (!block.contains(Regex("""FOCUS\s*[：:]""", RegexOption.IGNORE_CASE))) continue

            val dict = parseKeyValueBlock(block)
            if (dict.isEmpty()) continue

            return StudyCardParsed(
                focus = dict["FOCUS"] ?: "解析結果参照",
                level = dict["LEVEL"] ?: "NORMAL",
                bestExpressions = dict["BEST_EXPRESSIONS"] ?: "",
                metaphorChains = dict["METAPHOR_CHAINS"] ?: "",
                doNext = dict["DO_NEXT"] ?: "",
                tags = dict["TAGS"] ?: "",
                fullParsedContent = dict.entries.joinToString("\n\n") { "${it.key}: ${it.value}" }
            )
        }

        // ブロック区切りなしの場合
        val dict = parseKeyValueBlock(targetArea)
        if (dict.isNotEmpty() && (dict["FOCUS"]?.isNotBlank() == true || dict["BEST_EXPRESSIONS"]?.isNotBlank() == true)) {
            return StudyCardParsed(
                focus = dict["FOCUS"] ?: "解析結果参照",
                level = dict["LEVEL"] ?: "NORMAL",
                bestExpressions = dict["BEST_EXPRESSIONS"] ?: "",
                metaphorChains = dict["METAPHOR_CHAINS"] ?: "",
                doNext = dict["DO_NEXT"] ?: "",
                tags = dict["TAGS"] ?: "",
                fullParsedContent = dict.entries.joinToString("\n\n") { "${it.key}: ${it.value}" }
            )
        }

        // フォールバック
        return StudyCardParsed(
            focus = "解析結果",
            level = "",
            bestExpressions = output.take(500),
            metaphorChains = "",
            doNext = "",
            tags = "",
            fullParsedContent = output
        )
    }

    // ==========================================
    // PERSONA_GEN 解析（WPF版 ParsePersonas 移植）
    // ==========================================
    fun parsePersonaGenOutput(output: String): List<PersonaParsed> {
        val result = mutableListOf<PersonaParsed>()
        if (output.isBlank()) return result

        val beginMarker = LpConstants.MarkerBegin[OperationKind.PERSONA_GEN] ?: "<<<PERSONA_PACK_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.PERSONA_GEN] ?: "<<<PERSONA_PACK_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        // @@@PERSONA|n@@@ で分割
        val blocks = targetArea.split(Regex("""@@@\s*PERSONA\s*\|\s*\d+\s*@@@""", RegexOption.IGNORE_CASE))

        for (block in blocks) {
            if (block.isBlank()) continue
            if (!block.contains(Regex("""NAME\s*[：:]""", RegexOption.IGNORE_CASE))) continue

            val dict = parseKeyValueBlock(block)
            if (dict.isEmpty()) continue

            val name = normalizeText(dict["NAME"] ?: "").trim()
            if (name.isBlank()) continue

            result.add(PersonaParsed(
                name = name,
                location = normalizeText(dict["LOCATION"] ?: "").trim(),
                bio = normalizeText(dict["BIO"] ?: "").trim(),
                style = normalizeText(dict["STYLE"] ?: "").trim(),
                tags = normalizeCommaList(dict["TAGS"] ?: "")
            ))
        }

        // 旧形式フォールバック
        if (result.isEmpty()) {
            return parsePersonaGenOutputLegacy(output)
        }

        return result
    }

    private fun parsePersonaGenOutputLegacy(output: String): List<PersonaParsed> {
        val result = mutableListOf<PersonaParsed>()

        val itemsBlock = extractBetween(output, MARKER_ITEMS_BEGIN, MARKER_ITEMS_END)
        if (itemsBlock != null) {
            val items = itemsBlock.split(MARKER_ITEM_SEP)
            for (item in items) {
                val name = extractTagContent(item, "NAME") ?: continue
                result.add(PersonaParsed(
                    name = name,
                    location = extractTagContent(item, "LOCATION") ?: "",
                    bio = extractTagContent(item, "BIO") ?: "",
                    style = extractTagContent(item, "STYLE") ?: "",
                    tags = extractTagContent(item, "TAGS") ?: ""
                ))
            }
        } else {
            val name = extractTagContent(output, "NAME") ?: return emptyList()
            result.add(PersonaParsed(
                name = name,
                location = extractTagContent(output, "LOCATION") ?: "",
                bio = extractTagContent(output, "BIO") ?: "",
                style = extractTagContent(output, "STYLE") ?: "",
                tags = extractTagContent(output, "TAGS") ?: ""
            ))
        }

        return result
    }

    // ==========================================
    // TOPIC_GEN 解析（WPF版 ParseTopics 移植）
    // ==========================================
    fun parseTopicGenOutput(output: String): List<TopicParsed> {
        val result = mutableListOf<TopicParsed>()
        if (output.isBlank()) return result

        val beginMarker = LpConstants.MarkerBegin[OperationKind.TOPIC_GEN] ?: "<<<TOPIC_PACK_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.TOPIC_GEN] ?: "<<<TOPIC_PACK_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        // @@@TOPIC|n@@@ で分割
        val blocks = targetArea.split(Regex("""@@@\s*TOPIC\s*\|\s*\d+\s*@@@""", RegexOption.IGNORE_CASE))

        for (block in blocks) {
            if (block.isBlank()) continue
            if (!block.contains(Regex("""TITLE\s*[：:]""", RegexOption.IGNORE_CASE))) continue

            val dict = parseKeyValueBlock(block)
            if (dict.isEmpty()) continue

            // FIXセクションの特殊抽出
            var fixRaw = extractFixSection(block)
            if (fixRaw.isBlank()) fixRaw = dict["FIX"] ?: ""

            val title = dict["TITLE"] ?: ""
            if (title.isBlank()) continue

            result.add(TopicParsed(
                title = title,
                emotion = dict["EMOTION"] ?: "",
                scene = dict["SCENE"] ?: "",
                tags = normalizeCommaList(dict["TAGS"] ?: ""),
                fixConditions = fixRaw
            ))
        }

        // 旧形式フォールバック
        if (result.isEmpty()) {
            val itemsBlock = extractBetween(output, MARKER_ITEMS_BEGIN, MARKER_ITEMS_END)
            if (itemsBlock != null) {
                val items = itemsBlock.split(MARKER_ITEM_SEP)
                for (item in items) {
                    val title = extractTagContent(item, "TITLE") ?: continue
                    result.add(TopicParsed(
                        title = title,
                        emotion = extractTagContent(item, "EMOTION") ?: "",
                        scene = extractTagContent(item, "SCENE") ?: "",
                        tags = extractTagContent(item, "TAGS") ?: "",
                        fixConditions = extractTagContent(item, "FIX_CONDITIONS") ?: ""
                    ))
                }
            }
        }

        return result
    }

    // ==========================================
    // OBSERVE_IMAGE 解析
    // ==========================================
    fun parseObservationOutput(output: String): ObservationParsed? {
        if (output.isBlank()) return null

        val beginMarker = LpConstants.MarkerBegin[OperationKind.OBSERVE_IMAGE] ?: "<<<OBSERVATION_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.OBSERVE_IMAGE] ?: "<<<OBSERVATION_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        val dict = parseKeyValueBlock(targetArea)

        val motif = dict["IMAGE_MOTIF"] ?: dict["MOTIF"] ?: ""
        val visual = dict["VISUAL"] ?: ""

        return if (motif.isNotEmpty() || visual.isNotEmpty()) {
            ObservationParsed(
                motif = motif,
                visual = visual,
                sound = dict["SOUND"] ?: "",
                metaphors = dict["METAPHORS"] ?: "",
                coreCandidates = dict["CORE_SENTENCE_CANDIDATES"] ?: dict["CORE_CANDIDATES"] ?: ""
            )
        } else {
            // 旧形式フォールバック
            parseObservationOutputLegacy(output)
        }
    }

    private fun parseObservationOutputLegacy(output: String): ObservationParsed? {
        val motif = extractTagContent(output, "MOTIF") ?: ""
        val visual = extractTagContent(output, "VISUAL") ?: ""

        return if (motif.isNotEmpty() || visual.isNotEmpty()) {
            ObservationParsed(
                motif = motif,
                visual = visual,
                sound = extractTagContent(output, "SOUND") ?: "",
                metaphors = extractTagContent(output, "METAPHORS") ?: "",
                coreCandidates = extractTagContent(output, "CORE_CANDIDATES") ?: ""
            )
        } else {
            null
        }
    }

    // ==========================================
    // CORE_EXTRACT 解析
    // ==========================================
    fun parseCoreExtractOutput(output: String): CoreExtractParsed? {
        if (output.isBlank()) return null

        val beginMarker = LpConstants.MarkerBegin[OperationKind.CORE_EXTRACT] ?: "<<<CORE_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.CORE_EXTRACT] ?: "<<<CORE_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        val dict = parseKeyValueBlock(targetArea)

        val theme = dict["THEME"] ?: ""
        val coreSentence = dict["CORE_SENTENCE"] ?: ""

        return if (theme.isNotEmpty() || coreSentence.isNotEmpty()) {
            CoreExtractParsed(
                theme = theme,
                emotion = dict["EMOTION"] ?: "",
                takeaway = dict["TAKEAWAY"] ?: "",
                coreSentence = coreSentence
            )
        } else {
            // 旧形式フォールバック
            CoreExtractParsed(
                theme = extractTagContent(output, "THEME") ?: "",
                emotion = extractTagContent(output, "EMOTION") ?: "",
                takeaway = extractTagContent(output, "TAKEAWAY") ?: "",
                coreSentence = extractTagContent(output, "CORE_SENTENCE") ?: ""
            ).takeIf { it.theme.isNotEmpty() || it.coreSentence.isNotEmpty() }
        }
    }

    // ==========================================
    // GIKO 解析
    // ==========================================
    fun parseGikoOutput(output: String): GikoParsed? {
        if (output.isBlank()) return null

        val beginMarker = LpConstants.MarkerBegin[OperationKind.GIKO] ?: "<<<GIKO_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.GIKO] ?: "<<<GIKO_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        val dict = parseKeyValueBlock(targetArea)
        val gikoText = dict["GIKO_TEXT"] ?: dict["TEXT"] ?: ""

        return if (gikoText.isNotBlank()) {
            GikoParsed(
                title = dict["TOPIC"] ?: "擬古文",
                bodyText = normalizeBodyText(gikoText)
            )
        } else {
            // 旧形式フォールバック
            val title = extractBetween(output, MARKER_TITLE_BEGIN, MARKER_TITLE_END)?.trim() ?: "擬古文"
            val body = extractBetween(output, MARKER_BODY_BEGIN, MARKER_BODY_END)?.trim()
            if (body != null) GikoParsed(title, body) else null
        }
    }

    // ==========================================
    // REVISION 解析
    // ==========================================
    fun parseRevisionOutput(output: String): List<RevisionParsed> {
        val result = mutableListOf<RevisionParsed>()
        if (output.isBlank()) return result

        val beginMarker = LpConstants.MarkerBegin[OperationKind.REVISION_FULL] ?: "<<<REVISION_PACK_BEGIN>>>"
        val endMarker = LpConstants.MarkerEnd[OperationKind.REVISION_FULL] ?: "<<<REVISION_PACK_END>>>"

        var targetArea = extractMarkerRange(output, beginMarker, endMarker)
        if (targetArea.isEmpty()) targetArea = output

        // @@@REVISION|n@@@ で分割
        val blocks = targetArea.split(Regex("""@@@\s*REVISION\s*\|\s*\d+\s*@@@""", RegexOption.IGNORE_CASE))

        for (block in blocks) {
            if (block.isBlank()) continue

            val dict = parseKeyValueBlock(block)
            val body = dict["REVISED_TEXT"] ?: dict["TEXT"] ?: dict["BODY"] ?: ""
            if (body.isBlank()) continue

            result.add(RevisionParsed(
                bodyText = normalizeBodyText(body),
                comment = dict["COMMENT"] ?: dict["NOTE"] ?: ""
            ))
        }

        // 旧形式フォールバック
        if (result.isEmpty()) {
            val itemsBlock = extractBetween(output, MARKER_ITEMS_BEGIN, MARKER_ITEMS_END)
            if (itemsBlock != null) {
                val items = itemsBlock.split(MARKER_ITEM_SEP)
                for (item in items) {
                    val body = extractTagContent(item, "BODY")
                        ?: extractBetween(item, MARKER_BODY_BEGIN, MARKER_BODY_END)
                        ?: continue
                    result.add(RevisionParsed(body.trim(), extractTagContent(item, "COMMENT") ?: ""))
                }
            } else {
                val body = extractBetween(output, MARKER_BODY_BEGIN, MARKER_BODY_END)
                if (body != null) {
                    result.add(RevisionParsed(body.trim(), ""))
                }
            }
        }

        return result
    }

    // ==========================================
    // ヘルパーメソッド
    // ==========================================

    private fun extractBetween(text: String, beginMarker: String, endMarker: String): String? {
        val beginPos = text.indexOf(beginMarker)
        if (beginPos == -1) return null

        val startPos = beginPos + beginMarker.length
        val endPos = text.indexOf(endMarker, startPos)
        if (endPos == -1) return null

        return text.substring(startPos, endPos)
    }

    private fun extractTagContent(text: String, tagName: String): String? {
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

    // 本文の正規化（WPF版 OutputFormatNormalizer 移植）
    private fun normalizeBodyText(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("""^[\s\n]+"""), "")  // 先頭の空白削除
            .replace(Regex("""[\s\n]+$"""), "")  // 末尾の空白削除
            .replace(Regex("""\n{3,}"""), "\n\n")  // 3連続以上の改行を2つに
    }

    // カンマ区切りリストの正規化
    private fun normalizeCommaList(text: String): String {
        return text
            .replace("、", ",")
            .replace("，", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
