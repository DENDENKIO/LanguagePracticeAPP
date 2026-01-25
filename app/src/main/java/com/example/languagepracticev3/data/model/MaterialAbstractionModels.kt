// app/src/main/java/com/example/languagepracticev3/data/model/MaterialAbstractionModels.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 物質-抽象変換のコース種別
 */
enum class MaterialAbstractionCourse(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    MATERIAL_TO_ABSTRACT(
        displayName = "物質→抽象コース",
        description = "日常の物質を観察し、そこから感情を引き出して表現する",
        emoji = "🔴→🟣"
    ),
    ABSTRACT_TO_MATERIAL(
        displayName = "抽象→物質コース",
        description = "感情テーマから最適な物質を選び、具体的に描写する",
        emoji = "🟣→🔴"
    )
}

/**
 * 物質→抽象コースのステップ（6ステップ + 結果）
 */
enum class MaterialToAbstractStep(
    val displayName: String,
    val description: String,
    val emoji: String,
    val tips: List<String>
) {
    MATERIAL_SELECTION(
        displayName = "物質選択",
        description = "観察対象となる物質を決定します",
        emoji = "🔴",
        tips = listOf(
            "触れることができる、物理的に存在するものを選ぶ",
            "抽象的な概念（愛、時間、幸せなど）は避ける",
            "あなたの経験や直感で選んだ物質だからこそ、深い思考につながる"
        )
    ),
    OBSERVATION(
        displayName = "観察フェーズ",
        description = "5つの感覚で詳細に観察します（写真のように見えるまで）",
        emoji = "🟠",
        tips = listOf(
            "「意味づけ」をせず、ありのままを描写する",
            "最低3つの感覚を使う（視覚+触覚+嗅覚が最小セット）",
            "実物がなくても想像で記述してOK"
        )
    ),
    FEATURE_EXTRACTION(
        displayName = "特徴抽出",
        description = "観察した内容から本質的な特徴を列挙します（感情語禁止）",
        emoji = "🟡",
        tips = listOf(
            "感情語を使わず、純粋に「物質的事実」として列挙する",
            "「傷がある」「色褪せている」「隅に置かれている」など",
            "後で連想の材料になるので、できるだけ多く書き出す"
        )
    ),
    ASSOCIATION(
        displayName = "連想フェーズ",
        description = "各特徴から3〜5個の連想を引き出し、最強の連想を選びます",
        emoji = "🟢",
        tips = listOf(
            "1つの特徴から複数の連想を出す（傷→過去の痕跡、完璧さの欠如、歴史...）",
            "どれが最も「強く」自分に響くかを感じる",
            "ここから具体→抽象への飛躍が起こる"
        )
    ),
    CONCEPTUALIZATION(
        displayName = "感情・概念化",
        description = "最強の連想をテーマとして確定し、禁止ワードを設定します",
        emoji = "🔵",
        tips = listOf(
            "テーマ名そのもの＋類語が禁止ワードになる",
            "既存テーマを選ぶか、カスタムテーマを作成できる",
            "禁止ワードがあるから「示す」表現が生まれる"
        )
    ),
    EXPRESSION_GENERATION(
        displayName = "表現生成",
        description = "禁止ワードを避けて3〜5行で表現します",
        emoji = "🟣",
        tips = listOf(
            "抽象語を一切使わず、物質の具体的な状態だけで感情を伝える",
            "比喩を1〜2個入れると強くなる",
            "読者が「あ、これは○○を象徴しているんだ」と自分で気づける仕掛けに"
        )
    ),
    RESULT_DISPLAY(
        displayName = "結果表示",
        description = "完成した表現を確認し、振り返ります",
        emoji = "✨",
        tips = listOf(
            "チェックリストで最終確認",
            "同じテーマは他の物質からも到達可能",
            "これが「物質は異なるが、本質的な感情は同じ」という発見"
        )
    )
}

/**
 * 抽象→物質コースのステップ（6ステップ + 結果）
 */
