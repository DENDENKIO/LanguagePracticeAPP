// app/src/main/java/com/example/languagepracticev3/data/model/MaterialAbstractionDictionary.kt
package com.example.languagepracticev3.data.model

/**
 * 物質-抽象変換辞書
 * 仕様書のJSON.txtに基づく20軸、8ファセット、200タグ、テンプレートフレームを定義
 */
object MaterialAbstractionDictionary {

    // ====================
    // 20軸（ベクトル）
    // ====================
    data class Axis(
        val id: Int,
        val key: String,
        val label: String,
        val definition: String,
        val examples: List<String>
    )

    val axes: List<Axis> = listOf(
        Axis(1, "time", "時間", "経過・直前直後・周期・期限", listOf("新しい/古い", "あと数分", "毎週入れ替わる")),
        Axis(2, "rhythm", "速度・リズム", "反復・間・テンポ・途切れ", listOf("点滅", "一定間隔で滴る", "沈黙が続く")),
        Axis(3, "boundary", "境界・内部", "外/内・覆い・封・露出・開示", listOf("殻", "未開封", "蓋", "包み")),
        Axis(4, "reversibility", "可逆性", "戻る/戻らない・修復可能性", listOf("割れたら戻らない", "焦げ跡", "修復痕")),
        Axis(5, "placement", "位置・配置", "奥/端/中心・重なり・埋没", listOf("棚の奥", "端に寄る", "下敷き")),
        Axis(6, "reach", "距離・到達", "手が届く/届かない・障害・隔たり", listOf("ガラス越し", "遠い", "手前にある")),
        Axis(7, "quantity", "大きさ・量", "満杯/空・欠損量・密度", listOf("半分", "溢れる", "空洞")),
        Axis(8, "shape", "形状・輪郭", "角/丸み・歪み・対称/非対称", listOf("いびつ", "均整", "へこみ")),
        Axis(9, "material", "材質・組成", "素材・混合・層・変質の仕方", listOf("錆", "繊維", "層が剥がれる")),
        Axis(10, "surface", "表面・粗さ", "つる/ざら・膜・粉・ぬめり", listOf("粉を吹く", "ぬめる", "果点")),
        Axis(11, "hardness", "硬さ・弾性", "硬い/柔らかい・戻る/戻らない", listOf("押すと戻る", "へこむ", "潰れる")),
        Axis(12, "temperature", "温度", "冷/温・体温化・熱源", listOf("掌で温まる", "冷蔵庫の冷え")),
        Axis(13, "moisture", "湿度・含水", "乾/湿・蒸発・染み・水分の移動", listOf("湿る", "乾いて軽い", "染みる")),
        Axis(14, "smell", "匂い", "残り香・混在・変質", listOf("発酵", "青臭さ", "甘い匂い")),
        Axis(15, "sound", "音", "反響・破裂音・無音・きしみ", listOf("乾いた音", "きしむ", "吸われる静けさ")),
        Axis(16, "visibility", "光・視認性", "反射・影・透明度・見えにくさ", listOf("逆光", "曇り", "透け")),
        Axis(17, "continuity", "連続性・一体性", "切れ目・裂け・継ぎ目・接合", listOf("縫い目", "ひび", "切れ")),
        Axis(18, "trace", "汚れ・痕跡", "履歴・指紋・擦れ・染み・黒ずみ", listOf("擦り減り", "染み", "黒ずみ")),
        Axis(19, "control", "操作・制御", "手・道具・圧力・固定・介入", listOf("刃が近い", "握られる", "固定される")),
        Axis(20, "context", "社会・規範（場・役割）", "制度・用途・陳列・検品・儀式・禁止", listOf("店頭", "検品", "供え物", "禁止"))
    )

    // ====================
    // 8ファセット（大分類）
    // ====================
    data class Facet(
        val key: String,
        val label: String,
        val description: String,
        val sensoryScore: Float,
        val abstractScore: Float
    )

