// app/src/main/java/com/example/languagepracticev3/data/model/MaterialAbstractionModels.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 物質-抽象変換プロセスの7つのステップ
 */
enum class MaterialAbstractionStep(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    MATERIAL_SELECTION(
        "物質選択",
        "観察の対象となる物質を選択します",
        "🔴"
    ),
    OBSERVATION(
        "観察フェーズ",
        "5つの感覚で詳細に観察します",
        "🟠"
    ),
    FEATURE_EXTRACTION(
        "特徴抽出",
        "本質的な特徴を4つの観点から抽出します",
        "🟡"
    ),
    ASSOCIATION(
        "連想フェーズ",
        "特徴から感情・概念を連想します",
        "🟢"
    ),
    CONCEPTUALIZATION(
        "概念化フェーズ",
        "テーマを確定します",
        "🔵"
    ),
    EXPRESSION_GENERATION(
        "表現生成",
        "禁止ワードを避けて表現します",
        "🟣"
    ),
    RESULT_DISPLAY(
        "結果表示",
        "プロセス全体を振り返ります",
        "🟣"
    )
}

/**
 * 5つの感覚タイプ
 */
enum class SenseType(
    val displayName: String,
    val emoji: String,
    val guidingQuestion: String,
    val examples: List<String>
) {
    VISUAL(
        "視覚",
        "👁️",
        "この物質を見たとき、何が見えますか？形、色、光の反射、表面の質感を詳しく記述してください。",
        listOf("赤い", "丸い", "艶がある", "傷がある", "色褪せた", "透明な")
    ),
    TACTILE(
        "触覚",
        "✋",
        "この物質に触れたとき、どんな感触がありますか？温度、硬さ、質感、重さを想像してください。",
        listOf("冷たい", "滑らか", "ざらざら", "重い", "柔らかい", "硬い")
    ),
    AUDITORY(
        "聴覚",
        "🔊",
        "この物質が発する音、または関連する音を想像してください。",
        listOf("静か", "カサカサ", "コツコツ", "沈黙", "かすかな音")
    ),
    OLFACTORY(
        "嗅覚",
        "👃",
        "この物質のにおいを想像してください。どんな香りがしますか？",
        listOf("甘い", "酸っぱい", "土の匂い", "懐かしい香り", "無臭")
    ),
    GUSTATORY(
        "味覚",
        "👅",
        "この物質の味を想像してください（食べられる場合）。または、味を想像するとしたら？",
        listOf("甘い", "酸っぱい", "苦い", "塩辛い", "無味")
    )
}

/**
 * 4つの特徴抽出の観点
 */
enum class FeatureAspect(
    val displayName: String,
    val guidingQuestion: String,
    val followUpQuestions: List<String>
) {
    FORM_AND_STATE(
        "形と状態",
        "この物質を見たとき、まず何が目に入りますか？その形や状態は『新しい』か『古い』か。『完璧』か『傷ついている』か。",
        listOf(
            "完璧に見えますか？それとも欠けている部分がありますか？",
            "どのくらいの時間が経過しているように見えますか？",
            "その状態は良いと言えますか？悪いと言えますか？"
        )
    ),
    TIME_PASSAGE(
        "時間経過",
        "この物質から『時間経過』を感じますか？新鮮さを失っているのか、それとも『ずっと変わらない』ものなのか。",
        listOf(
            "この先、どう変化していくと思いますか？",
            "過去にはどんな状態だったと想像しますか？",
            "時間はこの物質に何を刻んでいますか？"
        )
    ),
    POSITION_AND_PLACEMENT(
        "位置と配置",
        "この物質は『どこに置かれている』のか想像できますか？他のものと一緒にあるのか、『孤立』しているのか。",
        listOf(
            "光は当たっていますか？それとも暗い場所ですか？",
            "誰かに注目されていますか？見落とされていますか？",
            "周りの環境との関係はどうですか？"
        )
    ),
    CUSTOM_FEATURE(
        "カスタム特徴",
        "上の3つの質問以外に、この物質について『目立つ特徴』や『感じたこと』がありますか？",
        listOf(
            "直感的に感じたことを言葉にしてみてください",
            "抽象的な言い方でも構いません",
            "『完璧ではない』『歴史を抱えている』など、自由に表現してください"
        )
    )
}

