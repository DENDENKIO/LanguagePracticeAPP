// app/src/main/java/com/example/languagepracticev3/data/model/LengthProfile.kt
package com.example.languagepracticev3.data.model

enum class LengthProfile(val displayName: String, val wordRange: String) {
    STUDY_SHORT("練習用（短め）", "200-400字"),
    STANDARD("標準", "400-800字"),
    LONG("長め", "800-1200字"),
    ESSAY("エッセイ", "1200-2000字");

    companion object {
        fun fromName(name: String): LengthProfile {
            return entries.find { it.name == name } ?: STUDY_SHORT
        }
    }
}