    val facets: List<Facet> = listOf(
        Facet("CORE", "基幹", "どの対象にも当てはまる変換骨格", 0.7f, 0.7f),
        Facet("TIME", "時間・進行", "進行、期限、反復、蓄積、劣化/熟成など", 0.5f, 0.8f),
        Facet("UNC", "不確かさ・情報", "不明点、分岐、盲点、偽装、開示など", 0.4f, 0.9f),
        Facet("STR", "境界・構造", "殻/蓋、断裂、亀裂、欠損、規格内外など", 0.5f, 0.8f),
        Facet("CTRL", "力学・制御", "介入、固定、圧力、抑制、破壊/非破壊など", 0.6f, 0.8f),
        Facet("SPACE", "位置・関係", "端/中心、奥/手前、重なり、埋没、行列など", 0.6f, 0.6f),
        Facet("VALUE", "価値・選別・規範", "基準、比較、採用/却下、値引き、用途限定など", 0.3f, 0.9f),
        Facet("SENSE", "感覚・見え方", "反射、逆光、粗さ、粘性、温度差、残り香など", 0.9f, 0.2f)
    )

    // ====================
    // タグ（200個）
    // ====================
    data class Tag(
        val id: String,
        val label: String,
        val facet: String,
        val aliases: List<String> = emptyList()
    )