/**
 * 感情テーマデータベース
 */
object EmotionThemeDatabase {
    data class ThemeInfo(
        val name: String,
        val definition: String,
        val opposites: List<String>,
        val commonFeatures: List<String>,
        val forbiddenWords: List<String>
    )

    val themes = mapOf(
        "孤独" to ThemeInfo(
            name = "孤独",
            definition = "誰にも望まれていない状態、見落とされる感覚",
            opposites = listOf("繋がり", "愛", "選別", "承認"),
            commonFeatures = listOf("見えない", "選ばれない", "待つ", "時間経過"),
            forbiddenWords = listOf("孤独", "寂しい", "悲しい", "孤立", "一人", "見落とされた", "ひとりぼっち", "寂寥")
        ),
        "喪失" to ThemeInfo(
            name = "喪失",
            definition = "かつてあったものが失われた状態、空白の感覚",
            opposites = listOf("獲得", "満たされる", "豊かさ", "存在"),
            commonFeatures = listOf("空っぽ", "痕跡", "記憶", "不在"),
            forbiddenWords = listOf("喪失", "失う", "なくなった", "消えた", "空虚", "欠如", "失った", "不在")
        ),
        "期待" to ThemeInfo(
            name = "期待",
            definition = "まだ起きていないことへの希望や予感",
            opposites = listOf("諦め", "絶望", "無関心", "達成"),
            commonFeatures = listOf("未開封", "待つ", "可能性", "未来"),
            forbiddenWords = listOf("期待", "希望", "楽しみ", "待ち遠しい", "ワクワク", "予感", "望み")
        ),
        "儚さ" to ThemeInfo(
            name = "儚さ",
            definition = "一瞬で消えてしまう美しさや無常感",
            opposites = listOf("永遠", "不変", "堅固", "永続"),
            commonFeatures = listOf("消える", "変化", "瞬間", "脆さ"),
            forbiddenWords = listOf("儚い", "消える", "一瞬", "無常", "はかない", "もろい", "脆い")
        ),
        "懐かしさ" to ThemeInfo(
            name = "懐かしさ",
            definition = "過去への郷愁、戻れない時間への思い",
            opposites = listOf("新鮮", "未来", "前進", "忘却"),
            commonFeatures = listOf("古い", "色褪せ", "記憶", "時間"),
            forbiddenWords = listOf("懐かしい", "昔", "思い出", "郷愁", "ノスタルジー", "過去")
        ),
        "不安" to ThemeInfo(
            name = "不安",
            definition = "先が見えない恐れ、不確実性への恐怖",
            opposites = listOf("安心", "確信", "平穏", "安定"),
            commonFeatures = listOf("揺れる", "暗い", "不確か", "待つ"),
            forbiddenWords = listOf("不安", "怖い", "恐れ", "心配", "緊張", "怯える", "恐怖")
        ),
        "希望" to ThemeInfo(
            name = "希望",
            definition = "困難の中でも前を向く力、光を見出す感覚",
            opposites = listOf("絶望", "諦め", "暗闇", "停滞"),
            commonFeatures = listOf("光", "芽吹き", "上昇", "温かさ"),
            forbiddenWords = listOf("希望", "光", "明るい", "前向き", "ポジティブ", "未来")
        ),
        "成長" to ThemeInfo(
            name = "成長",
            definition = "変化を通じて強くなること、進化の過程",
            opposites = listOf("停滞", "退化", "固定", "衰退"),
            commonFeatures = listOf("変化", "時間", "経験", "傷"),
            forbiddenWords = listOf("成長", "進歩", "発展", "向上", "進化", "強くなる")
        ),
        "自由" to ThemeInfo(
            name = "自由",
            definition = "束縛からの解放、制約のない状態",
            opposites = listOf("束縛", "制限", "閉塞", "支配"),
            commonFeatures = listOf("広がり", "風", "空", "動き"),
            forbiddenWords = listOf("自由", "解放", "束縛", "制限", "開放", "自在")
        ),
        "愛" to ThemeInfo(
            name = "愛",
            definition = "深い繋がりと慈しみの感情",
            opposites = listOf("憎しみ", "無関心", "孤立", "拒絶"),
            commonFeatures = listOf("温かさ", "触れる", "守る", "繋がり"),
            forbiddenWords = listOf("愛", "愛情", "恋", "慈しみ", "優しさ", "思いやり")
        )
    )

