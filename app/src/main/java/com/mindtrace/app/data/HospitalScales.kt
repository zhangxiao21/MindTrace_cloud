package com.mindtrace.app.data

/**
 * 国内医院与医疗体系常用的自评 / 筛查量表。
 *
 * 追溯方式：每个量表的 source / sourceEn 给出原始文献（作者、年份、期刊）、
 * 国内通行中文译本依据与可核对的公开链接；note / noteEn 说明计分口径、
 * 使用边界与授权要求。题目与选项均为中英双语，接入 ScaleCatalog.all 后
 * 自动出现在「全部量表」的独立分类中，复用现有的逐题作答、分级、来源
 * 展示、补记日期、趋势与复诊报告链路。
 *
 * 未纳入的常见量表：SCL-90、HAMD / HAMA、MMSE / MoCA、PANSS、Y-BOCS 等
 * 属于受版权保护或须由经过培训的专业人员施测 / 评分的工具，不适合以
 * 「自评问卷」形式放进本机应用。
 */
object HospitalScales {

    private const val CATEGORY = "国内医院常用"
    private const val CATEGORY_EN = "Commonly used in Chinese hospitals"

    private fun o(zh: String, en: String, value: Int) = ScaleOption(zh, en, value)
    private fun q(zh: String, en: String, options: List<ScaleOption>) = ScaleQuestion(zh, en, options)
    private fun b(
        min: Int,
        max: Int,
        key: String,
        label: String,
        labelEn: String,
        advice: String,
        adviceEn: String
    ) = SeverityBand(min, max, key, label, labelEn, advice, adviceEn)

    // ---------- 宗氏（Zung）SAS / SDS ----------

    private val zungOptions = listOf(
        o("没有或很少时间", "None or a little of the time", 1),
        o("小部分时间", "Some of the time", 2),
        o("相当多时间", "A good part of the time", 3),
        o("绝大部分或全部时间", "Most or all of the time", 4)
    )

    /** 反向计分题：选项顺序不变，权重按 4/3/2/1 反转。 */
    private val zungReversed = zungOptions.map { o(it.label, it.labelEn, 5 - it.value) }

    private fun zungQuestions(spec: List<Triple<String, String, Boolean>>): List<ScaleQuestion> =
        spec.map { (zh, en, reversed) -> q(zh, en, if (reversed) zungReversed else zungOptions) }

    val zungSas = ScaleDef(
        type = "ZUNGSAS",
        name = "宗氏焦虑自评量表（SAS）",
        nameEn = "Zung Self-Rating Anxiety Scale (SAS)",
        shortName = "SAS（宗氏）",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "国内精神科与综合医院最常用的焦虑自评工具之一，20 个条目覆盖紧张、害怕、心悸、头晕等心理与躯体感受，按标准分判读。",
        descriptionEn = "One of the most widely used anxiety self-rating scales in Chinese hospitals: 20 items covering psychological and physical symptoms, read as an index score.",
        timeframe = "最近一周",
        timeframeEn = "Past week",
        instruction = "请根据最近一周的实际感受，选择每种情况出现的时间比例。",
        instructionEn = "Based on the past week, choose how much of the time each statement applied to you.",
        questions = zungQuestions(
            listOf(
                Triple("我觉得比平常容易紧张和着急", "I feel more nervous and anxious than usual", false),
                Triple("我无缘无故地感到害怕", "I feel afraid for no reason at all", false),
                Triple("我容易心里烦乱或觉得惊恐", "I get upset easily or feel panicky", false),
                Triple("我觉得我可能将要发疯", "I feel like I am falling apart and going to pieces", false),
                Triple("我觉得一切都很好，也不会发生什么不幸", "I feel that everything is all right and nothing bad will happen", true),
                Triple("我手脚发抖打颤", "My arms and legs shake and tremble", false),
                Triple("我因为头痛、颈痛和背痛而苦恼", "I am bothered by headaches, neck and back pain", false),
                Triple("我觉得容易衰弱和疲乏", "I feel weak and get tired easily", false),
                Triple("我觉得心平气和，并且容易安静坐着", "I feel calm and can sit quietly with ease", true),
                Triple("我觉得心跳得很快", "I can feel my heart beating fast", false),
                Triple("我因为一阵阵头晕而苦恼", "I am bothered by dizzy spells", false),
                Triple("我有晕倒发作，或觉得要晕倒似的", "I have fainting spells or feel like fainting", false),
                Triple("我吸气呼气都感到很容易", "I can breathe in and out easily", true),
                Triple("我的手脚麻木和刺痛", "I get feelings of numbness and tingling in my hands or feet", false),
                Triple("我因为胃痛和消化不良而苦恼", "I am bothered by stomach aches or indigestion", false),
                Triple("我常常要小便", "I have to empty my bladder often", false),
                Triple("我的手常常是干燥温暖的", "My hands are usually dry and warm", true),
                Triple("我脸红发热", "My face gets hot and blushes", false),
                Triple("我容易入睡并且一夜睡得很好", "I fall asleep easily and get a good night's rest", true),
                Triple("我做恶梦", "I have bad dreams", false)
            )
        ),
        bands = listOf(
            b(25, 49, "normal", "无明显焦虑", "No significant anxiety", "标准分未达到国内常用的 50 分界值。若仍有明显不适，可以如实记录并在复诊时告诉医生。", "Your index score is below the commonly used cut-off of 50. If you still feel unwell, keep recording and mention it at your next visit."),
            b(50, 59, "mild", "轻度焦虑", "Mild anxiety", "标准分达到 50 分。可以观察它与睡眠、用药、压力事件的关联，并在复诊时把这段记录带给医生。", "Your index score reaches 50. Look at how it relates to sleep, medication and stressful events, and bring these records to your appointment."),
            b(60, 69, "moderate", "中度焦虑", "Moderate anxiety", "焦虑相关症状较明显，建议尽早与医生或心理咨询师讨论，不要自行调整药物。", "Anxiety symptoms appear moderately prominent. Consider discussing this with a clinician soon; do not change medication on your own."),
            b(70, 100, "severe", "重度焦虑", "Severe anxiety", "分数较高，建议尽快就诊。如果同时出现伤害自己的念头，请立即联系 120 / 110 或 12356。", "This is a high score; please seek clinical care soon. If you also have thoughts of harming yourself, contact 120 / 110 or 12356 immediately.")
        ),
        scoring = ScaleScoring.ZUNG,
        maxScoreOverride = 100,
        estimatedMinutes = 5,
        featured = true,
        source = "Zung WWK (1971). A rating instrument for anxiety disorders. Psychosomatics 12(6): 371–379。国内通行中文条目与 20 题 1–4 分、标准分换算见汪向东等《心理卫生评定量表手册》（中国心理卫生杂志社，1999 增订版）。条目核对：https://en.wikipedia.org/wiki/Zung_Self-Rating_Anxiety_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Zung+1971+Self-Rating+Anxiety+Scale",
        sourceEn = "Zung WWK (1971). A rating instrument for anxiety disorders. Psychosomatics 12(6): 371-379. Chinese items and the 1-4 / index-score conversion follow the Chinese Mental Health Rating Scale Manual (Wang Xiangdong et al., 1999). Cross-check: https://en.wikipedia.org/wiki/Zung_Self-Rating_Anxiety_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Zung+1971+Self-Rating+Anxiety+Scale",
        note = "结果按标准分（粗分 × 1.25）显示，国内常以 50 分为筛查界值；5 道正向表述题目按原量表反向计分。它是筛查与追踪工具，不是诊断；心悸、头晕等也可能来自躯体疾病或药物副作用，正式使用或电子化再分发请先确认授权。",
        noteEn = "Shown as the index score (raw x 1.25); 50 is the usual screening cut-off in China, and five positively worded items are reverse-scored. This is a screening and tracking tool, not a diagnosis; palpitations and dizziness can also come from physical illness or medication. Confirm licensing before formal or electronic redistribution."
    )

