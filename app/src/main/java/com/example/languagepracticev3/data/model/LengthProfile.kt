package com.example.languagepracticev3.data.model

enum class LengthProfile(val displayName: String, val minChars: Int, val maxChars: Int) {
    // 詳細版の定義
    MICRO("超短文", 50, 100),
    STUDY_SHORT("短文", 90, 200),
    STUDY_MEDIUM("中文", 250, 450),
    STUDY_LONG("長文", 450, 800),
    ESSAY("エッセイ", 800, 1500),

    // RouteModels用の追加（互換性のため）
    SHORT("短め", 100, 200),
    MEDIUM("普通", 200, 400),
    LONG("長め", 400, 800),
    VERY_LONG("とても長い", 800, 1500)
}
