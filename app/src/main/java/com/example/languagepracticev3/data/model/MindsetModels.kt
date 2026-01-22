package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "ms_day")
data class MsDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String = "",
    val focusMindsets: String = "",
    val scene: String = "",
    val startRitual: String? = null,
    val endRitual: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun getFocusMindsetList(): List<Int> {
        if (focusMindsets.isBlank()) return emptyList()
        return focusMindsets.split(",", "、")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..6 }
    }
}

@Entity(
    tableName = "ms_entry",
    foreignKeys = [
        ForeignKey(
            entity = MsDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MsEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long = 0,
    val entryType: String = "",
    val bodyText: String = "",
    val createdAt: String = ""
)

@Entity(
    tableName = "ms_ai_step_log",
    foreignKeys = [
        ForeignKey(
            entity = MsDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MsAiStepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long = 0,
    val stepName: String = "",
    val promptText: String = "",
    val rawOutput: String = "",
    val parsedJson: String = "",
    val status: String = "",
    val createdAt: String = "",
    val finishedAt: String? = null
)

@Entity(
    tableName = "ms_review",
    foreignKeys = [
        ForeignKey(
            entity = MsDay::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MsReview(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long = 0,
    val totalScore: Int = 0,
    val subscoresJson: String = "",
    val strengths: String = "",
    val weaknesses: String = "",
    val nextDayPlan: String = "",
    val coreLink: String = "",
    val createdAt: String = ""
)

@Entity(tableName = "ms_export_log")
data class MsExportLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long = 0,
    val filePath: String = "",
    val createdAt: String = ""
)

data class MsPlanResult(
    val focusMindsets: List<Int> = emptyList(),
    val scene: String = "",
    val tasks: List<String> = emptyList(),
    val startRitual: String? = null,
    val endRitual: String? = null
)

data class MsReviewResult(
    val totalScore: Int = 0,
    val subscores: Map<String, Int> = emptyMap(),
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val nextDayPlan: String = "",
    val coreLink: String = ""
)

data class DrillDef(
    val entryType: String = "",
    val title: String = "",
    val hint: String = ""
)

data class MindsetInfo(
    val id: Int,
    val name: String = "",
    val shortName: String = "",
    val drills: List<DrillDef> = emptyList()
)

object MindsetDefinitions {
    val all: Map<Int, MindsetInfo> = mapOf(
        1 to MindsetInfo(
            id = 1,
            name = "世界を素材として見る",
            shortName = "素材化",
            drills = listOf(
                DrillDef("A1_TITLE", "場面にタイトル", "最低3つ"),
                DrillDef("A2_VIEWPOINT", "視点3通り", "1人称/三人称/物の視点"),
                DrillDef("A3_WHY5", "Why×5", "1テーマを深掘り")
            )
        ),
        2 to MindsetInfo(
            id = 2,
            name = "比喩で翻訳",
            shortName = "比喩",
            drills = listOf(
                DrillDef("B1_METAPHOR", "新しい比喩を1つ", "比喩で翻訳"),
                DrillDef("B2_DESTROY", "既存比喩を壊して3変形", "比喩で翻訳"),
                DrillDef("B3_ABSTRACT", "抽象→具体物", "孤独/不安/希望など")
            )
        ),
        3 to MindsetInfo(
            id = 3,
            name = "観察を対話として扱う",
            shortName = "観察",
            drills = listOf(
                DrillDef("C1_OBSERVE10", "1物10分観察", "形/質感/語りかけ"),
                DrillDef("C2_NEGATIVE", "ネガティブ・スペース記述", "間・距離・余白"),
                DrillDef("C3_QUESTION", "対象に質問", "最低3問")
            )
        ),
        4 to MindsetInfo(
            id = 4,
            name = "経験を錬金術で変換",
            shortName = "錬金術",
            drills = listOf(
                DrillDef("D1_ALCHEMY", "3層記録", "事実/感情/普遍"),
                DrillDef("D2_SYNESTHESIA", "感情→色/音/触感へ変換", "共感覚的変換"),
                DrillDef("D3_FAILURE", "失敗を素材化", "物語の一部にする")
            )
        ),
        5 to MindsetInfo(
            id = 5,
            name = "メタ認知（第二の自分）",
            shortName = "メタ認知",
            drills = listOf(
                DrillDef("E1_NOWLOG", "今なにしてる？ログ", "3回以上、目標10回"),
                DrillDef("E2_FRIEND", "友人に助言するなら？", "客観視"),
                DrillDef("E3_SCORE", "10点採点＋理由3つ", "自己評価")
            )
        ),
        6 to MindsetInfo(
            id = 6,
            name = "ルーティンを儀式化",
            shortName = "儀式化",
            drills = listOf(
                DrillDef("F1_SANCTUARY", "聖域定義", "場所/条件"),
                DrillDef("F2_START", "始まりの儀式", "固定動作3つ"),
                DrillDef("F3_END", "終わりの儀式", "固定動作2つ"),
                DrillDef("F4_PLAN", "明日の実行計画", "時間ブロック")
            )
        )
    )

    fun getMindsetName(id: Int): String {
        return all[id]?.name ?: "マインドセット$id"
    }

    fun getMindsetShortName(id: Int): String {
        return all[id]?.shortName ?: "M$id"
    }

    fun getAllDrills(): List<DrillDef> {
        return all.values.flatMap { it.drills }
    }

    fun getDrillsByMindset(mindsetId: Int): List<DrillDef> {
        return all[mindsetId]?.drills ?: emptyList()
    }
}