    val zungSds = ScaleDef(
        type = "ZUNGSDS",
        name = "宗氏抑郁自评量表（SDS）",
        nameEn = "Zung Self-Rating Depression Scale (SDS)",
        shortName = "SDS（宗氏）",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "国内医院最常用的抑郁自评工具之一，20 个条目覆盖情绪、睡眠、食欲、精力与无望感，按标准分判读。",
        descriptionEn = "One of the most widely used depression self-rating scales in Chinese hospitals: 20 items covering mood, sleep, appetite, energy and hopelessness, read as an index score.",
        timeframe = "最近一周",
        timeframeEn = "Past week",
        instruction = "请根据最近一周的实际感受，选择每种情况出现的时间比例。",
        instructionEn = "Based on the past week, choose how much of the time each statement applied to you.",
        questions = zungQuestions(
            listOf(
                Triple("我感到情绪沮丧、郁闷", "I feel down-hearted and blue", false),
                Triple("早晨是我心情最好的时候", "Morning is when I feel the best", true),
                Triple("我要哭或想哭", "I have crying spells or feel like crying", false),
                Triple("我夜间睡眠不好", "I have trouble sleeping at night", false),
                Triple("我吃饭像平时一样多", "I eat as much as I used to", true),
                Triple("我与异性密切接触时和以往一样感到愉快", "I still enjoy contact with others as much as before", true),
                Triple("我感到体重减轻", "I notice that I am losing weight", false),
                Triple("我为便秘而烦恼", "I am bothered by constipation", false),
                Triple("我的心跳比平时快", "My heart beats faster than usual", false),
                Triple("我无故感到疲劳", "I get tired for no reason", false),
                Triple("我的头脑像往常一样清楚", "My mind is as clear as it used to be", true),
                Triple("我做事情像平时一样不感到困难", "I find it easy to do the things I used to do", true),
                Triple("我坐卧不安，难以保持平静", "I am restless and cannot keep still", false),
                Triple("我对未来感到有希望", "I feel hopeful about the future", true),
                Triple("我比平时更容易激怒", "I am more irritable than usual", false),
                Triple("我觉得决定什么事很容易", "I find it easy to make decisions", true),
                Triple("我感到自己是有用的、不可缺少的人", "I feel that I am useful and needed", true),
                Triple("我的生活很有意义", "My life feels meaningful and full", true),
                Triple("假若我死了，别人会过得更好", "I feel that others would be better off if I were dead", false),
                Triple("我仍旧喜爱自己平时喜爱的东西", "I still enjoy the things I used to enjoy", true)
            )
        ),
        bands = listOf(
            b(25, 52, "normal", "无明显抑郁", "No significant depression", "标准分未达到国内常用的 53 分界值。情绪仍值得持续记录，波动本身就是有用的复诊信息。", "Your index score is below the commonly used cut-off of 53. Keep tracking: changes over time are useful information for your appointment."),
            b(53, 62, "mild", "轻度抑郁", "Mild depression", "标准分达到 53 分。可以看看它与睡眠、用药、季节或生活事件的关联，并在复诊时讨论。", "Your index score reaches 53. Look at links with sleep, medication, season or life events, and discuss them at your appointment."),
            b(63, 72, "moderate", "中度抑郁", "Moderate depression", "抑郁相关症状较明显，建议尽早与医生或心理咨询师讨论，不要自行停药或改药。", "Depressive symptoms appear moderately prominent. Please discuss this with a clinician soon; do not stop or change medication on your own."),
            b(73, 100, "severe", "重度抑郁", "Severe depression", "分数较高，建议尽快就诊。如果同时出现伤害自己的念头，请立即联系 120 / 110 或 12356。", "This is a high score; please seek clinical care soon. If you also have thoughts of harming yourself, contact 120 / 110 or 12356 immediately.")
        ),
        scoring = ScaleScoring.ZUNG,
        maxScoreOverride = 100,
        estimatedMinutes = 5,
        featured = true,
        selfHarmQuestionIndex = 18,
        source = "Zung WWK (1965). A self-rating depression scale. Archives of General Psychiatry 12: 63–70。国内通行中文条目、10 道反向计分题与标准分换算见汪向东等《心理卫生评定量表手册》（中国心理卫生杂志社，1999 增订版）。条目核对：https://en.wikipedia.org/wiki/Zung_Self-Rating_Depression_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Zung+1965+Self-Rating+Depression+Scale",
        sourceEn = "Zung WWK (1965). A self-rating depression scale. Archives of General Psychiatry 12: 63-70. Chinese items, the 10 reverse-scored items and the index conversion follow the Chinese Mental Health Rating Scale Manual (Wang Xiangdong et al., 1999). Cross-check: https://en.wikipedia.org/wiki/Zung_Self-Rating_Depression_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Zung+1965+Self-Rating+Depression+Scale",
        note = "结果按标准分（粗分 × 1.25）显示，国内常以 53 分为筛查界值；10 道正向表述题目按原量表反向计分。第 19 题涉及活着是否有意义，回答偏重时应用会提供紧急支持入口。量表不是诊断，正式使用或电子化再分发请先确认授权。",
        noteEn = "Shown as the index score (raw x 1.25); 53 is the usual screening cut-off in China, and ten positively worded items are reverse-scored. Item 19 touches on whether life is worth living, so a non-minimal answer opens the crisis-support entry. This is not a diagnosis; confirm licensing before formal or electronic redistribution."
    )

