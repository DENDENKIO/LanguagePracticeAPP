// app/src/main/java/com/example/languagepracticev3/data/model/LpConstants.kt
package com.example.languagepracticev3.data.model

/**
 * 定数・マーカー定義（付録A・H・K関連）
 * WPF版 Helpers/Constants.cs をKotlinに移植
 */
object LpConstants {
    // 終端文字列
    const val DONE_SENTINEL = "⟦LP_DONE_9F3A2C⟧"

    // マーカー定義（開始）
    val MarkerBegin: Map<OperationKind, String> = mapOf(
        OperationKind.READER_AUTO_GEN to "<<<READER_BEGIN>>>",
        OperationKind.TOPIC_GEN to "<<<TOPIC_PACK_BEGIN>>>",
        OperationKind.PERSONA_GEN to "<<<PERSONA_PACK_BEGIN>>>",
        OperationKind.OBSERVE_IMAGE to "<<<OBSERVATION_BEGIN>>>",
        OperationKind.TEXT_GEN to "<<<TEXT_GEN_BEGIN>>>",
        OperationKind.GIKO to "<<<GIKO_BEGIN>>>",
        OperationKind.STUDY_CARD to "<<<STUDY_CARD_BEGIN>>>",
        OperationKind.CORE_EXTRACT to "<<<CORE_BEGIN>>>",
        OperationKind.REVISION_FULL to "<<<REVISION_PACK_BEGIN>>>",
        OperationKind.PERSONA_VERIFY_ASSIST to "<<<PERSONA_VERIFY_BEGIN>>>",
        // MindsetLab用
        OperationKind.MS_PLAN_GEN to "---BEGIN MS_PLAN_GEN---",
        OperationKind.MS_REVIEW_SCORE to "---BEGIN MS_REVIEW_SCORE---"
    )

    // マーカー定義（終了）
    val MarkerEnd: Map<OperationKind, String> = mapOf(
        OperationKind.READER_AUTO_GEN to "<<<READER_END>>>",
        OperationKind.TOPIC_GEN to "<<<TOPIC_PACK_END>>>",
        OperationKind.PERSONA_GEN to "<<<PERSONA_PACK_END>>>",
        OperationKind.OBSERVE_IMAGE to "<<<OBSERVATION_END>>>",
        OperationKind.TEXT_GEN to "<<<TEXT_GEN_END>>>",
        OperationKind.GIKO to "<<<GIKO_END>>>",
        OperationKind.STUDY_CARD to "<<<STUDY_CARD_END>>>",
        OperationKind.CORE_EXTRACT to "<<<CORE_END>>>",
        OperationKind.REVISION_FULL to "<<<REVISION_PACK_END>>>",
        OperationKind.PERSONA_VERIFY_ASSIST to "<<<PERSONA_VERIFY_END>>>",
        // MindsetLab用
        OperationKind.MS_PLAN_GEN to "---END MS_PLAN_GEN---",
        OperationKind.MS_REVIEW_SCORE to "---END MS_REVIEW_SCORE---"
    )

    // MindsetLab用マーカー（直接参照用）
    const val MS_PLAN_BEGIN = "---BEGIN MS_PLAN_GEN---"
    const val MS_PLAN_END = "---END MS_PLAN_GEN---"
    const val MS_REVIEW_BEGIN = "---BEGIN MS_REVIEW_SCORE---"
    const val MS_REVIEW_END = "---END MS_REVIEW_SCORE---"
}
