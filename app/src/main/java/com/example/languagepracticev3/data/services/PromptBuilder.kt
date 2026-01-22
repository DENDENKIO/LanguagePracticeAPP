package com.example.languagepracticev3.data.services

import com.example.languagepracticev3.data.model.LengthProfile
import com.example.languagepracticev3.data.model.OperationKind
import java.util.UUID

/**
 * WPF版LanguagePracticeのPromptBuilder.csをKotlinに移植
 */
class PromptBuilder {

    // ==========================================
    // 定数
    // ==========================================
    object LpConstants {
        const val DONE_SENTINEL = "⟦LP_DONE_9F3A2C⟧"

        val MarkerBegin = mapOf(
            OperationKind.READER_AUTO_GEN to "⟦READER_BEGIN⟧",
            OperationKind.TOPIC_GEN to "⟦TOPIC_BEGIN⟧",
            OperationKind.PERSONA_GEN to "⟦PERSONA_BEGIN⟧",
            OperationKind.OBSERVE_IMAGE to "⟦OBSERVE_BEGIN⟧",
            OperationKind.TEXT_GEN to "⟦TEXT_BEGIN⟧",
            OperationKind.STUDY_CARD to "⟦STUDYCARD_BEGIN⟧",
            OperationKind.CORE_EXTRACT to "⟦CORE_BEGIN⟧",
            OperationKind.REVISION_FULL to "⟦REVISION_BEGIN⟧",
            OperationKind.GIKO to "⟦GIKO_BEGIN⟧",
            OperationKind.PERSONA_VERIFY_ASSIST to "⟦VERIFY_BEGIN⟧",
            // 追加分
            OperationKind.ANALYZE to "⟦ANALYZE_BEGIN⟧",
            OperationKind.COMPARE to "⟦COMPARE_BEGIN⟧",
            OperationKind.OBSERVE to "⟦OBSERVE_BEGIN⟧",
            OperationKind.POETRY_DRAFT to "⟦POETRY_DRAFT_BEGIN⟧",
            OperationKind.POETRY_CORE to "⟦POETRY_CORE_BEGIN⟧",
            OperationKind.POETRY_REV to "⟦POETRY_REV_BEGIN⟧"
        )

        val MarkerEnd = mapOf(
            OperationKind.READER_AUTO_GEN to "⟦READER_END⟧",
            OperationKind.TOPIC_GEN to "⟦TOPIC_END⟧",
            OperationKind.PERSONA_GEN to "⟦PERSONA_END⟧",
            OperationKind.OBSERVE_IMAGE to "⟦OBSERVE_END⟧",
            OperationKind.TEXT_GEN to "⟦TEXT_END⟧",
            OperationKind.STUDY_CARD to "⟦STUDYCARD_END⟧",
            OperationKind.CORE_EXTRACT to "⟦CORE_END⟧",
            OperationKind.REVISION_FULL to "⟦REVISION_END⟧",
            OperationKind.GIKO to "⟦GIKO_END⟧",
            OperationKind.PERSONA_VERIFY_ASSIST to "⟦VERIFY_END⟧",
            // 追加分
            OperationKind.ANALYZE to "⟦ANALYZE_END⟧",
            OperationKind.COMPARE to "⟦COMPARE_END⟧",
            OperationKind.OBSERVE to "⟦OBSERVE_END⟧",
            OperationKind.POETRY_DRAFT to "⟦POETRY_DRAFT_END⟧",
            OperationKind.POETRY_CORE to "⟦POETRY_CORE_END⟧",
            OperationKind.POETRY_REV to "⟦POETRY_REV_END⟧"
        )
    }

    // ==========================================
    // 共通ヘッダ
    // ==========================================
    private fun getCommonHeader(): String {
        return """
【絶対禁止（出力）】
- Web検索結果の表示、URL/リンク/出典の列挙は禁止。
- Markdownは禁止。
- 前置き、断り書き、メタ発言（「〜します」「AIとして」等）は禁止。
- マーカー外への出力は禁止（成果物は必ず指定マーカー内のみ）。

【共通原則】
- 指示された出力フォーマット（キー名、順序、区切り）を厳守する。
- 不明な場合は推測で断定せず、指定フォーマットの中で「UNCLEAR」等の許容表現を用いる。
- 出力の最後に必ず次の終端文字列を、そのまま出力する：${LpConstants.DONE_SENTINEL}
        """.trimIndent()
    }