    // ---------- 综合医院 / 专科常用量表 ----------

    private val hadsOptions = listOf(
        o("完全没有 / 从不", "Not at all", 0),
        o("偶尔 / 有时", "Sometimes", 1),
        o("经常", "Often", 2),
        o("几乎总是 / 大部分时间", "Most of the time", 3)
    )
    private val hadsReversed = hadsOptions.mapIndexed { index, option ->
        o(option.label, option.labelEn, 3 - index)
    }
    private fun hadsQuestions(spec: List<Triple<String, String, Boolean>>): List<ScaleQuestion> =
        spec.map { (zh, en, reversed) -> q(zh, en, if (reversed) hadsReversed else hadsOptions) }

    val hads = ScaleDef(
        type = "HADS",
        name = "医院焦虑抑郁量表（HADS）",
        nameEn = "Hospital Anxiety and Depression Scale (HADS)",
        shortName = "HADS",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "综合医院内科、肿瘤科与术前评估常用的 14 题情绪筛查，分焦虑与抑郁两个分量表，不含躯体症状条目。",
        descriptionEn = "A 14-item mood screen widely used in general hospitals, oncology and pre-operative assessment, with separate anxiety and depression subscales and no somatic items.",
        timeframe = "最近一周",
        timeframeEn = "Past week",
        instruction = "请根据最近一周的感受选择；题目按焦虑、抑郁交替排列，结果页会分别给出两个分量表分数。",
        instructionEn = "Answer for the past week. Anxiety and depression items alternate; the result page shows both subscale scores.",
        questions = hadsQuestions(
            listOf(
                Triple("我感到紧张或痛苦", "I feel tense or wound up", false),
                Triple("我仍能享受以往喜欢的事物", "I still enjoy the things I used to enjoy", true),
                Triple("我有一种害怕的感觉，好像有可怕的事要发生", "I get a frightened feeling as if something awful is about to happen", false),
                Triple("我能发笑并看到事情有趣的一面", "I can laugh and see the funny side of things", true),
                Triple("烦恼的念头在我脑中盘旋", "Worrying thoughts go through my mind", false),
                Triple("我感到愉快、心情开朗", "I feel cheerful", true),
                Triple("我能安闲而轻松地坐着", "I can sit at ease and feel relaxed", true),
                Triple("我觉得自己好像变慢了", "I feel as if I am slowed down", false),
                Triple("我有一种胃里翻腾似的害怕感觉", "I get a frightened feeling like butterflies in the stomach", false),
                Triple("我已不再在意自己的外表", "I have lost interest in my appearance", false),
                Triple("我感到坐立不安，好像必须活动", "I feel restless and have to be on the move", false),
                Triple("我对将来的事抱有期待", "I look forward with enjoyment to things", true),
                Triple("我会突然感到恐慌", "I get sudden feelings of panic", false),
                Triple("我能好好欣赏一本书、广播或电视节目", "I can enjoy a good book, radio or TV programme", true)
            )
        ),
        bands = listOf(
            b(0, 14, "normal", "未见明显情绪困扰", "No significant distress", "总分不高。结果页给出的两个分量表分数更适合与医生逐项讨论。", "The total is low. The two subscale scores on the result page are the better basis for discussion with your clinician."),
            b(15, 21, "moderate", "轻中度情绪困扰", "Mild to moderate distress", "建议把焦虑、抑郁两个分量表分数分别带给医生看，并结合躯体病情一起判断。", "Take both subscale scores to your clinician and interpret them alongside your physical condition."),
            b(22, 42, "severe", "明显情绪困扰", "Marked distress", "情绪困扰较明显，建议尽快请医生或心理专业人员评估；若出现伤害自己的念头，请联系 120 / 110 或 12356。", "Distress appears marked; please seek clinical or psychological assessment soon. If you have thoughts of harming yourself, contact 120 / 110 or 12356.")
        ),
        estimatedMinutes = 4,
        featured = false,
        source = "Zigmond AS & Snaith RP (1983). The Hospital Anxiety and Depression Scale. Acta Psychiatrica Scandinavica 67(6): 361–370。国内综合医院常用译本见《心理卫生评定量表手册》与《中国综合医院焦虑抑郁量表应用共识》。条目与 0–3 计分核对：https://en.wikipedia.org/wiki/Hospital_Anxiety_and_Depression_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Zigmond+Snaith+1983+Hospital+Anxiety+Depression+Scale",
        sourceEn = "Zigmond AS & Snaith RP (1983). The Hospital Anxiety and Depression Scale. Acta Psychiatrica Scandinavica 67(6): 361-370. Chinese versions are widely used in general hospitals. Items and 0-3 scoring: https://en.wikipedia.org/wiki/Hospital_Anxiety_and_Depression_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Zigmond+Snaith+1983+Hospital+Anxiety+Depression+Scale",
        note = "为便于手机端逐题作答，选项统一为四级频率并按原量表方向计分（6 道正向表述题反向计分）；原版每题选项措辞各不相同。临床更常按焦虑 / 抑郁两个分量表各 0–21 分分别判读（常用界值 8 / 11），结果页已给出分量表分数。HADS 受版权保护，正式或电子化再分发须取得授权；量表不是诊断。",
        noteEn = "For one-question-per-screen use, options are unified into four frequency levels with original scoring direction (six positively worded items are reverse-scored); the paper version uses different wording per item. Clinicians usually read the anxiety and depression subscales separately (0-21 each, usual cut-offs 8 / 11), and both are shown on the result page. HADS is copyrighted: obtain permission for formal or electronic redistribution. Not a diagnosis."
    )

