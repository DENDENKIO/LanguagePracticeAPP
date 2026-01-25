// app/src/main/java/com/example/languagepracticev3/data/model/MaterialAbstractionSession.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 物質-抽象変換セッション（Room Entity）
 * MaterialAbstractionModels.kt の仕様に準拠
 */
@Entity(tableName = "material_abstraction_sessions")
data class MaterialAbstractionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // セッション基本情報
    val sessionTitle: String = "",
    val courseType: Int = 0, // 0: MATERIAL_TO_ABSTRACT, 1: ABSTRACT_TO_MATERIAL
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",

    // ===== 物質→抽象コース用フィールド =====
    val selectedMaterial: String = "",
    // 観察フェーズ
    val observationVisual: String = "",
    val observationTactile: String = "",
    val observationAuditory: String = "",
    val observationOlfactory: String = "",
    val observationGustatory: String = "",
    // 特徴抽出
    val featureFormState: String = "",
    val featureTimePassage: String = "",
    val featurePositionPlacement: String = "",
    val featureCustom: String = "",
    // 連想フェーズ
    val associationFromFormState: String = "",
    val associationFromTimePassage: String = "",
    val associationFromPositionPlacement: String = "",
    val associationFromCustom: String = "",
    val strongestAssociation: String = "",

    // ===== 共通フィールド =====
    val selectedTheme: String = "",
    val isCustomTheme: Boolean = false,
    val customThemeDefinition: String = "",
    val forbiddenWords: String = "", // カンマ区切り

    // ===== 抽象→物質コース用フィールド =====
    // テーマ理解
    val themeDefinition: String = "",
    val themeOrigin: String = "",
    val themeOpposites: String = "",
    val themeCharacteristics: String = "",
    // 物質候補
    val materialCandidate1: String = "",
    val materialCandidate2: String = "",
    val materialCandidate3: String = "",
    val materialCandidate4: String = "",
    val materialCandidate5: String = "",
    val materialCandidateReasons: String = "", // JSON形式
    // 物質型決定
    val chosenMaterial: String = "",
    val chosenMaterialReason: String = "",
    // 物質の具体化
    val materialState: String = "",
    val materialContext: String = "", // いつ、どこで、誰が
    val materialCondition: String = "", // 損傷度、新しさなど

    // ===== 最終表現 =====
    val generatedExpression: String = "",

    // フィードバック
    val feedbackVisualCount: Int = 0,
    val feedbackTactileCount: Int = 0,
    val feedbackAuditoryCount: Int = 0,
    val feedbackOlfactoryCount: Int = 0,
    val feedbackGustatoryCount: Int = 0,
    val feedbackMetaphorCount: Int = 0,
    val feedbackForbiddenWordUsed: Boolean = false
)