    private fun getLengthInstruction(profile: LengthProfile): Triple<String, Int, Int> {
        // LengthProfileに定義されたminChars/maxCharsを使用
        val min = profile.minChars
        val max = profile.maxChars

        val instruction = """
【文字量（改行除く文字数）】
- 本文は指定レンジに収めること（±10〜15%の軽微な誤差は許容）。
- 文字数調整のために内容の核を薄めないこと。
RANGE: ${min}〜${max}字
        """.trimIndent()
        return Triple(instruction, min, max)
    }

    // ==========================================
    // メインのビルド関数
    // ==========================================
    fun buildPrompt(
        operation: OperationKind,
        writer: String = "",
        topic: String = "",
        reader: String = "",
        length: LengthProfile = LengthProfile.STUDY_SHORT,
        sourceText: String = "",
        imageUrl: String = "",
        genre: String = "",
        toneLabel: String = "",
        toneRule: String = "",
        coreTheme: String = "",
        coreEmotion: String = "",
        coreTakeaway: String = "",
        coreSentence: String = "",
        // Persona検証用
        personaName: String = "",
        personaBio: String = "",
        evidence1: String = "",
        evidence2: String = "",
        evidence3: String = "",
        // Reader自動生成用
        contextKind: String = ""
    ): String {
        return when (operation) {
            OperationKind.READER_AUTO_GEN -> buildReaderAutoPrompt(contextKind)
            OperationKind.TOPIC_GEN -> buildTopicGenPrompt(imageUrl)
            OperationKind.PERSONA_GEN -> buildPersonaGenPrompt(genre)
            OperationKind.OBSERVE_IMAGE -> buildObserveImagePrompt(imageUrl)
            OperationKind.TEXT_GEN -> buildTextGenPrompt(writer, topic, reader, toneLabel, length)
            OperationKind.STUDY_CARD -> buildStudyCardPrompt(reader, toneLabel, sourceText)
            OperationKind.CORE_EXTRACT -> buildCoreExtractPrompt(reader, sourceText)
            OperationKind.REVISION_FULL -> buildRevisionFullPrompt(sourceText, coreTheme, coreEmotion, coreTakeaway, reader, coreSentence)
            OperationKind.GIKO -> buildGikoPrompt(toneLabel, toneRule, reader, topic, sourceText)
            OperationKind.PERSONA_VERIFY_ASSIST -> buildPersonaVerifyPrompt(personaName, personaBio, evidence1, evidence2, evidence3)
            // 追加された操作に対応
            OperationKind.ANALYZE -> buildAnalyzePrompt(sourceText, reader)
            OperationKind.COMPARE -> buildComparePrompt(sourceText, reader)
            OperationKind.OBSERVE -> buildObservePrompt(sourceText)
            OperationKind.POETRY_DRAFT -> buildPoetryDraftPrompt(topic, writer, reader, length)
            OperationKind.POETRY_CORE -> buildPoetryCorePrompt(sourceText)
            OperationKind.POETRY_REV -> buildPoetryRevPrompt(sourceText, coreTheme, coreEmotion)
        }
    }

    // ==========================================
    // 追加: ANALYZE
    // ==========================================
    private fun buildAnalyzePrompt(sourceText: String, reader: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.ANALYZE]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.ANALYZE]

        return """
${getCommonHeader()}

あなたは「テキスト分析者」です。入力されたテキストを分析してください。

【入力】
READER：$reader
SOURCE_TEXT：
$sourceText

【出力フォーマット（厳守）】
$markerBegin
SUMMARY：<要約>
THEMES：<主題のリスト>
TONE：<文体の特徴>
STRENGTHS：<良い点>
IMPROVEMENTS：<改善点>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // 追加: COMPARE
    // ==========================================
    private fun buildComparePrompt(sourceText: String, reader: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.COMPARE]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.COMPARE]

        return """
${getCommonHeader()}

あなたは「テキスト比較者」です。入力されたテキストを比較分析してください。

【入力】
READER：$reader
SOURCE_TEXT：
$sourceText