enum class AbstractToMaterialStep(
    val displayName: String,
    val description: String,
    val emoji: String,
    val tips: List<String>
) {
    THEME_SELECTION(
        displayName = "テーマ選択",
        description = "アプリからランダムまたは手動でテーマを選択します",
        emoji = "🟣",
        tips = listOf(
            "ランダムボタンで予想外のテーマに挑戦できる",
            "既存テーマには定義と禁止ワードが用意されている",
            "カスタムテーマも作成可能"
        )
    ),
    THEME_UNDERSTANDING(
        displayName = "テーマ理解",
        description = "抽象テーマの特徴・由来・反対語を掘り下げます",
        emoji = "🔵",
        tips = listOf(
            "テーマを「多面的」に分解する",
            "反対語を考えると、テーマの本質が見えてくる",
            "「期待」の反対は「諦め」「絶望」「無関心」など"
        )
    ),
    MATERIAL_CANDIDATES(
        displayName = "物質選定",
        description = "テーマに最適な物質を3〜5個考えます",
        emoji = "🟢",
        tips = listOf(
            "テーマの複数の側面を捉えられる物質を探す",
            "「期待」なら未開封の封筒、発芽前の種子、割られる直前の卵...",
            "それぞれの物質がテーマをどう象徴するか理由も書く"
        )
    ),
    MATERIAL_DECISION(
        displayName = "物質型決定",
        description = "最も相応しい物質を1つ選択します",
        emoji = "🟡",
        tips = listOf(
            "テーマの複数の側面を最もよく捉えられる物質を選ぶ",
            "無理に合わせようとするより、最適な物質を探す方が早い",
            "選んだ理由を明確にしておく"
        )
    ),
    MATERIAL_SPECIFICATION(
        displayName = "物質の具体化",
        description = "選んだ物質の状態をテーマに合わせて設定します",
        emoji = "🟠",
        tips = listOf(
            "「いつ」「どこで」「誰が」を設定する",
            "テーマを最もよく表す「状態」は何か考える",
            "例：「期待」なら「割られる直前の卵（手に持たれている）」"
        )
    ),
    DESCRIPTION(
        displayName = "描写フェーズ",
        description = "5つの感覚で具体的に3〜5行で描写します",
        emoji = "🔴",
        tips = listOf(
            "最低3つの感覚を使う",
            "時間軸を入れるとより強くなる",
            "感情語を使わず、読者に推測させる"
        )
    ),
    RESULT_DISPLAY(
        displayName = "結果表示",
        description = "完成した描写を確認し、振り返ります",
        emoji = "✨",
        tips = listOf(
            "チェックリストで最終確認",
            "抽象テーマから具体的な物質の描写が完成",
            "逆方向のトレーニングで思考の柔軟性が高まる"
        )
    )
}

/**
 * 5つの感覚タイプ
 */
enum class SenseType(
    val displayName: String,
    val emoji: String,
    val guidingQuestion: String,
    val examples: List<String>,
    val detailedGuide: String
) {
    VISUAL(
        displayName = "視覚",
        emoji = "👁️",
        guidingQuestion = "この物質を見たとき、何が見えますか？",
        examples = listOf("赤と黄色が混在", "ツヤツヤした部分", "黒ずんだ部分が3箇所", "片側がへこんでいる"),
        detailedGuide = "形、色、光の反射、表面の質感、大きさ、位置関係を詳しく記述してください。「赤い」だけでなく「赤と黄色が混在し、黒ずんだ部分が3箇所ある」のように具体的に。"
    ),
    TACTILE(
        displayName = "触覚",
        emoji = "✋",
        guidingQuestion = "触るとどんな感触ですか？温度は？",
        examples = listOf("表面はざらざら", "軸のところは柔らかくぬめっている", "冷たい", "次第に体温で温まる"),
        detailedGuide = "温度、硬さ、質感、重さ、湿り気を想像してください。「冷たい」だけでなく「冷たいが、体温に徐々に温まっていく」のように変化も記述。"
    ),
    AUDITORY(
        displayName = "聴覚",
        emoji = "👂",
        guidingQuestion = "音は聞こえますか？静寂も描写できます",
        examples = listOf("耳に当てると微かなざわめき", "静かに沈黙している", "指で弾くとコツンと鳴る"),
        detailedGuide = "物質が発する音、関連する音、または「静寂」そのものを描写してください。「静かに沈黙している」「中から微かに何かがざわめいている」など。"
    ),
    OLFACTORY(
        displayName = "嗅覚",
        emoji = "👃",
        guidingQuestion = "どんな匂いがしますか？",
        examples = listOf("土と鶏舎の匂いが混ざった", "かすかに発酵した甘い匂い", "無臭だが埃っぽい"),
        detailedGuide = "直接の匂い、連想される匂い、または「無臭であること」を記述してください。「かすかに、土と鶏舎の匂いが混ざった匂いがした」のように。"
    ),
    GUSTATORY(
        displayName = "味覚",
        emoji = "👅",
        guidingQuestion = "味わうとしたらどんな味ですか？（想像でも可）",
        examples = listOf("生卵特有の滑らかさ", "殻の苦さ", "甘酸っぱい", "渋みが残る"),
        detailedGuide = "実際に味わえる場合はその味を、そうでなければ「味わうとしたら」を想像して記述。「噛もうとすれば、生卵特有の滑らかさと、殻の苦さがあるはずだった」など。"
    )
}

