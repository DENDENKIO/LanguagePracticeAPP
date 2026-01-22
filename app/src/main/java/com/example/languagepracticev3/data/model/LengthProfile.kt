// app/src/main/java/com/example/languagepracticev3/data/models/LengthProfile.kt
package com.example.languagepracticev3.data.models

enum class LengthProfile(val displayName: String, val minChars: Int, val maxChars: Int) {
    MICRO("超短文", 50, 100),
    STUDY_SHORT("短文", 90, 200),
    STUDY_MEDIUM("中文", 250, 450),
    STUDY_LONG("長文", 450, 800),
    ESSAY("エッセイ", 800, 1500)
}