【出力フォーマット（厳守）】
$markerBegin
SIMILARITIES：<共通点>
DIFFERENCES：<相違点>
RECOMMENDATION：<推奨>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // 追加: OBSERVE
    // ==========================================
    private fun buildObservePrompt(sourceText: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.OBSERVE]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.OBSERVE]

        return """
${getCommonHeader()}

あなたは「観察者」です。入力されたテキストや対象を観察してノートを作成してください。

【入力】
SOURCE：
$sourceText

【出力フォーマット（厳守）】
$markerBegin
VISUAL：<視覚的観察>
SOUND：<聴覚的観察>
OTHER_SENSES：<その他の感覚>
IMPRESSIONS：<印象・所感>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // 追加: POETRY_DRAFT
    // ==========================================
    private fun buildPoetryDraftPrompt(
        topic: String,
        writer: String,
        reader: String,
        length: LengthProfile
    ): String {
        val (lengthInstruction, min, max) = getLengthInstruction(length)
        val markerBegin = LpConstants.MarkerBegin[OperationKind.POETRY_DRAFT]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.POETRY_DRAFT]

        return """
${getCommonHeader()}
$lengthInstruction

あなたは「詩の作成者」です。指定されたお題で詩の初稿を作成してください。

【入力】
TOPIC：$topic
WRITER：$writer
READER：$reader
LENGTH：${min}〜${max}字

【出力フォーマット（厳守）】
$markerBegin
TITLE：<タイトル>
DRAFT：
<詩の本文>

NOTES：<制作メモ>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // 追加: POETRY_CORE
    // ==========================================
    private fun buildPoetryCorePrompt(sourceText: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.POETRY_CORE]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.POETRY_CORE]

        return """
${getCommonHeader()}

あなたは「詩の核抽出者」です。入力された詩から核となる要素を抽出してください。

【入力】
SOURCE_POEM：
$sourceText

【出力フォーマット（厳守）】
$markerBegin
THEME：<主題>
EMOTION：<感情>
KEY_IMAGES：<重要なイメージ>
CORE_LINE：<核となる一行>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // 追加: POETRY_REV
    // ==========================================
    private fun buildPoetryRevPrompt(
        sourceText: String,
        coreTheme: String,
        coreEmotion: String
    ): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.POETRY_REV]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.POETRY_REV]

        return """
${getCommonHeader()}

あなたは「詩の推敲者」です。核を維持しながら詩を推敲してください。

【不変条件】
THEME：$coreTheme
EMOTION：$coreEmotion

【入力】
SOURCE_POEM：
$sourceText

【出力フォーマット（厳守）】
$markerBegin
REVISED_POEM：
<推敲後の詩>

CHANGES：<変更点の説明>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-1. READER_AUTO_GEN
    // ==========================================
    fun buildReaderAutoPrompt(contextKind: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.READER_AUTO_GEN]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.READER_AUTO_GEN]

        return """
${getCommonHeader()}

あなたは「読者像（READER）を1つだけ決める担当」です。

【入力】
- 作品種別：$contextKind

【生成ルール】
- 読者像は必ず1つだけ。
- 1〜2文で、人物像＋読む状況（時間・気分）＋読書体力（平易/普通/文学寄りのいずれかのニュアンス）を含める。
- 汎用的すぎる「一般の読者」だけは避け、状況を具体化する。

【出力フォーマット（厳守）】
$markerBegin
READER：<1〜2文。1つだけ>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-2. TOPIC_GEN
    // ==========================================
    fun buildTopicGenPrompt(imageUrl: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.TOPIC_GEN]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.TOPIC_GEN]

        val imageRef = if (imageUrl.isBlank()) {
            "【画像参照】なし（AIの独創性にお任せ。季節感のある具体的な情景を想定）"
        } else {
            """【画像参照（最優先）】
次のURL先の画像を見て、雰囲気、時間帯、場所、モチーフを反映したお題を生成すること。
URL: $imageUrl"""
        }

        return """
${getCommonHeader()}

あなたは「詳細お題ジェネレーター」です。以下の仕様で詳細お題を生成してください。

$imageRef

【目的】
- 作品生成・学習に使うため、余白が入りすぎないよう物理条件（FIX）を固定する。

【出力件数】
- 可能なら3〜5件（ただしフォーマット崩れを起こさないことを最優先）