    /**
     * テーマ名から情報を取得
     */
    fun getTheme(name: String): ThemeInfo? {
        return themes[name] ?: themes.values.find {
            it.name.contains(name) || name.contains(it.name)
        }
    }

    /**
     * カスタムテーマの禁止ワードを生成
     */
    fun generateForbiddenWords(themeName: String): List<String> {
        return listOf(
            themeName,
            "${themeName}な",
            "${themeName}い",
            "${themeName}さ",
            "${themeName}感"
        )
    }
}

/**
 * 物質-抽象変換セッション
 */
@Entity(tableName = "material_abstraction_sessions")
data class MaterialAbstractionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // セッション基本情報
    val sessionTitle: String = "",
    val currentStep: Int = 0,

    // ステップ1: 物質選択
    val selectedMaterial: String = "",

    // ステップ2: 観察フェーズ（5感）
    val observationVisual: String = "",
    val observationTactile: String = "",
    val observationAuditory: String = "",
    val observationOlfactory: String = "",
    val observationGustatory: String = "",

    // ステップ3: 特徴抽出（4つの観点）
    val featureFormState: String = "",
    val featureTimePassage: String = "",
    val featurePositionPlacement: String = "",
    val featureCustom: String = "",

    // ステップ4: 連想フェーズ
    val associationFromFormState: String = "",
    val associationFromTimePassage: String = "",
    val associationFromPositionPlacement: String = "",
    val associationFromCustom: String = "",
    val strongestAssociation: String = "",

    // ステップ5: 概念化フェーズ
    val selectedTheme: String = "",
    val isCustomTheme: Boolean = false,
    val customThemeDefinition: String = "",
    val forbiddenWords: String = "",  // カンマ区切りで保存

    // ステップ6: 表現生成
    val generatedExpression: String = "",

    // ステップ7: 結果 - フィードバック情報
    val feedbackVisualCount: Int = 0,
    val feedbackTactileCount: Int = 0,
    val feedbackAuditoryCount: Int = 0,
    val feedbackOlfactoryCount: Int = 0,
    val feedbackGustatoryCount: Int = 0,
    val feedbackMetaphorCount: Int = 0,
    val feedbackForbiddenWordUsed: Boolean = false,

    // メタデータ
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * 感覚語カウンター用のキーワードリスト
 */
object SensoryKeywords {
    val visual = listOf(
        "見", "色", "光", "影", "形", "明", "暗", "白", "黒", "赤", "青", "緑", "黄",
        "輝", "眩", "透", "濁", "艶", "くすん", "鮮やか", "薄", "濃", "丸", "四角", "線"
    )

    val tactile = listOf(
        "触", "冷", "温", "熱", "硬", "柔", "滑", "ざらざら", "ごつごつ", "重", "軽",
        "厚", "薄", "湿", "乾", "粘", "しっとり", "さらさら"
    )

    val auditory = listOf(
        "音", "声", "響", "静", "騒", "鳴", "聞", "カサカサ", "コツコツ", "パチパチ",
        "沈黙", "無音", "かすか", "大きな"
    )

    val olfactory = listOf(
        "匂", "香", "臭", "薫", "嗅", "芳", "甘い香り", "酸っぱい匂い", "土の匂い"
    )

    val gustatory = listOf(
        "味", "甘", "酸", "苦", "辛", "塩", "旨", "不味", "口", "舌"
    )

    val metaphor = listOf(
        "ような", "みたい", "ごとく", "似た", "例える", "まるで", "あたかも", "さながら"
    )

    /**
     * テキスト内の感覚語をカウント
     */
    fun countSensoryWords(text: String): Map<String, Int> {
        return mapOf(
            "visual" to visual.count { text.contains(it) },
            "tactile" to tactile.count { text.contains(it) },
            "auditory" to auditory.count { text.contains(it) },
            "olfactory" to olfactory.count { text.contains(it) },
            "gustatory" to gustatory.count { text.contains(it) },
            "metaphor" to metaphor.count { text.contains(it) }
        )
    }
}
