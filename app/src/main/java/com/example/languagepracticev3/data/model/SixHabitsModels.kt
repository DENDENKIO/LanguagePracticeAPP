// app/src/main/java/com/example/languagepracticev3/data/model/SixHabitsModels.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 6つの思考習慣のマインドセットタイプ
 */
enum class MindsetType(
    val number: Int,
    val displayName: String,
    val description: String,
    val icon: String  // Material Icons名
) {
    WORLD_AS_MATERIAL(
        1,
        "世界を「素材」として見る",
        "タイトルをつける・視点変換・なぜ5回",
        "Visibility"
    ),
    METAPHOR_TRANSLATION(
        2,
        "比喩で世界を「翻訳」する",
        "新しい比喩・既存比喩の壊し・抽象→具体",
        "Translate"
    ),
    OBSERVATION_AS_DIALOGUE(
        3,
        "観察を「対話」として扱う",
        "10分観察・ネガティブスペース・対象への質問",
        "Forum"
    ),
    EXPERIENCE_ALCHEMY(
        4,
        "経験を「錬金術」で変換する",
        "3層記録・感情→感覚・失敗を素材に",
        "AutoAwesome"
    ),
    METACOGNITION(
        5,
        "メタ認知を育てる",
        "1日10回自問・友人への問い直し・採点",
        "Psychology"
    ),
    ROUTINE_AS_RITUAL(
        6,
        "ルーティンを「儀式」として設計",
        "聖域・始まりの儀式・終わりの儀式",
        "SelfImprovement"
    );

    companion object {
        fun fromNumber(number: Int): MindsetType? =
            entries.find { it.number == number }
    }
}

/**
 * 6つの思考習慣セッション
 */
@Entity(tableName = "six_habits_sessions")
data class SixHabitsSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 基本情報
    val mindsetType: Int = 1,  // MindsetType.number
    val sessionDate: String = "",

    // 各マインドセットの共通フィールド
    val practiceType: String = "",  // 具体的な練習タイプ
    val inputText: String = "",     // ユーザー入力
    val outputText: String = "",    // 生成/変換結果

    // マインドセット①用
    val sceneTitle: String = "",         // シーンタイトル
    val perspective1: String = "",       // 一人称視点
    val perspective2: String = "",       // 三人称視点
    val perspective3: String = "",       // 物の視点
    val whyChain: String = "",           // なぜ5回の連鎖（JSON形式）

    // マインドセット②用
    val originalMetaphor: String = "",   // 元の比喩
    val transformedMetaphor: String = "", // 変換後の比喩
    val abstractConcept: String = "",    // 抽象概念
    val concreteThing: String = "",      // 具体物への変換

    // マインドセット③用
    val observationTarget: String = "",  // 観察対象
    val observationNotes: String = "",   // 観察メモ（形・色・質感）
    val dialogueQuestion: String = "",   // 対象への質問
    val dialogueAnswer: String = "",     // 想像した答え
    val negativeSpace: String = "",      // ネガティブスペースの観察

    // マインドセット④用
    val factLayer: String = "",          // 事実の層
    val emotionLayer: String = "",       // 感情の層
    val universalLayer: String = "",     // 普遍の層
    val emotionToColor: String = "",     // 感情→色
    val emotionToSound: String = "",     // 感情→音
    val emotionToTexture: String = "",   // 感情→触感
    val failureAsStory: String = "",     // 失敗を素材に

    // マインドセット⑤用
    val selfQuestion: String = "",       // 自問内容
    val selfAnswer: String = "",         // 自答内容
    val friendAdvice: String = "",       // 友人へのアドバイス
    val dailyScore: Int = 0,             // 今日の自己採点
    val scoreReason1: String = "",       // 採点理由1
    val scoreReason2: String = "",       // 採点理由2
    val scoreReason3: String = "",       // 採点理由3
    val tomorrowPlan: String = "",       // 明日への改善

    // マインドセット⑥用
    val sacredSpace: String = "",        // 聖域の設計
    val startRitual: String = "",        // 始まりの儀式
    val endRitual: String = "",          // 終わりの儀式
    val ritualNotes: String = "",        // 儀式のメモ

    // メタデータ
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * 日次トラッキング
 */
@Entity(tableName = "six_habits_daily_tracking")
data class SixHabitsDailyTracking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: String = "",  // YYYY-MM-DD形式

    // マインドセット①のチェック
    val titleCount: Int = 0,           // タイトルをつけた回数
    val perspectiveChanged: Boolean = false,
    val whyChainDone: Boolean = false,

    // マインドセット②のチェック
    val newMetaphorCount: Int = 0,     // 新しい比喩を作った回数
    val metaphorTransformed: Boolean = false,
    val emotionToConcreteCount: Int = 0,

    // マインドセット③のチェック
    val observationMinutes: Int = 0,   // 観察した分数
    val negativeSpaceDone: Boolean = false,
    val objectQuestionDone: Boolean = false,

    // マインドセット④のチェック
    val threeLayerRecordDone: Boolean = false,
    val emotionToSenseDone: Boolean = false,
    val failureAsMaterialDone: Boolean = false,

    // マインドセット⑤のチェック
    val selfQuestionCount: Int = 0,    // 自問回数
    val friendAdviceDone: Boolean = false,
    val dailyScoringDone: Boolean = false,

    // マインドセット⑥のチェック
    val startRitualDone: Boolean = false,
    val endRitualDone: Boolean = false,

    // 総合スコア
    val totalScore: Int = 0,
    val notes: String = "",

    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * 蓄積された素材（比喩、感情変換など）
 */
@Entity(tableName = "six_habits_materials")
data class SixHabitsMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val materialType: String = "",  // "metaphor", "emotion_to_sense", "title", etc.
    val content: String = "",
    val category: String = "",      // 分類用タグ
    val sourceSessionId: Long = 0,

    val createdAt: String = "",
    val isFavorite: Boolean = false
)

/**
 * 各習慣の練習タイプ
 */
object SixHabitsPracticeTypes {
    // マインドセット①
    const val TITLE_NAMING = "title_naming"              // シーンにタイトルをつける
    const val PERSPECTIVE_SHIFT = "perspective_shift"    // 3視点で見る
    const val WHY_CHAIN = "why_chain"                    // なぜ5回

    // マインドセット②
    const val NEW_METAPHOR = "new_metaphor"              // 新しい比喩を作る
    const val TRANSFORM_METAPHOR = "transform_metaphor"  // 既存比喩を壊して作り直す
    const val ABSTRACT_TO_CONCRETE = "abstract_to_concrete" // 抽象→具体

    // マインドセット③
    const val TEN_MINUTE_OBSERVATION = "ten_minute_observation"  // 10分観察
    const val NEGATIVE_SPACE = "negative_space"          // ネガティブスペース
    const val QUESTION_TO_OBJECT = "question_to_object"  // 対象に質問

    // マインドセット④
    const val THREE_LAYER_RECORD = "three_layer_record"  // 3層記録
    const val EMOTION_TO_SENSE = "emotion_to_sense"      // 感情→五感
    const val FAILURE_AS_MATERIAL = "failure_as_material" // 失敗を素材に

    // マインドセット⑤
    const val SELF_QUESTIONING = "self_questioning"      // 自問自答
    const val FRIEND_ADVICE = "friend_advice"            // 友人へのアドバイス
    const val DAILY_SCORING = "daily_scoring"            // 今日の採点

    // マインドセット⑥
    const val SACRED_SPACE = "sacred_space"              // 聖域設計
    const val START_RITUAL = "start_ritual"              // 始まりの儀式
    const val END_RITUAL = "end_ritual"                  // 終わりの儀式
}