    private val gdsYes = listOf(o("是", "Yes", 1), o("否", "No", 0))
    private val gdsNo = listOf(o("是", "Yes", 0), o("否", "No", 1))
    private fun gdsQuestions(spec: List<Triple<String, String, Boolean>>): List<ScaleQuestion> =
        spec.map { (zh, en, yesScores) -> q(zh, en, if (yesScores) gdsYes else gdsNo) }

    val gds15 = ScaleDef(
        type = "GDS15",
        name = "老年抑郁量表简版（GDS-15）",
        nameEn = "Geriatric Depression Scale, 15-item (GDS-15)",
        shortName = "GDS-15",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "老年科、记忆门诊与社区慢病随访常用的 15 题是 / 否自评，避开躯体症状，适合与躯体疾病共存时筛查情绪。",
        descriptionEn = "A 15-item yes/no screen common in geriatrics, memory clinics and community chronic-care follow-up; it avoids somatic items, which helps when physical illness is present.",
        timeframe = "最近一周",
        timeframeEn = "Past week",
        instruction = "请按最近一周的感受回答「是」或「否」。",
        instructionEn = "Answer yes or no based on the past week.",
        questions = gdsQuestions(
            listOf(
                Triple("你对自己的生活基本上满意吗？", "Are you basically satisfied with your life?", false),
                Triple("你是否已放弃了许多活动与兴趣？", "Have you dropped many of your activities and interests?", true),
                Triple("你是否觉得生活空虚？", "Do you feel that your life is empty?", true),
                Triple("你是否常感到厌倦？", "Do you often get bored?", true),
                Triple("你是否大部分时间精力充沛？", "Are you in good spirits most of the time?", false),
                Triple("你是否害怕会有不幸的事落到你头上？", "Are you afraid that something bad is going to happen to you?", true),
                Triple("你是否大部分时间感到快乐？", "Do you feel happy most of the time?", false),
                Triple("你是否常感到无助？", "Do you often feel helpless?", true),
                Triple("你是否宁愿待在家里，而不愿外出做些新鲜事？", "Do you prefer to stay at home rather than go out and do new things?", true),
                Triple("你是否觉得记忆力比以前差？", "Do you feel you have more problems with memory than most people?", true),
                Triple("你觉得现在活着很惬意吗？", "Do you think it is wonderful to be alive now?", false),
                Triple("你是否觉得像现在这样活着毫无意义？", "Do you feel pretty worthless the way you are now?", true),
                Triple("你觉得精力充沛吗？", "Do you feel full of energy?", false),
                Triple("你是否觉得自己的处境毫无希望？", "Do you feel that your situation is hopeless?", true),
                Triple("你是否觉得大多数人处境比自己好？", "Do you think that most people are better off than you are?", true)
            )
        ),
        bands = listOf(
            b(0, 4, "normal", "未见明显抑郁", "No significant depression", "未达到常用的 5 分界值。老年人情绪变化也常与药物、睡眠和躯体疾病相关，可持续记录。", "Below the usual cut-off of 5. In older adults, mood changes often relate to medication, sleep or physical illness, so keep tracking."),
            b(5, 9, "mild", "提示抑郁可能", "Possible depression", "达到常用的 5 分筛查界值，建议与医生或家属一起复看这几题，并安排进一步评估。", "At or above the usual screening cut-off of 5. Review these items with a clinician or family member and arrange further assessment."),
            b(10, 15, "severe", "很可能抑郁", "Likely depression", "达到常用的 10 分界值，建议尽快到老年科 / 精神心理科评估；若出现伤害自己的念头，请联系 120 / 110 或 12356。", "At or above the usual cut-off of 10. Please seek geriatric or mental-health assessment soon. If you have thoughts of harming yourself, contact 120 / 110 or 12356.")
        ),
        estimatedMinutes = 3,
        featured = false,
        source = "Sheikh JI & Yesavage JA (1986). Geriatric Depression Scale (GDS): recent evidence and development of a shorter version. Clinical Gerontologist 5(1–2): 165–173；原始量表 Yesavage 等 (1983), Journal of Psychiatric Research 17(1): 37–49。作者声明 GDS 可自由用于临床与研究（需注明出处）。条目与界值核对：https://en.wikipedia.org/wiki/Geriatric_Depression_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Sheikh+Yesavage+1986+Geriatric+Depression+Scale+short+version",
        sourceEn = "Sheikh JI & Yesavage JA (1986). Geriatric Depression Scale (GDS): recent evidence and development of a shorter version. Clinical Gerontologist 5(1-2): 165-173; original scale: Yesavage et al. (1983), Journal of Psychiatric Research 17(1): 37-49. The authors allow free clinical and research use with attribution. Items and cut-offs: https://en.wikipedia.org/wiki/Geriatric_Depression_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Sheikh+Yesavage+1986+Geriatric+Depression+Scale+short+version",
        note = "5 道正向表述题目按原量表反向计分；常用界值为 5 分提示可能、10 分提示很可能抑郁。它是筛查工具，不能替代老年科 / 精神科评估，也不用于判断认知功能。",
        noteEn = "Five positively worded items are reverse-scored; usual cut-offs are 5 for possible and 10 for probable depression. This is a screening tool, not a substitute for geriatric or psychiatric assessment, and it does not measure cognition."
    )

    private val severityOptions = listOf(
        o("没有 / 正常", "None / normal", 0),
        o("轻微", "Mild", 1),
        o("明显 / 中度", "Moderate", 2),
        o("严重", "Severe", 3)
    )