    val tags: List<Tag> = listOf(
        // CORE (16)
        Tag("CORE-001", "未実行", "CORE", listOf("まだ起きていない")),
        Tag("CORE-002", "直前", "CORE", listOf("寸前")),
        Tag("CORE-003", "直後", "CORE", listOf("起きたばかり")),
        Tag("CORE-004", "途中", "CORE", listOf("進行中", "移行中")),
        Tag("CORE-005", "不可逆", "CORE", listOf("戻らない")),
        Tag("CORE-006", "可逆", "CORE", listOf("戻せる")),
        Tag("CORE-007", "未確定", "CORE", listOf("不明", "確定前")),
        Tag("CORE-008", "開示", "CORE", listOf("明らかになる")),
        Tag("CORE-009", "隠蔽", "CORE", listOf("覆われている")),
        Tag("CORE-010", "制御下", "CORE", listOf("操作される")),
        Tag("CORE-011", "自律", "CORE", listOf("操作されない")),
        Tag("CORE-012", "脆弱", "CORE", listOf("壊れやすい")),
        Tag("CORE-013", "摩耗", "CORE", listOf("消耗の跡")),
        Tag("CORE-014", "評価待ち", "CORE", listOf("判定前", "選別前")),
        Tag("CORE-015", "隔離", "CORE", listOf("切り離し")),
        Tag("CORE-016", "接近", "CORE", listOf("距離が縮む")),

        // TIME (30)
        Tag("TIME-001", "期限", "TIME", listOf("タイムリミット")),
        Tag("TIME-002", "猶予", "TIME"),
        Tag("TIME-003", "延期", "TIME", listOf("先延ばし")),
        Tag("TIME-004", "遅延", "TIME", listOf("遅れる")),
        Tag("TIME-005", "加速", "TIME"),
        Tag("TIME-006", "減速", "TIME"),
        Tag("TIME-007", "停滞", "TIME", listOf("止まる")),
        Tag("TIME-008", "反復", "TIME", listOf("繰り返し")),
        Tag("TIME-009", "周期", "TIME"),
        Tag("TIME-010", "一過性", "TIME", listOf("すぐ消える")),
        Tag("TIME-011", "恒常", "TIME", listOf("変わらない")),
        Tag("TIME-012", "積算", "TIME", listOf("少しずつ足される")),
        Tag("TIME-013", "蓄積", "TIME", listOf("溜まる")),
        Tag("TIME-014", "目減り", "TIME", listOf("減っていく")),
        Tag("TIME-015", "劣化進行", "TIME", listOf("傷みが進む")),
        Tag("TIME-016", "熟成進行", "TIME", listOf("成熟が進む")),
        Tag("TIME-017", "乾燥進行", "TIME", listOf("水分が抜ける")),
        Tag("TIME-018", "腐敗進行", "TIME", listOf("腐りが進む")),
        Tag("TIME-019", "予熱", "TIME", listOf("温まる前段")),
        Tag("TIME-020", "冷却", "TIME", listOf("冷えていく")),
        Tag("TIME-021", "準備段階", "TIME", listOf("本番前")),
        Tag("TIME-022", "移行段階", "TIME", listOf("切り替わり")),
        Tag("TIME-023", "終了間際", "TIME"),
        Tag("TIME-024", "先送り", "TIME", listOf("後回し")),
        Tag("TIME-025", "取り置き", "TIME"),
        Tag("TIME-026", "更新", "TIME", listOf("新しくする")),
        Tag("TIME-027", "置換", "TIME", listOf("入れ替わり")),
        Tag("TIME-028", "残滓", "TIME", listOf("過去の名残")),
        Tag("TIME-029", "風化", "TIME", listOf("薄れる")),
        Tag("TIME-030", "不可逆の経過", "TIME", listOf("取り返しのつかない時間")),

        // UNC (32)
        Tag("UNC-001", "不明点", "UNC"),
        Tag("UNC-002", "情報不足", "UNC"),
        Tag("UNC-003", "裏付け不足", "UNC", listOf("根拠不足")),
        Tag("UNC-004", "憶測可能", "UNC", listOf("推測の余地")),
        Tag("UNC-005", "伏せ情報", "UNC", listOf("秘匿")),
        Tag("UNC-006", "誤認余地", "UNC", listOf("見間違い")),
        Tag("UNC-007", "見かけ倒し", "UNC"),
        Tag("UNC-008", "期待値", "UNC", listOf("振れ幅")),
        Tag("UNC-009", "当たり外れ", "UNC"),
        Tag("UNC-010", "分岐", "UNC"),
        Tag("UNC-011", "多分岐", "UNC"),
        Tag("UNC-012", "偏り", "UNC", listOf("偏向")),
        Tag("UNC-013", "ゆらぎ", "UNC"),
        Tag("UNC-014", "ノイズ混入", "UNC", listOf("混ざりもの")),
        Tag("UNC-015", "偽装", "UNC", listOf("紛らわしさ")),
        Tag("UNC-016", "本物性", "UNC", listOf("真正らしさ")),
        Tag("UNC-017", "合図", "UNC", listOf("サイン")),
        Tag("UNC-018", "予兆", "UNC", listOf("気配")),
        Tag("UNC-019", "兆候ズレ", "UNC", listOf("サインと実態のズレ")),
        Tag("UNC-020", "検証待ち", "UNC"),
        Tag("UNC-021", "確認不能", "UNC"),
        Tag("UNC-022", "盲点", "UNC"),
        Tag("UNC-023", "死角", "UNC"),
        Tag("UNC-024", "視認困難", "UNC", listOf("読み取りにくい")),
        Tag("UNC-025", "透明化", "UNC", listOf("見えやすくなる")),
        Tag("UNC-026", "曖昧境界", "UNC"),
        Tag("UNC-027", "隠れ条件", "UNC"),
        Tag("UNC-028", "未開封", "UNC", listOf("未開示")),
        Tag("UNC-029", "封印", "UNC"),
        Tag("UNC-030", "解封", "UNC", listOf("封が解ける")),
        Tag("UNC-031", "公開", "UNC"),
        Tag("UNC-032", "露見", "UNC", listOf("隠しが破れる")),

        // STR (34)
        Tag("STR-001", "境界線", "STR"),
        Tag("STR-002", "閾値", "STR", listOf("しきい値")),
        Tag("STR-003", "内外差", "STR"),
        Tag("STR-004", "皮膜", "STR", listOf("薄い境")),
        Tag("STR-005", "殻", "STR"),
        Tag("STR-006", "蓋", "STR"),
        Tag("STR-007", "密閉", "STR"),
        Tag("STR-008", "開放", "STR"),
        Tag("STR-009", "連続", "STR"),
        Tag("STR-010", "断裂", "STR"),
        Tag("STR-011", "切断", "STR"),
        Tag("STR-012", "破断", "STR"),
        Tag("STR-013", "亀裂", "STR"),
        Tag("STR-014", "ひび", "STR"),
        Tag("STR-015", "継ぎ目", "STR"),
        Tag("STR-016", "接合", "STR"),
        Tag("STR-017", "固着", "STR"),
        Tag("STR-018", "剥離", "STR"),
        Tag("STR-019", "ほころび", "STR"),
        Tag("STR-020", "欠損", "STR"),
        Tag("STR-021", "欠け", "STR"),
        Tag("STR-022", "歪み", "STR"),
        Tag("STR-023", "変形", "STR"),
        Tag("STR-024", "反り", "STR"),
        Tag("STR-025", "へこみ", "STR"),
        Tag("STR-026", "圧痕", "STR"),
        Tag("STR-027", "均一", "STR", listOf("ムラが少ない")),
        Tag("STR-028", "まだら", "STR", listOf("ムラ")),
        Tag("STR-029", "混層", "STR"),
        Tag("STR-030", "積層", "STR"),
        Tag("STR-031", "粒度", "STR", listOf("粗い/細かい")),
        Tag("STR-032", "規格内", "STR"),
        Tag("STR-033", "規格外", "STR"),
        Tag("STR-034", "修復痕", "STR", listOf("直した跡")),

        // CTRL (28)
        Tag("CTRL-001", "介入可能", "CTRL"),
        Tag("CTRL-002", "介入不能", "CTRL"),
        Tag("CTRL-003", "手が届く", "CTRL"),
        Tag("CTRL-004", "手が届かない", "CTRL"),
        Tag("CTRL-005", "固定", "CTRL"),
        Tag("CTRL-006", "拘束", "CTRL"),
        Tag("CTRL-007", "解放", "CTRL"),
        Tag("CTRL-008", "抑制", "CTRL"),
        Tag("CTRL-009", "誘導", "CTRL"),
        Tag("CTRL-010", "押圧", "CTRL", listOf("圧力")),
        Tag("CTRL-011", "緊張", "CTRL", listOf("張り")),
        Tag("CTRL-012", "たるみ", "CTRL"),
        Tag("CTRL-013", "抵抗", "CTRL"),
        Tag("CTRL-014", "反発", "CTRL"),
        Tag("CTRL-015", "追従", "CTRL", listOf("力に合わせて変わる")),
        Tag("CTRL-016", "破壊介入", "CTRL"),
        Tag("CTRL-017", "非破壊介入", "CTRL"),
        Tag("CTRL-018", "切削", "CTRL", listOf("切る/削る")),
        Tag("CTRL-019", "破砕", "CTRL", listOf("砕く")),
        Tag("CTRL-020", "破裂", "CTRL"),
        Tag("CTRL-021", "注入", "CTRL"),
        Tag("CTRL-022", "排出", "CTRL"),
        Tag("CTRL-023", "取り出し", "CTRL"),
        Tag("CTRL-024", "封入", "CTRL", listOf("閉じ込める")),
        Tag("CTRL-025", "露出動作", "CTRL", listOf("見せる操作")),
        Tag("CTRL-026", "隠す動作", "CTRL", listOf("隠す操作")),
        Tag("CTRL-027", "取り扱い注意", "CTRL"),
        Tag("CTRL-028", "雑扱い", "CTRL", listOf("乱暴")),

        // SPACE (22)
        Tag("SPACE-001", "端", "SPACE"),
        Tag("SPACE-002", "中心", "SPACE"),
        Tag("SPACE-003", "奥", "SPACE"),
        Tag("SPACE-004", "手前", "SPACE"),
        Tag("SPACE-005", "上", "SPACE"),
        Tag("SPACE-006", "下", "SPACE"),
        Tag("SPACE-007", "高所", "SPACE"),
        Tag("SPACE-008", "低所", "SPACE"),
        Tag("SPACE-009", "密集", "SPACE"),
        Tag("SPACE-010", "空隙", "SPACE", listOf("隙間")),
        Tag("SPACE-011", "隣接", "SPACE"),
        Tag("SPACE-012", "重なり", "SPACE"),
        Tag("SPACE-013", "埋没", "SPACE", listOf("埋もれる")),
        Tag("SPACE-014", "孤立配置", "SPACE", listOf("単独配置")),
        Tag("SPACE-015", "行列", "SPACE", listOf("並び")),
        Tag("SPACE-016", "ずれ", "SPACE", listOf("列から外れる")),
        Tag("SPACE-017", "逆転", "SPACE", listOf("上下/表裏の反転")),
        Tag("SPACE-018", "影", "SPACE"),
        Tag("SPACE-019", "見晴らし", "SPACE"),
        Tag("SPACE-020", "境目配置", "SPACE", listOf("境界に置かれる")),
        Tag("SPACE-021", "退避", "SPACE", listOf("脇に寄せる")),
        Tag("SPACE-022", "陳列", "SPACE", listOf("見られる配置")),

        // VALUE (26)
        Tag("VALUE-001", "比較対象", "VALUE"),
        Tag("VALUE-002", "基準", "VALUE", listOf("物差し")),
        Tag("VALUE-003", "規格検査", "VALUE", listOf("検品")),
        Tag("VALUE-004", "合格", "VALUE"),
        Tag("VALUE-005", "不合格", "VALUE"),
        Tag("VALUE-006", "採用", "VALUE", listOf("選ばれる")),
        Tag("VALUE-007", "却下", "VALUE", listOf("選ばれない")),
        Tag("VALUE-008", "優先度高", "VALUE"),
        Tag("VALUE-009", "優先度低", "VALUE"),
        Tag("VALUE-010", "代替可能", "VALUE"),
        Tag("VALUE-011", "代替不能", "VALUE"),
        Tag("VALUE-012", "希少", "VALUE"),
        Tag("VALUE-013", "供給過多", "VALUE"),
        Tag("VALUE-014", "在庫", "VALUE"),
        Tag("VALUE-015", "取り置き対象", "VALUE"),
        Tag("VALUE-016", "売れ残り", "VALUE"),
        Tag("VALUE-017", "見切り", "VALUE", listOf("期限処理")),
        Tag("VALUE-018", "値引き", "VALUE"),
        Tag("VALUE-019", "値上げ", "VALUE"),
        Tag("VALUE-020", "期待外れ", "VALUE", listOf("評価下振れ")),
        Tag("VALUE-021", "当たり", "VALUE", listOf("評価上振れ")),
        Tag("VALUE-022", "外れ", "VALUE", listOf("評価下振れ")),
        Tag("VALUE-023", "用途限定", "VALUE"),
        Tag("VALUE-024", "役割付与", "VALUE"),
        Tag("VALUE-025", "儀式性", "VALUE", listOf("記念/供え物")),
        Tag("VALUE-026", "禁止/制限", "VALUE", listOf("規範で縛る")),

        // SENSE (12)
        Tag("SENSE-001", "強調", "SENSE", listOf("一点だけ目立つ")),
        Tag("SENSE-002", "反射", "SENSE"),
        Tag("SENSE-003", "逆光", "SENSE"),
        Tag("SENSE-004", "透過", "SENSE", listOf("透け")),
        Tag("SENSE-005", "濁り", "SENSE"),
        Tag("SENSE-006", "粗さ", "SENSE", listOf("ざらつき")),
        Tag("SENSE-007", "滑り", "SENSE", listOf("つるつる")),
        Tag("SENSE-008", "粘性", "SENSE", listOf("ねばり")),
        Tag("SENSE-009", "温度差", "SENSE"),
        Tag("SENSE-010", "湿り気", "SENSE"),
        Tag("SENSE-011", "乾き", "SENSE", listOf("乾燥感")),
        Tag("SENSE-012", "残り香", "SENSE")
    )

