// app/src/main/java/com/example/languagepracticev3/data/model/AiSiteProfile.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AIサイトのプロファイル
 * WPF版 AiSiteCatalog.cs をKotlinに移植
 */
@Entity(tableName = "ai_site_profile")
data class AiSiteProfile(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val supportsAuto: Boolean = true
) {
    override fun toString(): String = name
}

/**
 * AIサイトカタログ（プリセット）
 */
object AiSiteCatalog {
    val Presets: List<AiSiteProfile> = listOf(
        AiSiteProfile(
            id = "GENSPARK",
            name = "Genspark (AI Chat)",
            url = "https://www.genspark.ai/agents?type=ai_chat",
            supportsAuto = true
        ),
        AiSiteProfile(
            id = "PERPLEXITY",
            name = "Perplexity",
            url = "https://www.perplexity.ai/",
            supportsAuto = true
        ),
        AiSiteProfile(
            id = "GOOGLE_AI",
            name = "Google AI (Landing)",
            url = "https://google.com/ai",
            // ランディングページでUIが一定ではないので自動は基本非推奨
            supportsAuto = false
        )
    )

    fun getByIdOrDefault(id: String?): AiSiteProfile {
        if (id.isNullOrBlank()) return Presets[0]
        return Presets.find { it.id == id } ?: Presets[0]
    }
}
