// app/src/main/java/com/example/languagepracticev3/data/models/OperationKind.kt
package com.example.languagepracticev3.data.models

enum class OperationKind(val displayName: String, val description: String) {
    READER_AUTO_GEN("読者像生成", "読者像（READER）を自動で1つ提案します（文章生成の前準備用）"),
    TOPIC_GEN("お題生成", "詳細なお題を複数生成します"),
    PERSONA_GEN("ペルソナ生成", "実在人物ベースの書き手像を生成します"),
    OBSERVE_IMAGE("画像観察", "画像から観察ノート（五感・比喩・核候補）を作成します"),
    TEXT_GEN("本文生成", "指定したお題・読者像・書き手から本文を生成し、作品として保存します"),
    STUDY_CARD("学習カード", "本文を分解して学習カードを作成します"),
    CORE_EXTRACT("核抽出", "本文の核（テーマ/感情/持ち帰り/核の一文）を抽出します"),
    REVISION_FULL("全文推敲", "核を不変条件にして全文推敲（複数案）を作ります"),
    GIKO("擬古文変換", "現代文を指定文調（平安風、漢文調など）に書き換えます"),
    PERSONA_VERIFY_ASSIST("ペルソナ検証", "人物プロフィールの根拠テキストをもとに矛盾/支持を整理します")
}