    // ====================
    // テンプレートフレーム（穴埋め式）
    // ====================
    data class TemplateFrame(
        val id: String,
        val facet: String,
        val text: String,
        val vars: List<String> = emptyList()
    )

    val templateFrames: List<TemplateFrame> = listOf(
        // CORE/汎用
        TemplateFrame("TF-GEN-001", "CORE", "それは【未実行/途中/直前/直後】で、【対象】はまだ【行為】されていない。", listOf("対象", "行為")),
        TemplateFrame("TF-GEN-002", "CORE", "【主体】が動けば、【対象】は【状態A】から【状態B】へ移る。", listOf("主体", "対象", "状態A", "状態B")),
        TemplateFrame("TF-GEN-003", "UNC", "外からは【情報】が言い切れず、【行為】で初めて決まる。", listOf("情報", "行為")),
        TemplateFrame("TF-GEN-004", "STR", "【境界】が入った瞬間、【連続】は終わる。", listOf("境界")),
        TemplateFrame("TF-GEN-005", "SPACE", "【対象】は【位置】にあり、次の動きで【結果】が変わる。", listOf("対象", "位置")),

        // TIME
        TemplateFrame("TF-TIME-001", "TIME", "それは【期限】まで【時間量】しか残っていない。"),
        TemplateFrame("TF-TIME-002", "TIME", "【対象】は【蓄積】で変わっていく（少しずつ、確実に）。", listOf("対象")),
        TemplateFrame("TF-TIME-003", "TIME", "変化は【加速/減速】していて、同じ速度では進まない。"),
        TemplateFrame("TF-TIME-004", "TIME", "ここから先は【一過性】で、同じ状態は戻らない。"),
        TemplateFrame("TF-TIME-005", "TIME", "【反復】のたびに、【対象】は少しずつ位置を変える。", listOf("対象")),
        TemplateFrame("TF-TIME-006", "TIME", "【移行段階】の境目にいて、どちらにも完全に属していない。"),
        TemplateFrame("TF-TIME-007", "TIME", "【目減り】しているのは【量/鮮度/時間】だ。"),
        TemplateFrame("TF-TIME-008", "TIME", "置かれたまま【先送り】されて、次の手が来ない。"),
        TemplateFrame("TF-TIME-009", "TIME", "【更新】され続ける場で、【対象】だけが古い時間を持つ。", listOf("対象")),
        TemplateFrame("TF-TIME-010", "TIME", "この経過は【不可逆】で、取り返しがつかない。"),

        // UNC
        TemplateFrame("TF-UNC-001", "UNC", "欠けているのは【情報】で、結論ではない。", listOf("情報")),
        TemplateFrame("TF-UNC-002", "UNC", "外見は整っているが、【見かけ倒し】の余地がある。"),
        TemplateFrame("TF-UNC-003", "UNC", "【盲点】が一箇所あり、そこだけ確かめられていない。"),
        TemplateFrame("TF-UNC-004", "UNC", "【分岐】があり、どちらへ転ぶかはまだ決まらない。"),
        TemplateFrame("TF-UNC-005", "UNC", "【当たり外れ】があるのに、外からは判別できない。"),
        TemplateFrame("TF-UNC-006", "UNC", "【予兆】はあるが、【裏付け】がない。"),
        TemplateFrame("TF-UNC-007", "UNC", "【合図】が出たら、一気に次へ進む。"),
        TemplateFrame("TF-UNC-008", "UNC", "【未開封/封印】が、情報の流通を止めている。"),
        TemplateFrame("TF-UNC-009", "UNC", "【解封/公開】が起これば、状況が反転する。"),
        TemplateFrame("TF-UNC-010", "UNC", "推測はできるが、断言はできない。"),

        // STR
        TemplateFrame("TF-STR-001", "STR", "【殻/蓋/皮膜】が、内側を守っている。"),
        TemplateFrame("TF-STR-002", "STR", "【密閉】されていて、外の影響が入りにくい。"),
        TemplateFrame("TF-STR-003", "STR", "【開放】された瞬間に、内外の差がなくなる。"),
        TemplateFrame("TF-STR-004", "STR", "【境界線】は薄いが、越えたら別物になる。"),
        TemplateFrame("TF-STR-005", "STR", "【閾値】を越えると、一気に崩れる。"),
        TemplateFrame("TF-STR-006", "STR", "【断裂/切断】は、状態ではなく\"出来事\"として迫っている。"),
        TemplateFrame("TF-STR-007", "STR", "【亀裂/ひび】は小さいが、方向だけははっきりしている。"),
        TemplateFrame("TF-STR-008", "STR", "【欠損/欠け】が、全体の印象を変える。"),
        TemplateFrame("TF-STR-009", "STR", "【均一】に見えるが、近づくと【まだら】がある。"),
        TemplateFrame("TF-STR-010", "STR", "【規格内/規格外】という線が、ここに引かれている。"),

        // CTRL
        TemplateFrame("TF-CTRL-001", "CTRL", "【主体】が触れれば、【対象】は従う（制御下）。", listOf("主体", "対象")),
        TemplateFrame("TF-CTRL-002", "CTRL", "【介入可能/不能】の差が、運命を分ける。"),
        TemplateFrame("TF-CTRL-003", "CTRL", "【固定/拘束】され、動ける範囲が決まっている。"),
        TemplateFrame("TF-CTRL-004", "CTRL", "【抑制】されているのは、動きではなく\"決行\"だ。"),
        TemplateFrame("TF-CTRL-005", "CTRL", "【押圧】で、形が静かに変わる。"),
        TemplateFrame("TF-CTRL-006", "CTRL", "【緊張/たるみ】が、次の破断を予告する。"),
        TemplateFrame("TF-CTRL-007", "CTRL", "【抵抗/反発】があり、簡単には変わらない。"),
        TemplateFrame("TF-CTRL-008", "CTRL", "【破壊介入】なら早いが、戻れない。"),
        TemplateFrame("TF-CTRL-009", "CTRL", "【非破壊介入】なら保てるが、限界がある。"),
        TemplateFrame("TF-CTRL-010", "CTRL", "【切削】は境界を作り、内側を露出させる。"),

        // SPACE
        TemplateFrame("TF-SPACE-001", "SPACE", "【端】にいる。落ちるか戻るかの境目だ。"),
        TemplateFrame("TF-SPACE-002", "SPACE", "【奥】にあり、手が伸びにくい。"),
        TemplateFrame("TF-SPACE-003", "SPACE", "【手前】にあり、最初に触れられる。"),
        TemplateFrame("TF-SPACE-004", "SPACE", "【上/下】の差が、圧力や優先を決める。"),
        TemplateFrame("TF-SPACE-005", "SPACE", "【重なり/埋没】で、見えなくなる。"),
        TemplateFrame("TF-SPACE-006", "SPACE", "【行列】の中で、【ずれ】だけが目立つ。"),
        TemplateFrame("TF-SPACE-007", "SPACE", "【影】が情報を隠し、【見晴らし】が情報を曝す。"),
        TemplateFrame("TF-SPACE-008", "SPACE", "【退避】され、主役の線から外れる。"),

        // VALUE
        TemplateFrame("TF-VALUE-001", "VALUE", "【基準】の前に置かれ、まだ判定は終わっていない。", listOf("基準")),
        TemplateFrame("TF-VALUE-002", "VALUE", "【比較対象】が来た瞬間、序列が決まる。"),
        TemplateFrame("TF-VALUE-003", "VALUE", "【合格/不合格】の線が、目に見えないところにある。"),
        TemplateFrame("TF-VALUE-004", "VALUE", "【優先度】があり、先に消えるものが決まっている。"),
        TemplateFrame("TF-VALUE-005", "VALUE", "【代替可能】なら、選ばれない理由が増える。"),
        TemplateFrame("TF-VALUE-006", "VALUE", "【代替不能】なら、多少の欠点は許される。"),
        TemplateFrame("TF-VALUE-007", "VALUE", "【在庫/売れ残り】は位置と時間の問題でもある。"),
        TemplateFrame("TF-VALUE-008", "VALUE", "【見切り/値引き】は価値の再定義だ。"),

        // SENSE
        TemplateFrame("TF-SENSE-001", "SENSE", "【強調】される一点が、全体の意味を引っ張る。"),
        TemplateFrame("TF-SENSE-002", "SENSE", "【反射】が走り、表面が\"硬さ/新しさ\"を見せる。"),
        TemplateFrame("TF-SENSE-003", "SENSE", "【逆光】で輪郭だけが残り、内側は読めない。"),
        TemplateFrame("TF-SENSE-004", "SENSE", "【温度差】が、時間の経過を体に知らせる。")
    )