【出力フォーマット（厳守）】
$markerBegin
@@@TOPIC|1@@@
TITLE：<1行。余白のない具体。抽象語禁止>
EMOTION：<1〜3個>
SCENE：<STATIC/DYNAMIC/MIX>
TAGS：<カンマ区切りで3〜8個>
FIX：
- PLACE：<具体地>
- TIME：<季節＋時刻>
- WEATHER：<雨/風/気温など固定>
- LIGHT：<光源/強さ>
- SOUND：<音>
- OBJECTS：<登場物>
@@@TOPIC_END@@@
（以下繰り返し）
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-3. PERSONA_GEN
    // ==========================================
    fun buildPersonaGenPrompt(genre: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.PERSONA_GEN]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.PERSONA_GEN]
        val genreInstruction = if (genre.isBlank()) "指定なし（適度に分散）" else genre

        return """
${getCommonHeader()}

あなたは文学・人物データベースの構築者です。指定された分野の人物について詳細情報を生成してください。

【指定分野】
$genreInstruction

【生成ルール】
- 実在人物（日本・海外を含む）を扱うこと。
- ただし、確実でない事実を断定しない（曖昧ならその旨をBIOに含める）。
- 人数は可能なら5名。

【出力フォーマット（厳守：マーカー内のみ）】
$markerBegin
@@@PERSONA|1@@@
NAME：<実名（よみ/通称があれば併記）>
LOCATION：<国籍、または主な活動場所/ゆかりの地>
BIO：<経歴・人物像・エピソードを100〜200字目安>
STYLE：<文体・特徴・癖・好むモチーフなどを具体的に>
TAGS：<カンマ区切り。検索用。3〜8個>
@@@PERSONA_END@@@
（以下繰り返し）
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-4. OBSERVE_IMAGE
    // ==========================================
    fun buildObserveImagePrompt(imageUrl: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.OBSERVE_IMAGE]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.OBSERVE_IMAGE]

        return """
${getCommonHeader()}

あなたは「観察ノート生成者」です。画像から読み取れる情報をもとに、五感描写・比喩・心象変換の素材を作成してください。

【画像参照（最優先）】
URL: $imageUrl
※出力にURLは書かない。

【目的】
- 五感描写・比喩の素材集め（観察ノート中心）。
- 最後に「核の一文候補」を3つ出す。

【出力フォーマット（厳守）】
$markerBegin
IMAGE_MOTIF：<見えるモチーフを5〜12語>
VISUAL：<視覚の箇条書き 5〜10>
SOUND：<音の仮説 2〜5>
SMELL_AIR：<匂い/空気感の仮説 2〜5>
TOUCH_TEMP：<触覚/温度の仮説 2〜5>
WEIGHT_BODY：<重さ/身体感覚の仮説 1〜3>

METAPHORS：
- <比喩1（感覚→比喩）>
- <比喩2>
- <比喩3>
- <比喩4>
- <比喩5>

TRANSFORM：
- <比喩→感情/状況へ変容1>
- <変容2>
- <変容3>

CORE_SENTENCE_CANDIDATES：
- <核の一文候補1>
- <候補2>
- <候補3>

TAGS：<カンマ区切りで3〜8個>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-5. TEXT_GEN
    // ==========================================
    fun buildTextGenPrompt(
        writer: String,
        topic: String,
        readerResolved: String,
        tone: String,
        lengthProfile: LengthProfile
    ): String {
        val (lengthInstruction, min, max) = getLengthInstruction(lengthProfile)
        val markerBegin = LpConstants.MarkerBegin[OperationKind.TEXT_GEN]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.TEXT_GEN]

        val toneInstruction = if (tone.isBlank()) "指定なし（通常の現代日本語）" else tone
        val writerInstruction = if (writer.isBlank()) "AIにお任せ" else writer
        val isTopicEmpty = topic.isBlank()

        val seed = UUID.randomUUID().toString().replace("-", "").take(10)

        val readerSection = if (readerResolved.isNotBlank()) {
            """
【読者像（READER）】
- 指定された読者像に合わせて、語彙の難度、比喩の密度、説明量、余韻の長さを調整する。
- 読者像を勝手に変更しない（READERは入力を尊重）。
            """.trimIndent()
        } else ""

        val topicAutoSection = if (isTopicEmpty) {
            """
【TOPICが空のとき（重要）】
- TOPICはAIが「新規に自作」する（こちらから候補は与えない）。
- ただし毎回似た発想に寄らないように、下のSEEDを乱数種として使い、発想を意図的に散らす。
SEED：$seed
            """.trimIndent()
        } else ""

        return """
${getCommonHeader()}
$lengthInstruction
$readerSection

あなたは文章生成者です。指定された読者像に合わせて、作品（短文〜長文）を生成してください。

【入力】
WRITER：$writerInstruction
TOPIC：${if (isTopicEmpty) "(AUTO_CREATE)" else topic}
READER：$readerResolved
TONE：$toneInstruction
LENGTH_PROFILE：$lengthProfile
LENGTH_RANGE：${min}〜${max}字（改行除く）

$topicAutoSection

【出力フォーマット（厳守）】
$markerBegin
WRITER：<実際に用いた書き手>
READER：$readerResolved
TOPIC：<実際に用いたお題>
LENGTH_PROFILE：$lengthProfile
TEXT：
<本文>

TECHNIQUE_MEMO：
- <五感>
- <比喩>
- <音調>
- <省略/余韻>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-6. STUDY_CARD
    // ==========================================
    fun buildStudyCardPrompt(readerResolved: String, tone: String, sourceText: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.STUDY_CARD]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.STUDY_CARD]
        val toneInstruction = if (tone.isBlank()) "なし" else tone

        return """