    val ais = ScaleDef(
        type = "AIS",
        name = "阿森斯失眠量表（AIS）",
        nameEn = "Athens Insomnia Scale (AIS)",
        shortName = "AIS",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "国内睡眠门诊常用的 8 题自评，覆盖入睡、夜醒、早醒、总睡眠时长、睡眠质量与日间功能。",
        descriptionEn = "An 8-item self-rating scale common in Chinese sleep clinics, covering sleep onset, night awakenings, early awakening, total sleep time, sleep quality and daytime function.",
        timeframe = "最近一个月（每周至少三次）",
        timeframeEn = "Past month (at least three times a week)",
        instruction = "请针对最近一个月里每周至少出现三次的情况，选择受影响的程度。",
        instructionEn = "For problems occurring at least three times a week over the past month, choose how severe they were.",
        questions = listOf(
            q("关灯后到入睡所需的时间延长", "Time needed to fall asleep after lights out", severityOptions),
            q("夜间醒来、难以再入睡", "Awakenings during the night", severityOptions),
            q("比期望的时间更早醒来", "Final awakening earlier than desired", severityOptions),
            q("总睡眠时间不足", "Total sleep duration too short", severityOptions),
            q("对整体睡眠质量不满意", "Overall quality of sleep unsatisfactory", severityOptions),
            q("白天情绪低落或易烦躁", "Daytime mood or irritability affected", severityOptions),
            q("白天体力、注意力或活动能力下降", "Daytime physical or mental functioning affected", severityOptions),
            q("白天困倦、想睡", "Daytime sleepiness", severityOptions)
        ),
        bands = listOf(
            b(0, 5, "normal", "未见明显失眠", "No significant insomnia", "未达到常用的 6 分界值。睡眠仍值得在每日打卡里持续记录。", "Below the usual cut-off of 6. Sleep is still worth tracking in your daily check-in."),
            b(6, 10, "mild", "提示失眠可能", "Possible insomnia", "达到常用的 6 分界值。可结合每日睡眠时长、用药与咖啡因摄入一起看，并在复诊时讨论。", "At or above the usual cut-off of 6. Compare with your daily sleep duration, medication and caffeine, and discuss at your appointment."),
            b(11, 24, "severe", "失眠较明显", "Marked insomnia", "分数较高，建议到睡眠门诊 / 精神心理科评估；不要自行加用或停用助眠药物。", "This is a high score; consider a sleep-clinic or mental-health assessment. Do not start or stop sleep medication on your own.")
        ),
        estimatedMinutes = 2,
        featured = false,
        source = "Soldatos CR, Dikeos DG & Paparrigopoulos TJ (2000). Athens Insomnia Scale: validation of an instrument based on ICD-10 criteria. Journal of Psychosomatic Research 48(6): 555–560；国内睡眠门诊通行中文译本见《中国失眠障碍诊断和治疗指南》。条目与 0–3 计分核对：https://en.wikipedia.org/wiki/Athens_Insomnia_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Soldatos+2000+Athens+Insomnia+Scale",
        sourceEn = "Soldatos CR, Dikeos DG & Paparrigopoulos TJ (2000). Athens Insomnia Scale: validation of an instrument based on ICD-10 criteria. Journal of Psychosomatic Research 48(6): 555-560; Chinese versions are used in sleep clinics. Items and 0-3 scoring: https://en.wikipedia.org/wiki/Athens_Insomnia_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Soldatos+2000+Athens+Insomnia+Scale",
        note = "按原量表 8 题各 0–3 分求和，国内常以 6 分为界。为便于手机端作答，选项统一为四级程度；原版各题选项措辞略有不同。它是筛查与追踪工具，不是失眠障碍的诊断。",
        noteEn = "Eight items scored 0-3 each; 6 is the commonly used cut-off in China. Options are unified into four severity levels for one-question-per-screen use, while the paper version words each item differently. Screening and tracking only, not a diagnosis of insomnia disorder."
    )

    private val essOptions = listOf(
        o("从不打瞌睡", "Would never doze", 0),
        o("轻度可能", "Slight chance of dozing", 1),
        o("中度可能", "Moderate chance of dozing", 2),
        o("很可能打瞌睡", "High chance of dozing", 3)
    )

    val ess = ScaleDef(
        type = "ESS",
        name = "Epworth 嗜睡量表（ESS）",
        nameEn = "Epworth Sleepiness Scale (ESS)",
        shortName = "ESS",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "呼吸科、睡眠中心与神经内科评估白天过度嗜睡的 8 题自评，也常用于睡眠呼吸暂停的初筛与随访。",
        descriptionEn = "An 8-item daytime-sleepiness scale used in respiratory medicine, sleep centres and neurology, including screening and follow-up for sleep apnoea.",
        timeframe = "最近一段时间 / 总体",
        timeframeEn = "Recent weeks / in general",
        instruction = "请估计在以下情境中打瞌睡或睡着的可能性，与是否真的经历过无关。",
        instructionEn = "Rate how likely you would be to doze off or fall asleep in each situation, whether or not it has actually happened.",
        questions = listOf(
            q("坐着阅读时", "Sitting and reading", essOptions),
            q("看电视时", "Watching TV", essOptions),
            q("在公共场所静坐不动时（如剧院、会议中）", "Sitting inactive in a public place (e.g. theatre or meeting)", essOptions),
            q("作为乘客连续乘车 1 小时不休息", "As a passenger in a car for an hour without a break", essOptions),
            q("环境许可时下午躺下休息", "Lying down to rest in the afternoon when circumstances permit", essOptions),
            q("坐着与人交谈时", "Sitting and talking to someone", essOptions),
            q("午餐未饮酒后安静坐着时", "Sitting quietly after a lunch without alcohol", essOptions),
            q("遇堵车时停车数分钟", "In a car, while stopped for a few minutes in traffic", essOptions)
        ),
        bands = listOf(
            b(0, 10, "normal", "白天嗜睡不明显", "Normal daytime alertness", "未超过常用的 10 分界值。若白天仍明显困倦，可结合睡眠时长、用药与打鼾情况记录。", "At or below the usual cut-off of 10. If you still feel sleepy, track sleep duration, medication and snoring alongside it."),
            b(11, 15, "moderate", "轻中度嗜睡", "Mild to moderate sleepiness", "超过 10 分，建议与医生讨论睡眠呼吸、药物或作息因素。", "Above 10. Discuss sleep-disordered breathing, medication and schedule with a clinician."),
            b(16, 24, "severe", "明显嗜睡", "Marked sleepiness", "分数较高，建议到睡眠中心评估；开车或操作机械时请特别留意安全。", "This is a high score; consider a sleep-centre assessment and take extra care when driving or operating machinery.")
        ),
        estimatedMinutes = 2,
        featured = false,
        source = "Johns MW (1991). A new method for measuring daytime sleepiness: the Epworth Sleepiness Scale. Sleep 14(6): 540–545。作者允许在临床与研究中免费使用并注明出处；国内睡眠中心通行译本见相关诊疗指南。条目与 0–3 计分核对：https://en.wikipedia.org/wiki/Epworth_Sleepiness_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Johns+1991+Epworth+Sleepiness+Scale",
        sourceEn = "Johns MW (1991). A new method for measuring daytime sleepiness: the Epworth Sleepiness Scale. Sleep 14(6): 540-545. The author permits free clinical and research use with attribution. Items and 0-3 scoring: https://en.wikipedia.org/wiki/Epworth_Sleepiness_Scale · https://pubmed.ncbi.nlm.nih.gov/?term=Johns+1991+Epworth+Sleepiness+Scale",
        note = "8 题各 0–3 分求和，常用界值 >10 提示白天过度嗜睡。它评估主观嗜睡倾向，不是睡眠呼吸暂停的诊断，也不能替代多导睡眠监测。",
        noteEn = "Eight items scored 0-3 each; above 10 usually suggests excessive daytime sleepiness. It measures subjective sleepiness, not sleep apnoea, and does not replace polysomnography."
    )