    // ====================
    // 軸とファセットの関連
    // ====================
    val axisAffinityByFacet: Map<String, List<Int>> = mapOf(
        "CORE" to listOf(1, 3, 4, 19, 20),
        "TIME" to listOf(1, 2, 12, 13, 14),
        "UNC" to listOf(3, 16, 1, 20),
        "STR" to listOf(3, 4, 17, 10, 8),
        "CTRL" to listOf(19, 11, 4, 6),
        "SPACE" to listOf(5, 6, 16, 7),
        "VALUE" to listOf(20, 5, 1),
        "SENSE" to listOf(16, 10, 12, 13, 14, 15, 11)
    )

    // ====================
    // 抽象テーマ（禁止ワード候補）
    // ====================
    val commonAbstractThemes: List<String> = listOf(
        "期待", "孤独", "喪失", "信頼", "後悔", "希望", "不安", "焦燥",
        "愛", "憎しみ", "悲しみ", "喜び", "恐怖", "怒り", "嫉妬", "羨望",
        "安心", "絶望", "達成感", "虚無感", "充実", "空虚", "緊張", "解放"
    )

    // ====================
    // ヘルパー関数
    // ====================

    fun getTagsByFacet(facetKey: String): List<Tag> {
        return tags.filter { it.facet == facetKey }
    }

    fun getTagById(tagId: String): Tag? {
        return tags.find { it.id == tagId }
    }

    fun getAxisById(axisId: Int): Axis? {
        return axes.find { it.id == axisId }
    }

    fun getFacetByKey(facetKey: String): Facet? {
        return facets.find { it.key == facetKey }
    }

    fun getTemplatesByFacet(facetKey: String): List<TemplateFrame> {
        return templateFrames.filter { it.facet == facetKey }
    }

    fun getRecommendedTagsForAxes(axisIds: List<Int>, mode: String = "abstract"): List<Tag> {
        val recommendedFacets = axisIds.flatMap { axisId ->
            axisAffinityByFacet.entries
                .filter { (_, affineAxes) -> axisId in affineAxes }
                .map { it.key }
        }.distinct()

        return recommendedFacets.flatMap { facetKey ->
            val facet = getFacetByKey(facetKey)
            val score = if (mode == "abstract") facet?.abstractScore ?: 0f else facet?.sensoryScore ?: 0f
            if (score >= 0.6f) getTagsByFacet(facetKey) else emptyList()
        }.distinctBy { it.id }
    }
}
