// app/src/main/java/com/example/languagepracticev3/data/model/OperationKind.kt
package com.example.languagepracticev3.data.model

/**
 * 操作種別 Enum
 * WPF版 Helpers/Constants.cs の OperationKind をKotlinに移植
 */
enum class OperationKind(val displayName: String) {
    READER_AUTO_GEN("読者像自動生成"),
    TOPIC_GEN("トピック生成"),
    PERSONA_GEN("ペルソナ生成"),
    OBSERVE_IMAGE("画像観察"),
    TEXT_GEN("テキスト生成"),
    GIKO("擬古文変換"),
    STUDY_CARD("学習カード作成"),
    CORE_EXTRACT("核抽出"),
    REVISION_FULL("全文推敲"),
    PERSONA_VERIFY_ASSIST("ペルソナ検証支援"),
    PRACTICE_SESSION("練習セッション"),
    // MindsetLab用
    MS_PLAN_GEN("MS計画生成"),
    MS_REVIEW_SCORE("MSレビュースコア");

    companion object {
        fun fromName(name: String): OperationKind {
            return entries.find { it.name == name } ?: TEXT_GEN
        }
    }
}

/**
 * 実行ステータス
 */
enum class RunStatus {
    SUCCESS,
    FAILED,
    REPAIRED_SUCCESS,
    REPAIRED_FAILED,
    CANCELLED,
    SKIPPED
}

/**
 * 作品種別
 */
enum class WorkKind {
    TEXT_GEN,
    GIKO,
    REVISION,
    USER_PRACTICE,
    STUDY_DERIVED,
    ANALYSIS
}

/**
 * 検証ステータス
 */
enum class VerificationStatus {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VERIFIED,
    DISPUTED
}

/**
 * 読みやすさレベル
 */
enum class ReadingLevel {
    EASY,
    NORMAL,
    LITERARY
}

// ※ LengthProfile は LengthProfile.kt で定義（重複を避ける）
