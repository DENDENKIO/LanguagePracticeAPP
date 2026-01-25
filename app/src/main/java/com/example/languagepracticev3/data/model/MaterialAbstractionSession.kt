// app/src/main/java/com/example/languagepracticev3/data/model/MaterialAbstractionSession.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 物質-抽象変換セッション
 * 仕様書に基づく7フェーズのトレーニングを保存
 */
@Entity(tableName = "material_abstraction_sessions")
data class MaterialAbstractionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionTitle: String = "",

    // Phase 1: 観察（5感覚、意味づけ禁止）
    val targetMaterial: String = "",           // 対象物質（りんご、封筒、卵など）
    val observationRaw: String = "",           // 具体描写（生データ）

    // Phase 2: 特徴抽出（事実だけ）
    val featureList: String = "",              // 特徴リスト（改行区切り）

    // Phase 3.5: 特徴翻訳（ベクトル化）
    val selectedAxes: String = "",             // 選択した軸（カンマ区切り、例: "1,3,4,19"）
    val selectedTags: String = "",             // 選択したタグ（カンマ区切り、例: "CORE-002,CORE-005"）
    val tagSentences: String = "",             // 生成したタグ文（改行区切り）

    // Phase 3.6: 収束（圧縮）
    val strongTagSentences: String = "",       // 強いタグ文（上位2〜4本、改行区切り）

    // Phase 4: 連想（タグ文→連想）
    val associations: String = "",             // 連想リスト（改行区切り）

    // Phase 5: テーマ決定
    val abstractTheme: String = "",            // 決定した抽象テーマ（期待、孤独、信頼など）

    // Phase 6: 抽象語禁止で表現
    val finalExpression: String = "",          // 最終表現（3〜5行、抽象語なし）
    val forbiddenWords: String = "",           // 禁止ワードリスト（カンマ区切り）

    // スコアリング情報
    val abstractScore: Int = 0,                // 抽象変換スコア（0〜5）
    val sensoryScore: Int = 0,                 // 描写スコア（0〜5）

    // メタデータ
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * 物質-抽象変換のステップ
 */
enum class MaterialAbstractionStep(val displayName: String, val description: String) {
    OBSERVATION("観察", "5感覚で対象を観察（意味づけ禁止）"),
    FEATURE_EXTRACTION("特徴抽出", "事実だけを箇条書きで列挙"),
    AXIS_TAG_SELECTION("軸・タグ選択", "20軸から軸を選び、タグを選択してタグ文を生成"),
    CONVERGENCE("収束", "タグ文を上位2〜4本に絞り込む"),
    ASSOCIATION("連想", "タグ文から連想を3〜5個ずつ出す"),
    THEME_DECISION("テーマ決定", "最も強い連想から抽象テーマを1つ決める"),
    FINAL_EXPRESSION("抽象語禁止で表現", "テーマを3〜5行で表現（抽象語を使わない）")
}
