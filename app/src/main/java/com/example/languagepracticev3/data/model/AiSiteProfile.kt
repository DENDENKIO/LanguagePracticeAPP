// app/src/main/java/com/example/languagepracticev3/data/model/AiSiteProfile.kt
package com.example.languagepracticev3.data.model

data class AiSiteProfile(
    val id: String,
    val name: String,
    val url: String,
    val supportsAuto: Boolean = true,
    // JavaScript: テキストエリアにプロンプトを入力するセレクタ
    val inputSelector: String = "textarea",
    // JavaScript: 送信ボタンのセレクタ
    val submitSelector: String = "button[type='submit']",
    // JavaScript: 出力を取得するセレクタ
    val outputSelector: String = ".response, .message, .output"
)

object AiSiteCatalog {
    val presets = listOf(
        AiSiteProfile(
            id = "GENSPARK",
            name = "Genspark (AI Chat)",
            url = "https://www.genspark.ai/agents?type=ai_chat",
            supportsAuto = true,
            inputSelector = "textarea[placeholder]",
            submitSelector = "button[type='submit'], button.send-button",
            outputSelector = ".message-content, .response-text"
        ),
        AiSiteProfile(
            id = "PERPLEXITY",
            name = "Perplexity",
            url = "https://www.perplexity.ai/",
            supportsAuto = true,
            inputSelector = "textarea",
            submitSelector = "button[aria-label='Submit']",
            outputSelector = ".prose, .markdown"
        ),
        AiSiteProfile(
            id = "GOOGLE_AI",
            name = "Google AI (Landing)",
            url = "https://google.com/ai",
            // ランディングページでUIが一定ではないので自動は基本非推奨
            supportsAuto = false,
            inputSelector = "textarea",
            submitSelector = "button",
            outputSelector = ".response"
        )
    )

    fun getByIdOrDefault(id: String?): AiSiteProfile {
        if (id.isNullOrBlank()) return presets.first()
        return presets.find { it.id == id } ?: presets.first()
    }
}
