// app/src/main/java/com/example/languagepracticev3/data/model/AbstractionModels.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 抽象化テクニックのステップ
 */
enum class AbstractionStep(val displayName: String, val description: String) {
    CONCRETE_SCENE(
        "具体的な情景",
        "何が起きた/見えたか、具体的に書く"
    ),
    DEEP_QUESTIONING(
        "つっこみを入れる",
        "本質を問う質問に答える"
    ),
    ABSTRACTION(
        "抽象化する",
        "場面が象徴する本質を一文で表現"
    ),
    SENSORY_DETAILS(
        "感覚的詳細",
        "5つの感覚で具体化（Show, Don't Tell）"
    ),
    METAPHOR(
        "メタファー検討",
        "新しい視点を提供する比喩を探す"
    )
}

/**
 * 抽象化テクニック セッション
 */
@Entity(tableName = "abstraction_sessions")
data class AbstractionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // セッション情報
    val sessionTitle: String = "",
    val currentStep: Int = 0,

    // Step 1: 具体的な情景
    val concreteScene: String = "",
    val sceneWho: String = "",       // 誰が
    val sceneWhat: String = "",      // 何を
    val sceneWhere: String = "",     // どこで
    val sceneWhen: String = "",      // いつ

    // Step 2: つっこみを入れる
    val questionWhatItShows: String = "",      // 「これは何を示しているのか？」
    val questionWhyImpressive: String = "",    // 「なぜそれが印象に残ったのか？」
    val questionWhatToFeel: String = "",       // 「読者に何を感じてほしいのか？」
    val questionWhoDecided: String = "",       // 「誰がそう決めたの？」
    val questionByWhatStandard: String = "",   // 「どんな基準で？」
    val questionSpecifically: String = "",     // 「具体的には？」

    // Step 3: 抽象化
    val abstractedSentence: String = "",       // 抽象化した一文
    val coreTheme: String = "",                // 主題
    val coreEmotion: String = "",              // 中心感情

    // Step 4: 感覚的詳細 (Show, Don't Tell)
    val sensoryVisual: String = "",            // 視覚
    val sensoryAuditory: String = "",          // 聴覚
    val sensoryTactile: String = "",           // 触覚
    val sensoryOlfactory: String = "",         // 嗅覚
    val sensoryGustatory: String = "",         // 味覚
    val povCharacter: String = "",             // 視点人物
    val povFocus: String = "",                 // 視点人物が注目するもの
    val povIgnore: String = "",                // 視点人物が見落とすもの

    // Step 5: メタファー
    val metaphorCandidate1: String = "",       // メタファー候補1
    val metaphorCandidate2: String = "",       // メタファー候補2
    val metaphorCandidate3: String = "",       // メタファー候補3
    val selectedMetaphor: Int = 0,             // 選択したメタファー (1, 2, or 3)
    val metaphorReason: String = "",           // 選択理由

    // 最終成果物
    val finalText: String = "",                // 統合された文章

    // メタデータ
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * つっこみ質問のテンプレート
 */
object AbstractionQuestionTemplates {
    val deepQuestions = listOf(
        "これは何を示しているのか？",
        "なぜそれが印象に残ったのか？",
        "読者に何を感じてほしいのか？"
    )

    val specificQuestions = listOf(
        "誰が？",
        "いつ？",
        "どこで？",
        "何が？",
        "なぜ？",
        "どのように？"
    )

    val challengeQuestions = listOf(
        "誰がそう決めたの？",
        "どんな基準で？",
        "具体的には？",
        "本当にそうなの？"
    )
}

/**
 * 感覚詳細のガイド
 */
object SensoryGuide {
    data class SensoryPrompt(
        val sense: String,
        val question: String,
        val example: String
    )

    val prompts = listOf(
        SensoryPrompt(
            "視覚",
            "読者の目に映像が浮かぶ描写は？",
            "例: 「雲一つない真っ青な空は、海と一体になってどこまでも広がっているようだった」"
        ),
        SensoryPrompt(
            "聴覚",
            "その場面で聞こえる音は？",
            "例: 「彼女のヒールが磨かれた大理石の床に鋭い音を立てた」"
        ),
        SensoryPrompt(
            "触覚",
            "身体で感じる感覚は？",
            "例: 「ベンチの冷たさが骨に染みて、その感覚が永遠に続くように思えた」"
        ),
        SensoryPrompt(
            "嗅覚",
            "その場の匂いは？",
            "例: 「湿った土の匂いと、長く人が住んでいない家特有のカビと麻布の匂いが混ざっていた」"
        ),
        SensoryPrompt(
            "味覚",
            "味を通じて伝えられることは？",
            "例: 「水は錆の味がした」"
        )
    )
}

/**
 * メタファー生成のヒント
 */
object MetaphorGuide {
    val avoidClicheList = listOf(
        "心が重い",
        "光が見える",
        "絶望は暗い森",
        "希望は光",
        "愛は火",
        "時は金なり"
    )

    val creativePrompts = listOf(
        "この体験を自然現象に例えると？",
        "この感情を日常の物体に例えると？",
        "この瞬間を場所に例えると？",
        "この状況を身体の動きに例えると？"
    )
}