    val ad8 = ScaleDef(
        type = "AD8",
        name = "AD8 认知功能筛查",
        nameEn = "AD8 Dementia Screening Interview",
        shortName = "AD8",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "记忆门诊、神经内科与社区老年健康筛查常用的 8 题问卷，比较「与过去相比」的认知与日常功能变化。",
        descriptionEn = "An 8-item informant questionnaire common in memory clinics, neurology and community screening, comparing current cognition and daily function with the past.",
        timeframe = "与几年前相比的变化",
        timeframeEn = "Change compared with a few years ago",
        instruction = "请由熟悉情况的人（家属或照护者）根据近几年的变化回答「是」或「否」。",
        instructionEn = "Ideally answered by someone who knows you well (family or carer), based on changes over the past few years.",
        questions = listOf(
            q("判断力出现问题，例如做决定、处理财务或购物时有困难", "Problems with judgement, e.g. decisions, finances or shopping", gdsYes),
            q("对业余爱好或日常活动的兴趣减退", "Less interest in hobbies or activities", gdsYes),
            q("反复重复同一个问题、故事或陈述", "Repeats questions, stories or statements", gdsYes),
            q("学习使用工具、电器或小器具（如遥控器、微波炉）有困难", "Trouble learning to use tools, appliances or gadgets", gdsYes),
            q("记错正确的年份或月份", "Forgets the correct month or year", gdsYes),
            q("处理复杂的财务事务（如收支平衡、缴费）有困难", "Trouble handling complicated financial affairs", gdsYes),
            q("记住约会或计划有困难", "Trouble remembering appointments or plans", gdsYes),
            q("日常思考或记忆方面持续存在问题", "Consistent problems with thinking or memory", gdsYes)
        ),
        bands = listOf(
            b(0, 1, "normal", "未见明显变化", "No significant change", "未达到常用的 2 分界值。若本人或家人仍感到担心，可记录具体例子带给医生。", "Below the usual cut-off of 2. If concerns remain, note concrete examples and bring them to a clinician."),
            b(2, 8, "screen_positive", "建议进一步评估", "Further assessment suggested", "达到常用的 2 分界值，建议到记忆门诊 / 神经内科做进一步评估；这不等于已经确诊痴呆。", "At or above the usual cut-off of 2. Consider a memory-clinic or neurology assessment; this is not a diagnosis of dementia.")
        ),
        estimatedMinutes = 2,
        featured = false,
        source = "Galvin JE 等 (2005). The AD8: a brief informant interview to detect dementia. Neurology 65(4): 559–564；国内应用见《中国痴呆与认知障碍诊治指南》与社区认知筛查相关文献。条目核对：https://pubmed.ncbi.nlm.nih.gov/?term=Galvin+2005+AD8+brief+informant+interview+dementia · https://www.alz.org/",
        sourceEn = "Galvin JE et al. (2005). The AD8: a brief informant interview to detect dementia. Neurology 65(4): 559-564; also used in Chinese dementia guidelines and community screening. Items: https://pubmed.ncbi.nlm.nih.gov/?term=Galvin+2005+AD8+brief+informant+interview+dementia · https://www.alz.org/",
        note = "AD8 原本由知情者（家属 / 照护者）填写，不是严格的自评量表；8 题各 0–1 分，≥2 分提示需要进一步评估。它只做筛查，不能诊断痴呆或判断病因，结果请交给医生解读。",
        noteEn = "AD8 is designed as an informant interview (family / carer), not a strict self-report. Eight items score 0-1 each; 2 or more suggests further assessment. It screens only: it cannot diagnose dementia or its cause. Ask a clinician to interpret the result."
    )

