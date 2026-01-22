// app/src/main/java/com/example/languagepracticev3/data/model/LengthProfile.kt
package com.example.languagepracticev3.data.model

/**
 * 長さプロファイル
 * WPF版 Helpers/Constants.cs の LengthProfile をKotlinに移植
 *
 * displayName: UI表示用の名前
 * minChars: 最小文字数
 * maxChars: 最大文字数
 * wordRange: 表示用の文字数範囲（UI表示用）
 */
enum class LengthProfile(
    val displayName: String,
    val minChars: Int,
    val maxChars: Int,
    val wordRange: String
) {
    STUDY_SHORT("練習用（短め）", 90, 200, "90-200字"),
    PRACTICE_MIDDLE("練習用（中）", 250, 450, "250-450字"),
    REVISION_LONG("推敲用（長め）", 450, 800, "450-800字");

    companion object {
        fun fromName(name: String): LengthProfile {
            return entries.find { it.name == name } ?: STUDY_SHORT
        }
    }
}
