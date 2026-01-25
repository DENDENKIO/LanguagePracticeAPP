// app/src/main/java/com/example/languagepracticev3/data/model/GlobalRevisionSession.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * グローバル・リビジョンセッション
 * Hayes らの研究に基づく3段階認知モデルを実装
 */
@Entity(tableName = "global_revision_sessions")
data class GlobalRevisionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 作品情報
    val workTitle: String = "",
    val originalText: String = "",

    // Step 1: 核の文を決める
    val coreSentence: String = "",           // 伝えたいことを一文で
    val coreTheme: String = "",              // 主題
    val coreEmotion: String = "",            // 中心感情・態度
    val coreTakeaway: String = "",           // 読者に渡したい変化・問い

    // Step 2: 問題の検出 (Detection)
    val detectedProblems: String = "",       // 検出した問題リスト (JSON形式)

    // Step 3: 問題の診断 (Diagnosis)
    val diagnosisContent: String = "",       // 内容レベルの診断
    val diagnosisStructure: String = "",     // 構成レベルの診断
    val diagnosisReader: String = "",        // 読者レベルの診断
    val diagnosisStyle: String = "",         // 文体レベルの診断

    // Step 4: 大規模修正案 (Revision)
    val revisionPlans: String = "",          // 修正案リスト (JSON形式)
    val revisionPriority: String = "",       // 優先順位

    // Step 5: リバース・アウトライン
    val reverseOutline: String = "",         // 段落要約リスト (JSON形式)
    val structureNotes: String = "",         // 構成に関するメモ

    // 最終成果物
    val revisedText: String = "",            // 推敲後のテキスト

    // メタデータ
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * 検出した問題
 */
data class DetectedProblem(
    val id: Int,
    val description: String,
    val location: String = ""  // 「冒頭」「3段落目」など
)

/**
 * 修正案
 */
data class RevisionPlan(
    val id: Int,
    val target: String,        // 修正対象
    val before: String,        // 変更前
    val after: String,         // 変更後
    val reason: String,        // 理由
    val priority: Int = 0      // 優先度 (1が最高)
)

/**
 * リバース・アウトライン項目
 */
data class OutlineItem(
    val paragraphNumber: Int,
    val summary: String,       // 段落の要点を1文で
    val contributeToCore: Boolean = true,  // 核に貢献しているか
    val notes: String = ""     // メモ
)

/**
 * グローバル・リビジョンのステップ
 */
enum class GlobalRevisionStep(val displayName: String, val description: String) {
    CORE_DEFINITION("核の文を決める", "この文章で伝えたいことを一文で書く"),
    DETECTION("問題の検出", "うまくいっていない点を批判的に読み取る"),
    DIAGNOSIS("問題の診断", "検出した問題の原因を4レベルで分析"),
    REVISION_PLAN("大規模修正案", "診断に基づいて具体的な修正案を立てる"),
    REVERSE_OUTLINE("リバース・アウトライン", "段落ごとの要点を整理し構成を見直す")
}