    val cage = ScaleDef(
        type = "CAGE",
        name = "CAGE 酒精使用筛查",
        nameEn = "CAGE Alcohol Screening Questionnaire",
        shortName = "CAGE",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "综合医院、体检与精神科常用的 4 题酒精使用问题筛查，名称来自 Cut down / Annoyed / Guilty / Eye-opener 四个问题。",
        descriptionEn = "A 4-item alcohol-problem screen common in general hospitals, health checks and psychiatry; the name comes from Cut down, Annoyed, Guilty and Eye-opener.",
        timeframe = "一生中 / 总体",
        timeframeEn = "Lifetime / in general",
        instruction = "请按自己的真实经历回答「是」或「否」。",
        instructionEn = "Answer yes or no based on your own experience.",
        questions = listOf(
            q("你是否曾觉得自己应该减少饮酒？", "Have you ever felt you should cut down on your drinking?", gdsYes),
            q("别人批评你喝酒时，你是否感到恼火？", "Have people annoyed you by criticising your drinking?", gdsYes),
            q("你是否曾对自己的饮酒感到内疚或后悔？", "Have you ever felt bad or guilty about your drinking?", gdsYes),
            q("你是否曾在早晨一起床就喝酒，以稳定情绪或缓解宿醉？", "Have you ever had a drink first thing in the morning to steady your nerves or get rid of a hangover?", gdsYes)
        ),
        bands = listOf(
            b(0, 1, "low", "未见明显提示", "No significant indication", "未达到常用的 2 分界值。CAGE 只反映酒精相关问题，不评估饮酒量；如需评估量，可用应用内的 AUDIT / AUDIT-C。", "Below the usual cut-off of 2. CAGE reflects alcohol-related problems, not quantity; use AUDIT / AUDIT-C in this app to assess consumption."),
            b(2, 4, "positive", "提示需进一步评估", "Further assessment suggested", "达到常用的 2 分界值，建议与医生讨论饮酒情况；戒断可能有风险，不要突然自行停酒。", "At or above the usual cut-off of 2. Discuss your drinking with a clinician; withdrawal can be risky, so do not stop abruptly on your own.")
        ),
        estimatedMinutes = 1,
        featured = false,
        source = "Ewing JA (1984). Detecting alcoholism: the CAGE questionnaire. JAMA 252(14): 1905–1907；公共领域，国内综合医院与精神科广泛使用。条目核对：https://en.wikipedia.org/wiki/CAGE_questionnaire · https://pubmed.ncbi.nlm.nih.gov/?term=Ewing+1984+Detecting+alcoholism+CAGE+questionnaire",
        sourceEn = "Ewing JA (1984). Detecting alcoholism: the CAGE questionnaire. JAMA 252(14): 1905-1907. Public domain and widely used in Chinese hospitals. Items: https://en.wikipedia.org/wiki/CAGE_questionnaire · https://pubmed.ncbi.nlm.nih.gov/?term=Ewing+1984+Detecting+alcoholism+CAGE+questionnaire",
        note = "4 题各 0–1 分，≥2 分提示需要进一步评估。CAGE 用于筛查酒精相关问题，不是酒精使用障碍的诊断，也不反映具体饮酒量；突然停酒可能有戒断风险，请咨询医生。",
        noteEn = "Four items scored 0-1 each; 2 or more suggests further assessment. CAGE screens for alcohol-related problems, not alcohol use disorder, and does not measure consumption. Sudden cessation can cause withdrawal; consult a clinician."
    )

    val ftnd = ScaleDef(
        type = "FTND",
        name = "尼古丁依赖检验量表（FTND）",
        nameEn = "Fagerstrom Test for Nicotine Dependence (FTND)",
        shortName = "FTND",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "国内戒烟门诊与呼吸科评估吸烟者尼古丁依赖程度的 6 题量表，常用于制定戒烟方案与随访。",
        descriptionEn = "A 6-item scale used in smoking-cessation clinics and respiratory medicine to grade nicotine dependence and guide follow-up.",
        timeframe = "目前",
        timeframeEn = "Currently",
        instruction = "仅适用于当前吸烟者，请按现在的吸烟习惯选择。",
        instructionEn = "For current smokers only; answer based on your present smoking habits.",
        questions = listOf(
            q("早晨醒来后多久吸第一支烟？", "How soon after waking do you smoke your first cigarette?", listOf(
                o("60 分钟以后", "After 60 minutes", 0),
                o("31–60 分钟", "31-60 minutes", 1),
                o("6–30 分钟", "6-30 minutes", 2),
                o("5 分钟以内", "Within 5 minutes", 3)
            )),
            q("在禁烟场所（如教堂、图书馆、影院）是否难以克制不吸烟？", "Is it difficult to refrain from smoking in places where it is forbidden?", gdsYes),
            q("一天中你最不愿放弃哪一支烟？", "Which cigarette would you hate most to give up?", listOf(
                o("其他任何一支", "Any other", 0),
                o("早晨第一支", "The first one in the morning", 1)
            )),
            q("你每天大约吸多少支烟？", "How many cigarettes do you smoke per day?", listOf(
                o("10 支或以下", "10 or fewer", 0),
                o("11–20 支", "11-20", 1),
                o("21–30 支", "21-30", 2),
                o("31 支或以上", "31 or more", 3)
            )),
            q("早晨醒来后的第一个小时，吸烟是否比其他时间更频繁？", "Do you smoke more frequently during the first hour after waking than during the rest of the day?", gdsYes),
            q("生病卧床时是否仍然吸烟？", "Do you smoke even if you are ill enough to stay in bed most of the day?", gdsYes)
        ),
        bands = listOf(
            b(0, 2, "low", "轻度依赖", "Low dependence", "依赖程度较低。若有戒烟打算，仍可与戒烟门诊讨论合适的方式与时间。", "Low dependence. If you plan to quit, a cessation clinic can still help with timing and method."),
            b(3, 5, "moderate", "中度依赖", "Moderate dependence", "存在中等程度的依赖，戒烟时容易出现渴求与戒断不适，建议到戒烟门诊随访。", "Moderate dependence; cravings and withdrawal are likely when quitting. Consider follow-up at a cessation clinic."),
            b(6, 10, "high", "高度依赖", "High dependence", "依赖程度较高，建议由戒烟门诊 / 呼吸科评估后再制定戒烟方案，并留意戒断症状。", "High dependence. Ask a cessation clinic or respiratory specialist to plan quitting and watch for withdrawal symptoms.")
        ),
        estimatedMinutes = 2,
        featured = false,
        source = "Heatherton TF, Kozlowski LT, Frecker RC & Fagerstrom KO (1991). The Fagerstrom Test for Nicotine Dependence: a revision of the Fagerstrom Tolerance Questionnaire. British Journal of Addiction 86(9): 1119–1127；国内戒烟门诊通行中文译本见《中国临床戒烟指南》。条目与各题权重核对：https://en.wikipedia.org/wiki/Fagerstr%C3%B6m_Test_for_Nicotine_Dependence · https://pubmed.ncbi.nlm.nih.gov/?term=Heatherton+1991+Fagerstrom+Test+Nicotine+Dependence",
        sourceEn = "Heatherton TF, Kozlowski LT, Frecker RC & Fagerstrom KO (1991). The Fagerstrom Test for Nicotine Dependence: a revision of the Fagerstrom Tolerance Questionnaire. British Journal of Addiction 86(9): 1119-1127; Chinese versions follow the national clinical smoking-cessation guideline. Items and weights: https://en.wikipedia.org/wiki/Fagerstr%C3%B6m_Test_for_Nicotine_Dependence · https://pubmed.ncbi.nlm.nih.gov/?term=Heatherton+1991+Fagerstrom+Test+Nicotine+Dependence",
        note = "各题按原量表权重计分，总分 0–10，常用界值 ≥6 提示高度依赖。仅适用于当前吸烟者，评估依赖程度而非吸烟危害，也不是诊断工具。",
        noteEn = "Items keep the original weights, total 0-10; 6 or more usually indicates high dependence. For current smokers only: it grades dependence, not harm, and is not a diagnostic tool."
    )