/**
 * 4つの特徴抽出の観点
 */
enum class FeatureAspect(
    val displayName: String,
    val guidingQuestion: String,
    val followUpQuestions: List<String>,
    val exampleAnswers: List<String>
) {
    FORM_AND_STATE(
        displayName = "形と状態",
        guidingQuestion = "この物質の形状と状態は？「新しい」か「古い」か。「完璧」か「傷ついている」か。",
        followUpQuestions = listOf(
            "傷はありますか？へこみ、欠け、黒ずみなど",
            "完璧に見えますか？それとも不完全ですか？",
            "新鮮さを保っていますか？失いかけていますか？"
        ),
        exampleAnswers = listOf(
            "少し歪んだ球体、片側がへこんでいる",
            "黒ずんだ部分が3箇所ある",
            "新鮮さを失いかけている"
        )
    ),
    TIME_PASSAGE(
        displayName = "時間経過",
        guidingQuestion = "この物質から「時間経過」を感じますか？",
        followUpQuestions = listOf(
            "どのくらいの時間が経過していますか？",
            "過去にはどんな状態だったと想像しますか？",
            "この先、どう変化していくと思いますか？"
        ),
        exampleAnswers = listOf(
            "軸が弱っている（劣化の兆候）",
            "季節を一つ多く過ごしているようだ",
            "もう二度と新鮮には戻らない"
        )
    ),
    POSITION_AND_PLACEMENT(
        displayName = "位置と配置",
        guidingQuestion = "この物質は「どこに」「どのように」置かれていますか？",
        followUpQuestions = listOf(
            "目立つ場所ですか？隠れた場所ですか？",
            "他のものとの関係は？一緒にある？孤立している？",
            "誰かに見つけられそうですか？見落とされていますか？"
        ),
        exampleAnswers = listOf(
            "果物かごの奥底、他の果物に埋もれている",
            "誰にも選ばれていない状態",
            "一つだけ取り残されている"
        )
    ),
    CUSTOM_FEATURE(
        displayName = "その他の特徴",
        guidingQuestion = "上記以外に気づいた特徴や、直感的に感じたことは？",
        followUpQuestions = listOf(
            "この物質が「語りかけてくる」ことは何ですか？",
            "言葉にしにくい印象でも構いません",
            "「完璧ではない」「歴史を抱えている」など自由に"
        ),
        exampleAnswers = listOf(
            "色がまだら（完璧ではない）",
            "誰にも手を伸ばされていない",
            "それでも軸は折れていない"
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
        val forbiddenWords: List<String>,
        val relatedMaterials: List<String>,
        val exampleExpression: String
    )

    val themes = mapOf(
        "孤独" to ThemeInfo(
            name = "孤独",
            definition = "誰にも望まれていない状態。周囲に人がいても選ばれない感覚。時間の中で価値が減少する感覚。",
            opposites = listOf("繋がり", "愛", "選ばれる", "承認"),
            commonFeatures = listOf("見落とされる", "選ばれない", "待つ", "時間経過", "隔離"),
            forbiddenWords = listOf("孤独", "寂しい", "悲しい", "孤立", "一人", "見落とされた", "ひとりぼっち", "寂寥", "取り残す", "可哀想", "無視", "忘れられた"),
            relatedMaterials = listOf("かごの奥のりんご", "棚の隅のボタン", "引き出しの奥の手紙", "売れ残った商品"),
            exampleExpression = "果物かごの奥底で、他の果物の根元に埋もれたりんご。赤と黄色の皮は、もう誰の目にも止まらない。かごを空けるたび、新しい果物が上に重ねられていった。"
        ),
        "期待" to ThemeInfo(
            name = "期待",
            definition = "何かが起こることを前もって信じる心。未来への希望、可能性への信頼。結末は実現か裏切りの二つ。",
            opposites = listOf("諦め", "絶望", "無関心", "達成"),
            commonFeatures = listOf("未開封", "待つ", "可能性", "未来", "不確実性", "高ぶり"),
            forbiddenWords = listOf("期待", "希望", "楽しみ", "待ち遠しい", "ワクワク", "予感", "望み", "ドキドキ"),
            relatedMaterials = listOf("未開封の封筒", "発芽前の種子", "割られる直前の卵", "届くはずの手紙"),
            exampleExpression = "掌に乗った卵。白い、完璧な、何も傷つけられていない卵。その冷たさが、親指の力で失われるまで、あと一秒。割った後に何が出てくるのか、もう誰にも止められない。"
        ),
        "喪失" to ThemeInfo(
            name = "喪失",
            definition = "かつてあったものが失われた状態。空白の感覚。二度と戻らないという確信。",
            opposites = listOf("獲得", "満たされる", "豊かさ", "存在"),
            commonFeatures = listOf("空っぽ", "痕跡", "記憶", "不在", "欠落"),
            forbiddenWords = listOf("喪失", "失う", "なくなった", "消えた", "空虚", "欠如", "失った", "不在", "悲しい", "辛い", "苦しい", "もう二度と"),
            relatedMaterials = listOf("空になったカップ", "抜け落ちた鍵穴", "色褪せた写真", "片方だけの靴"),
            exampleExpression = "テーブルの上に、二つ並んでいたはずのカップ。今は一つだけが、取っ手をこちらに向けて置かれている。もう一つがあった場所には、かすかな輪の跡だけが残っていた。"
        ),
        "儚さ" to ThemeInfo(
            name = "儚さ",
            definition = "存在が一時的で、すぐに消えてしまうという感覚。一瞬の美しさと無常感。",
            opposites = listOf("永遠", "不変", "堅固", "永続"),
            commonFeatures = listOf("消える", "変化", "瞬間", "脆さ", "一時性"),
            forbiddenWords = listOf("儚い", "消える", "一瞬", "無常", "はかない", "もろい", "脆い", "束の間", "刹那", "移ろう"),
            relatedMaterials = listOf("散りかけの花びら", "溶けかけの氷", "消えかけのろうそく", "朝露"),
            exampleExpression = "窓辺に落ちた桜の花びら。朝の光に透けて、葉脈が細い線のように浮かび上がっている。午後になれば、もうその色は茶色くくすんでいるだろう。"
        ),
        "懐かしさ" to ThemeInfo(
            name = "懐かしさ",
            definition = "過去の時間や場所への郷愁。戻りたいけど戻れない感覚。",
            opposites = listOf("新鮮", "未来", "前進", "忘却"),
            commonFeatures = listOf("古さ", "色褪せ", "匂い", "質感の変化", "記憶"),
            forbiddenWords = listOf("懐かしい", "昔", "思い出", "郷愁", "ノスタルジー", "過去", "あの頃", "戻りたい"),
            relatedMaterials = listOf("古い本", "使い込まれたスプーン", "祖母の手紙", "色褪せた写真"),
            exampleExpression = "引き出しの奥から出てきた木のスプーン。持ち手のところが少しすり減って、指の形に凹んでいる。誰かが毎日、同じように握っていた証拠だった。"
        ),
        "不安" to ThemeInfo(
            name = "不安",
            definition = "何か悪いことが起こりそうだという漠然とした恐れ。先が見えない恐怖。",
            opposites = listOf("安心", "確信", "安定", "平穏"),
            commonFeatures = listOf("不確実性", "暗さ", "揺れ", "不安定", "待つ"),
            forbiddenWords = listOf("不安", "怖い", "恐れ", "心配", "緊張", "怯える", "恐怖", "ドキドキ", "落ち着かない"),
            relatedMaterials = listOf("揺れるろうそくの炎", "軋む床板", "曇ったガラス", "傾いた椅子"),
            exampleExpression = "ろうそくの炎が、誰もいない部屋で揺れている。窓は閉まっているはずなのに。炎の影が壁に伸びたり縮んだりを繰り返すたび、部屋の形が変わっていくようだった。"
        ),
        "希望" to ThemeInfo(
            name = "希望",
            definition = "困難の中でも前を向く力。暗闇の中に光を見出す感覚。",
            opposites = listOf("絶望", "諦め", "暗闇", "停滞"),
            commonFeatures = listOf("光", "芽吹き", "上昇", "温かさ", "小さな兆し"),
            forbiddenWords = listOf("希望", "光", "明るい", "前向き", "ポジティブ", "未来", "きっと", "信じる"),
            relatedMaterials = listOf("芽吹く種", "夜明け前の空", "ひび割れから生える草", "窓から差す光"),
            exampleExpression = "コンクリートのひび割れから、小さな緑が顔を出していた。誰も水をやらないのに、そこだけ色が違う。アスファルトに囲まれた、たった一センチの緑。"
        ),
        "純粋" to ThemeInfo(
            name = "純粋",
            definition = "汚れがなく、混じりけのない状態。無垢さ。一度壊れたら戻らない。",
            opposites = listOf("汚染", "複雑", "世俗", "穢れ"),
            commonFeatures = listOf("白さ", "透明感", "完全さ", "傷のなさ"),
            forbiddenWords = listOf("純粋", "無垢", "清らか", "汚れない", "穢れない", "純白", "きれい", "澄んだ"),
            relatedMaterials = listOf("新雪", "透き通った水", "生まれたての卵", "朝露"),
            exampleExpression = "まだ誰も足を踏み入れていない雪。朝日が当たると、表面がかすかに輝いている。一歩踏み出せば、もうこの白さは戻らない。"
        ),
        "成長" to ThemeInfo(
            name = "成長",
            definition = "変化を通じて強くなること。進化の過程。傷を経験として抱える。",
            opposites = listOf("停滞", "退化", "固定", "衰退"),
            commonFeatures = listOf("変化", "時間", "経験", "傷", "強さ"),
            forbiddenWords = listOf("成長", "進歩", "発展", "向上", "進化", "強くなる", "大人になる"),
            relatedMaterials = listOf("年輪のある木", "使い込まれた道具", "傷のある手", "修繕された器"),
            exampleExpression = "まな板の表面には、無数の刃の跡が刻まれていた。深いもの、浅いもの、斜めに走るもの。どの傷も、誰かがここで何かを作った証だった。"
        ),
        "自由" to ThemeInfo(
            name = "自由",
            definition = "束縛からの解放。制約のない状態。",
            opposites = listOf("束縛", "制限", "閉塞", "支配"),
            commonFeatures = listOf("広がり", "風", "空", "動き", "開放"),
            forbiddenWords = listOf("自由", "解放", "束縛", "制限", "開放", "自在", "のびのび"),
            relatedMaterials = listOf("開いた窓", "切れた紐", "広い空", "舞い上がる葉"),
            exampleExpression = "窓が開いて、カーテンが部屋の中に膨らんだ。風が入ってくるたびに、布が大きく揺れて、また戻る。その動きを止めるものは、もう何もなかった。"
        )
    )

    fun getTheme(name: String): ThemeInfo? {
        return themes[name] ?: themes.values.find {
            it.name.contains(name) || name.contains(it.name)
        }
    }

    fun getRandomTheme(): ThemeInfo = themes.values.random()

    fun getAllThemeNames(): List<String> = themes.keys.toList()

    fun generateForbiddenWords(customTheme: String): List<String> {
        return listOf(
            customTheme,
            "${customTheme}な",
            "${customTheme}い",
            "${customTheme}さ",
            "${customTheme}感",
            "${customTheme}的"
        )
    }
}

/**
 * 感覚語カウンター用のキーワードリスト
 */
object SensoryKeywords {
    val visual = listOf(
        "見", "色", "光", "影", "形", "明", "暗", "白", "黒", "赤", "青", "緑", "黄",
        "輝", "眩", "透", "濁", "艶", "くすん", "鮮やか", "薄", "濃", "丸", "四角", "線",
        "映る", "ぼやける", "くっきり", "ツヤ", "マット"
    )

    val tactile = listOf(
        "触", "冷", "温", "熱", "硬", "柔", "滑", "ざらざら", "ごつごつ", "重", "軽",
        "厚", "薄", "湿", "乾", "粘", "しっとり", "さらさら", "ぬめる", "すべすべ"
    )

    val auditory = listOf(
        "音", "声", "響", "静", "騒", "鳴", "聞", "カサカサ", "コツコツ", "パチパチ",
        "沈黙", "無音", "かすか", "大きな", "ザワザワ", "シーン", "ゴロゴロ"
    )

    val olfactory = listOf(
        "匂", "香", "臭", "薫", "嗅", "芳", "甘い香り", "酸っぱい匂い", "土の匂い",
        "花の香り", "煙の匂い", "鼻をつく"
    )

    val gustatory = listOf(
        "味", "甘", "酸", "苦", "辛", "塩", "旨", "不味", "口", "舌",
        "噛む", "飲み込む", "後味"
    )

    val metaphor = listOf(
        "ような", "みたい", "ごとく", "似た", "例える", "まるで", "あたかも", "さながら",
        "〜を思わせる"
    )

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

    fun getTotalSensoryCount(text: String): Int {
        return countSensoryWords(text).values.sum()
    }
}

/**
 * 表現チェックリスト
 */
object ExpressionChecklist {
    data class ChecklistItem(
        val id: String,
        val description: String,
        var isChecked: Boolean = false
    )

    val items = listOf(
        ChecklistItem("photo_like", "物質の具体描写は「写真として見える」レベルか"),
        ChecklistItem("three_senses", "5つの感覚のうち、最低3つは入っているか"),
        ChecklistItem("no_abstract", "抽象語（孤独/悲しい/希望など）を一語も使っていないか"),
        ChecklistItem("time_axis", "物質に「時間軸」が含まれているか（昔〜今〜未来）"),
        ChecklistItem("context", "「いつ」「どこで」「誰が」のうち、最低一つはあるか"),
        ChecklistItem("metaphor_count", "メタファーは1〜2個程度で、陳腐でないか"),
        ChecklistItem("reader_discovery", "読者が「あ、これは○○を象徴しているんだ」と気づける仕掛けになっているか"),
        ChecklistItem("length", "3〜5行（または150〜300字）に収まっているか")
    )
}

/**
 * 物質-抽象変換セッション（Room Entity）
 */
@Entity(tableName = "material_abstraction_sessions")
data class MaterialAbstractionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // セッション基本情報
    val sessionTitle: String = "",
    val courseType: Int = 0, // 0: MATERIAL_TO_ABSTRACT, 1: ABSTRACT_TO_MATERIAL
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",

    // ===== 物質→抽象コース用フィールド =====
    val selectedMaterial: String = "",
    // 観察フェーズ
    val observationVisual: String = "",
    val observationTactile: String = "",
    val observationAuditory: String = "",
    val observationOlfactory: String = "",
    val observationGustatory: String = "",
    // 特徴抽出
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

    // ===== 共通フィールド =====
    val selectedTheme: String = "",
    val isCustomTheme: Boolean = false,
    val customThemeDefinition: String = "",
    val forbiddenWords: String = "", // カンマ区切り

    // ===== 抽象→物質コース用フィールド =====
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
    val materialCandidateReasons: String = "", // JSON形式
    // 物質型決定
    val chosenMaterial: String = "",
    val chosenMaterialReason: String = "",
    // 物質の具体化
    val materialState: String = "",
    val materialContext: String = "", // いつ、どこで、誰が
    val materialCondition: String = "", // 損傷度、新しさなど

    // ===== 最終表現 =====
    val generatedExpression: String = "",

    // フィードバック
    val feedbackVisualCount: Int = 0,
    val feedbackTactileCount: Int = 0,
    val feedbackAuditoryCount: Int = 0,
    val feedbackOlfactoryCount: Int = 0,
    val feedbackGustatoryCount: Int = 0,
    val feedbackMetaphorCount: Int = 0,
    val feedbackForbiddenWordUsed: Boolean = false
)
