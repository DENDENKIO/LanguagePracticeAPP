package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 物質-抽象変換セッション（2コース版）
 * 物質→抽象コース と 抽象→物質コース の両方に対応
 */
@Entity(tableName = "material_abstraction_sessions")
data class MaterialAbstractionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionTitle: String = "",
    val courseType: Int = 0,  // 0: MATERIAL_TO_ABSTRACT, 1: ABSTRACT_TO_MATERIAL

    // ===== 物質→抽象コース用 =====
    val selectedMaterial: String = "",

    // 観察フェーズ（7項目に細分化）
    val observationVisual: String = "",      // 形
    val observationTactile: String = "",     // 色
    val observationAuditory: String = "",    // 光
    val observationOlfactory: String = "",   // 触感
    val observationGustatory: String = "",   // におい
    val observationSound: String = "",       // 音
    val observationContext: String = "",     // 状況

    // 特徴抽出フェーズ
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

    // ===== 抽象→物質コース用 =====
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

    // 物質決定
    val chosenMaterial: String = "",
    val chosenMaterialReason: String = "",

    // 物質の具体化
    val materialState: String = "",
    val materialContext: String = "",
    val materialCondition: String = "",

    // ===== 共通フィールド =====
    val selectedTheme: String = "",
    val isCustomTheme: Boolean = false,
    val customThemeDefinition: String = "",
    val forbiddenWords: String = "",  // カンマ区切り

    // 表現生成
    val generatedExpression: String = "",

    // フィードバック情報
    val feedbackVisualCount: Int = 0,
    val feedbackTactileCount: Int = 0,
    val feedbackAuditoryCount: Int = 0,
    val feedbackOlfactoryCount: Int = 0,
    val feedbackGustatoryCount: Int = 0,
    val feedbackMetaphorCount: Int = 0,
    val feedbackForbiddenWordUsed: Boolean = false,

    // メタデータ
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)