    private val mspssOptions = listOf(
        o("非常不同意", "Very strongly disagree", 1),
        o("不同意", "Strongly disagree", 2),
        o("有点不同意", "Mildly disagree", 3),
        o("中立", "Neutral", 4),
        o("有点同意", "Mildly agree", 5),
        o("同意", "Strongly agree", 6),
        o("非常同意", "Very strongly agree", 7)
    )

    val mspss = ScaleDef(
        type = "MSPSS",
        name = "领悟社会支持量表（MSPSS）",
        nameEn = "Multidimensional Scale of Perceived Social Support (MSPSS)",
        shortName = "MSPSS",
        category = CATEGORY,
        categoryEn = CATEGORY_EN,
        description = "国内医院、慢病随访与心理健康研究常用的 12 题量表，评估来自家人、朋友与重要他人的主观支持感。",
        descriptionEn = "A 12-item scale widely used in Chinese hospitals, chronic-care follow-up and mental-health research, measuring perceived support from family, friends and a significant other.",
        timeframe = "目前 / 总体",
        timeframeEn = "These days / in general",
        instruction = "请按自己当下的真实感受，选择对每句话的同意程度。",
        instructionEn = "Based on how things feel right now, choose how much you agree with each statement.",
        questions = listOf(
            q("在我需要的时候，会有一位特别的人在我身边", "There is a special person who is around when I am in need", mspssOptions),
            q("我有一位可以分享快乐与忧伤的特别的人", "There is a special person with whom I can share my joys and sorrows", mspssOptions),
            q("我的家人真的尽力帮助我", "My family really tries to help me", mspssOptions),
            q("我能从家人那里得到所需的情感支持", "I get the emotional help and support I need from my family", mspssOptions),
            q("我有一位能真正安慰我的特别的人", "I have a special person who is a real source of comfort to me", mspssOptions),
            q("我的朋友真的尽力帮助我", "My friends really try to help me", mspssOptions),
            q("遇到不顺时我可以依靠朋友", "I can count on my friends when things go wrong", mspssOptions),
            q("我可以和家人谈论自己的问题", "I can talk about my problems with my family", mspssOptions),
            q("我有能分享快乐与忧伤的朋友", "I have friends with whom I can share my joys and sorrows", mspssOptions),
            q("我生命中有一位关心我感受的特别的人", "There is a special person in my life who cares about my feelings", mspssOptions),
            q("我的家人愿意帮我做决定", "My family is willing to help me make decisions", mspssOptions),
            q("我可以和朋友谈论自己的问题", "I can talk about my problems with my friends", mspssOptions)
        ),
        bands = listOf(
            b(12, 35, "low", "支持感较低", "Low perceived support", "主观感受到的支持偏少。这不代表没有人关心你，但值得和信任的人或咨询师聊聊。", "Perceived support is low. It does not mean nobody cares, but it is worth talking with someone you trust or a counsellor."),
            b(36, 60, "medium", "支持感中等", "Moderate perceived support", "主观支持感处于中间水平，可以留意在哪些关系里更愿意开口求助。", "Perceived support is moderate. Notice which relationships feel safest when you need help."),
            b(61, 84, "high", "支持感较高", "High perceived support", "目前主观上感到较被支持，可以继续把这些连接写进安全计划或日记里。", "You currently feel well supported. Consider recording these connections in your safety plan or journal.")
        ),
        estimatedMinutes = 3,
        featured = false,
        source = "Zimet GD, Dahlem NW, Zimet SG & Farley GK (1988). The Multidimensional Scale of Perceived Social Support. Journal of Personality Assessment 52(1): 30–41；国内通行中文译本（姜乾金等）见《心理卫生评定量表手册》，常用于医院与高校研究，量表可免费用于非商业研究。条目核对：https://pubmed.ncbi.nlm.nih.gov/?term=Zimet+1988+Multidimensional+Scale+of+Perceived+Social+Support",
        sourceEn = "Zimet GD, Dahlem NW, Zimet SG & Farley GK (1988). The Multidimensional Scale of Perceived Social Support. Journal of Personality Assessment 52(1): 30-41. A Chinese version (Jiang Qianjin et al.) appears in the Chinese Mental Health Rating Scale Manual; the scale is free for non-commercial research. Items: https://pubmed.ncbi.nlm.nih.gov/?term=Zimet+1988+Multidimensional+Scale+of+Perceived+Social+Support",
        note = "12 题各 1–7 分求和（12–84），常按平均分 <3 为低、3–5 为中、>5 为高支持来解读。它测的是「主观感受到的支持」，不等于客观支持网络，也不是诊断工具。",
        noteEn = "Twelve items scored 1-7 each (12-84); the usual reading is mean <3 low, 3-5 moderate, >5 high. It measures perceived support, not the objective support network, and is not a diagnostic tool."
    )

    val all: List<ScaleDef> = listOf(
        zungSas,
        zungSds,
        hads,
        gds15,
        ais,
        ess,
        ad8,
        cage,
        ftnd,
        mspss
    )
}