${getCommonHeader()}

あなたは「学習カード作成者」です。入力本文を分析し、学習可能なカードに分解してください。

【入力】
READER：$readerResolved
TONE：$toneInstruction
SOURCE_TEXT：
$sourceText

【出力フォーマット（厳守）】
$markerBegin
META：
- READER：$readerResolved
- TONE：$toneInstruction
- FOCUS：<効きの要約>
- LEVEL：<EASY|NORMAL|LITERARY>

BEST_EXPRESSIONS：
- <一文1>
- <一文2>
- <一文3>

TAGS：<カンマ区切り>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-7. CORE_EXTRACT
    // ==========================================
    fun buildCoreExtractPrompt(readerResolved: String, sourceText: String): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.CORE_EXTRACT]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.CORE_EXTRACT]

        return """
${getCommonHeader()}

あなたは「文章の核（Core）を抽出する担当」です。

【入力】
READER：$readerResolved
TEXT：
$sourceText

【出力フォーマット（厳守）】
$markerBegin
THEME：...
EMOTION：...
TAKEAWAY：...
READER：$readerResolved
CORE_SENTENCE：...
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-8. REVISION_FULL
    // ==========================================
    fun buildRevisionFullPrompt(
        sourceText: String,
        coreTheme: String,
        coreEmotion: String,
        coreTakeaway: String,
        coreReader: String,
        coreSentence: String
    ): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.REVISION_FULL]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.REVISION_FULL]

        return """
${getCommonHeader()}

あなたは「推敲者」です。核を維持し、本文を改稿してください。

【不変条件】
THEME：$coreTheme
EMOTION：$coreEmotion
TAKEAWAY：$coreTakeaway
READER：$coreReader
CORE_SENTENCE：$coreSentence

【対象本文】
$sourceText

【出力フォーマット（厳守）】
$markerBegin
REVISED_TEXT：
...

CHANGES：<変更点>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-9. GIKO
    // ==========================================
    fun buildGikoPrompt(
        toneLabel: String,
        toneRuleText: String,
        readerOrEmpty: String,
        topicLineOrEmpty: String,
        inputBody: String
    ): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.GIKO]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.GIKO]

        return """
${getCommonHeader()}

あなたは古典文体の専門家です。入力本文を指定文調に書き換えてください。

【入力】
TONE：$toneLabel
READER：$readerOrEmpty
お題：$topicLineOrEmpty

【入力本文】
$inputBody

【文調ルール】
$toneRuleText

【出力形式（厳守）】
$markerBegin
TONE：$toneLabel
GIKO_TEXT：
...
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }

    // ==========================================
    // K-10. PERSONA_VERIFY_ASSIST
    // ==========================================
    fun buildPersonaVerifyPrompt(
        personaName: String,
        personaBio: String,
        e1: String,
        e2: String,
        e3: String
    ): String {
        val markerBegin = LpConstants.MarkerBegin[OperationKind.PERSONA_VERIFY_ASSIST]
        val markerEnd = LpConstants.MarkerEnd[OperationKind.PERSONA_VERIFY_ASSIST]

        return """
${getCommonHeader()}

あなたは「人物プロフィールの検証支援者」です。

【対象Persona】
NAME：$personaName
BIO：$personaBio

【根拠テキスト】
E1：$e1
E2：$e2
E3：$e3

【出力フォーマット（厳守）】
$markerBegin
TARGET：
NAME：$personaName

VERDICT：SUPPORTED|NOT_SUPPORTED|CONTRADICTED|UNCLEAR
REASON：<理由>
$markerEnd

（最後に必ず ${LpConstants.DONE_SENTINEL} を出力）
        """.trimIndent()
    }
}
