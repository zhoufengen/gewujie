package com.gewujie.zibian.service;

import com.gewujie.zibian.model.Lesson;
import com.gewujie.zibian.model.LessonStyle;
import com.gewujie.zibian.repository.LessonRepository;
import com.gewujie.zibian.repository.LessonStyleRepository;
import com.gewujie.zibian.model.LearningRecord;
import com.gewujie.zibian.repository.LearningRecordRepository;
import com.gewujie.zibian.entity.SignInRecord;
import com.gewujie.zibian.repository.SignInRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class LearningService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonStyleRepository lessonStyleRepository;
    
    @Autowired
    private DictionaryService dictionaryService;
    
    @Autowired
    private LearningRecordRepository learningRecordRepository;
    
    @Autowired
    private SignInRecordRepository signInRecordRepository;
    
    // Basic mapping of characters to pinyin and definition for MVP
    private static final Map<String, String[]> CHARACTER_DATA = new HashMap<>();
    static {
        // Set default values for characters not found in mapping or API
        CHARACTER_DATA.put("default", new String[]{"", "", ""});
        // Common characters with pinyin and basic definition
        CHARACTER_DATA.put("一", new String[]{"yī", "one", "一个,一天,一年"});
        CHARACTER_DATA.put("二", new String[]{"èr", "two", "二月,二手,二十"});
        CHARACTER_DATA.put("三", new String[]{"sān", "three", "三个,三天,三年"});
        CHARACTER_DATA.put("四", new String[]{"sì", "four", "四个,四天,四年"});
        CHARACTER_DATA.put("五", new String[]{"wǔ", "five", "五个,五天,五年"});
        CHARACTER_DATA.put("六", new String[]{"liù", "six", "六个,六天,六年"});
        CHARACTER_DATA.put("七", new String[]{"qī", "seven", "七个,七天,七年"});
        CHARACTER_DATA.put("八", new String[]{"bā", "eight", "八个,八天,八年"});
        CHARACTER_DATA.put("九", new String[]{"jiǔ", "nine", "九个,九天,九年"});
        CHARACTER_DATA.put("十", new String[]{"shí", "ten", "十个,十天,十年"});
        CHARACTER_DATA.put("人", new String[]{"rén", "person", "人们,人类,人生"});
        CHARACTER_DATA.put("大", new String[]{"dà", "big", "大人,大小,大家"});
        CHARACTER_DATA.put("小", new String[]{"xiǎo", "small", "小孩,大小,小学"});
        CHARACTER_DATA.put("儿", new String[]{"ér", "child", "儿子,女儿,儿童"});
        CHARACTER_DATA.put("女", new String[]{"nǚ", "woman", "女人,女儿,女生"});
        CHARACTER_DATA.put("爸", new String[]{"bà", "father", "爸爸,老爸,阿爸"});
        CHARACTER_DATA.put("妈", new String[]{"mā", "mother", "妈妈,老妈,阿妈"});
        CHARACTER_DATA.put("爷", new String[]{"yé", "grandfather", "爷爷,老爷,太爷"});
        CHARACTER_DATA.put("奶", new String[]{"nǎi", "grandmother", "奶奶,牛奶,奶妈"});
        CHARACTER_DATA.put("哥", new String[]{"gē", "brother", "哥哥,大哥,表哥"});
        CHARACTER_DATA.put("姐", new String[]{"jiě", "sister", "姐姐,大姐,表姐"});
        CHARACTER_DATA.put("弟", new String[]{"dì", "younger brother", "弟弟,小弟,堂弟"});
        CHARACTER_DATA.put("妹", new String[]{"mèi", "younger sister", "妹妹,小妹,表妹"});
        CHARACTER_DATA.put("头", new String[]{"tóu", "head", "头发,头脑,头顶"});
        CHARACTER_DATA.put("眼", new String[]{"yǎn", "eye", "眼睛,眼神,眼光"});
        CHARACTER_DATA.put("耳", new String[]{"ěr", "ear", "耳朵,耳鸣,耳闻"});
        CHARACTER_DATA.put("口", new String[]{"kǒu", "mouth", "口腔,口才,口味"});
        CHARACTER_DATA.put("手", new String[]{"shǒu", "hand", "手心,手背,手指"});
        CHARACTER_DATA.put("脚", new String[]{"jiǎo", "foot", "脚步,脚印,脚趾"});
        CHARACTER_DATA.put("心", new String[]{"xīn", "heart", "心情,心脏,心理"});
        CHARACTER_DATA.put("天", new String[]{"tiān", "sky", "天空,天气,天堂"});
        CHARACTER_DATA.put("地", new String[]{"dì", "earth", "地面,地球,地理"});
        CHARACTER_DATA.put("日", new String[]{"rì", "sun", "日光,日期,日子"});
        CHARACTER_DATA.put("月", new String[]{"yuè", "moon", "月光,月份,月亮"});
        CHARACTER_DATA.put("星", new String[]{"xīng", "star", "星星,星空,星座"});
        CHARACTER_DATA.put("云", new String[]{"yún", "cloud", "云朵,云彩,云霄"});
        CHARACTER_DATA.put("风", new String[]{"fēng", "wind", "风力,风景,风格"});
        CHARACTER_DATA.put("雨", new String[]{"yǔ", "rain", "雨水,雨声,雨季"});
        CHARACTER_DATA.put("水", new String[]{"shuǐ", "water", "水果,水平,水分"});
        CHARACTER_DATA.put("火", new String[]{"huǒ", "fire", "火焰,火星,火药"});
        CHARACTER_DATA.put("山", new String[]{"shān", "mountain", "山峰,山脉,山谷"});
        CHARACTER_DATA.put("石", new String[]{"shí", "stone", "石头,石化,石材"});
        CHARACTER_DATA.put("花", new String[]{"huā", "flower", "花朵,花园,花瓣"});
        CHARACTER_DATA.put("草", new String[]{"cǎo", "grass", "草地,草丛,草叶"});
        CHARACTER_DATA.put("树", new String[]{"shù", "tree", "树木,树林,树叶"});
        CHARACTER_DATA.put("木", new String[]{"mù", "wood", "木材,木板,木匠"});
        CHARACTER_DATA.put("叶", new String[]{"yè", "leaf", "叶子,叶脉,叶片"});
        CHARACTER_DATA.put("鸟", new String[]{"niǎo", "bird", "鸟类,鸟语,鸟巢"});
        CHARACTER_DATA.put("鱼", new String[]{"yú", "fish", "鱼类,鱼缸,鱼肉"});
        CHARACTER_DATA.put("虫", new String[]{"chóng", "insect", "虫子,虫类,虫害"});
        CHARACTER_DATA.put("牛", new String[]{"niú", "cow", "牛奶,牛肉,牛仔"});
        CHARACTER_DATA.put("羊", new String[]{"yáng", "sheep", "羊肉,羊毛,羊群"});
        CHARACTER_DATA.put("马", new String[]{"mǎ", "horse", "马匹,马车,马术"});
        CHARACTER_DATA.put("狗", new String[]{"gǒu", "dog", "小狗,狼狗,猎狗"});
        CHARACTER_DATA.put("猫", new String[]{"māo", "cat", "小猫,猫咪,猫眼"});
        CHARACTER_DATA.put("红", new String[]{"hóng", "red", "红色,红旗,红花"});
        CHARACTER_DATA.put("黄", new String[]{"huáng", "yellow", "黄色,黄河,黄金"});
        CHARACTER_DATA.put("蓝", new String[]{"lán", "blue", "蓝色,蓝天,蓝图"});
        CHARACTER_DATA.put("绿", new String[]{"lǜ", "green", "绿色,绿化,绿叶"});
        CHARACTER_DATA.put("白", new String[]{"bái", "white", "白色,白天,白云"});
        CHARACTER_DATA.put("黑", new String[]{"hēi", "black", "黑色,黑夜,黑板"});
        CHARACTER_DATA.put("方", new String[]{"fāng", "square", "方向,方法,方面"});
        CHARACTER_DATA.put("圆", new String[]{"yuán", "circle", "圆形,圆圈,圆满"});
        CHARACTER_DATA.put("长", new String[]{"cháng", "long", "长度,长期,长久"});
        CHARACTER_DATA.put("高", new String[]{"gāo", "tall", "高度,高楼,高温"});
        CHARACTER_DATA.put("走", new String[]{"zǒu", "walk", "走路,走步,走亲"});
        CHARACTER_DATA.put("跑", new String[]{"pǎo", "run", "跑步,跑道,跑鞋"});
        CHARACTER_DATA.put("跳", new String[]{"tiào", "jump", "跳跃,跳绳,跳高"});
        CHARACTER_DATA.put("飞", new String[]{"fēi", "fly", "飞机,飞行,飞鸟"});
        CHARACTER_DATA.put("吃", new String[]{"chī", "eat", "吃饭,吃药,吃穿"});
        CHARACTER_DATA.put("喝", new String[]{"hē", "drink", "喝水,喝酒,喝茶"});
        CHARACTER_DATA.put("看", new String[]{"kàn", "see", "看见,看书,看法"});
        CHARACTER_DATA.put("听", new String[]{"tīng", "listen", "听见,听说,听力"});
        CHARACTER_DATA.put("说", new String[]{"shuō", "speak", "说话,说明,说法"});
        CHARACTER_DATA.put("唱", new String[]{"chàng", "sing", "唱歌,唱戏,唱词"});
        CHARACTER_DATA.put("读", new String[]{"dú", "read", "读书,阅读,读音"});
        CHARACTER_DATA.put("写", new String[]{"xiě", "write", "写字,写作,写法"});
        CHARACTER_DATA.put("画", new String[]{"huà", "draw", "画画,绘画,画面"});
        CHARACTER_DATA.put("玩", new String[]{"wán", "play", "玩耍,玩具,玩笑"});
        CHARACTER_DATA.put("来", new String[]{"lái", "come", "来到,来自,来往"});
        CHARACTER_DATA.put("去", new String[]{"qù", "go", "去向,去处,去掉"});
        CHARACTER_DATA.put("上", new String[]{"shàng", "up", "上学,上班,上升"});
        CHARACTER_DATA.put("下", new String[]{"xià", "down", "下班,下雨,下降"});
        CHARACTER_DATA.put("出", new String[]{"chū", "out", "出门,出发,出现"});
        CHARACTER_DATA.put("入", new String[]{"rù", "in", "入口,入学,进入"});
        CHARACTER_DATA.put("笑", new String[]{"xiào", "laugh", "笑容,笑话,笑柄"});
        CHARACTER_DATA.put("哭", new String[]{"kū", "cry", "哭泣,哭声,哭诉"});
        CHARACTER_DATA.put("叫", new String[]{"jiào", "call", "叫声,叫喊,叫好"});
        CHARACTER_DATA.put("想", new String[]{"xiǎng", "think", "想念,想法,想象"});
        CHARACTER_DATA.put("爱", new String[]{"ài", "love", "爱情,爱心,爱护"});
        CHARACTER_DATA.put("衣", new String[]{"yī", "clothes", "衣服,衣物,衣着"});
        CHARACTER_DATA.put("帽", new String[]{"mào", "hat", "帽子,帽檐,帽徽"});
        CHARACTER_DATA.put("鞋", new String[]{"xié", "shoe", "鞋子,鞋底,鞋跟"});
        CHARACTER_DATA.put("床", new String[]{"chuáng", "bed", "床铺,床单,床垫"});
        CHARACTER_DATA.put("桌", new String[]{"zhuō", "table", "桌子,桌面,桌椅"});
        CHARACTER_DATA.put("椅", new String[]{"yǐ", "chair", "椅子,椅背,椅垫"});
        CHARACTER_DATA.put("碗", new String[]{"wǎn", "bowl", "饭碗,碗碟,碗柜"});
        CHARACTER_DATA.put("杯", new String[]{"bēi", "cup", "杯子,茶杯,酒杯"});
        CHARACTER_DATA.put("灯", new String[]{"dēng", "light", "灯光,灯具,灯火"});
        CHARACTER_DATA.put("车", new String[]{"chē", "car", "车辆,汽车,火车"});
        CHARACTER_DATA.put("船", new String[]{"chuán", "boat", "船只,轮船,帆船"});
        CHARACTER_DATA.put("书", new String[]{"shū", "book", "书本,书包,书桌"});
        CHARACTER_DATA.put("笔", new String[]{"bǐ", "pen", "铅笔,钢笔,毛笔"});
        CHARACTER_DATA.put("纸", new String[]{"zhǐ", "paper", "纸张,报纸,纸巾"});
        CHARACTER_DATA.put("刀", new String[]{"dāo", "knife", "刀子,菜刀,剪刀"});
        CHARACTER_DATA.put("饭", new String[]{"fàn", "rice", "吃饭,米饭,饭盒"});
        CHARACTER_DATA.put("面", new String[]{"miàn", "noodle", "面条,面包,面粉"});
        CHARACTER_DATA.put("米", new String[]{"mǐ", "rice", "大米,小米,米粉"});
        CHARACTER_DATA.put("菜", new String[]{"cài", "vegetable", "蔬菜,菜品,菜单"});
        CHARACTER_DATA.put("肉", new String[]{"ròu", "meat", "肉类,牛肉,猪肉"});
        CHARACTER_DATA.put("蛋", new String[]{"dàn", "egg", "鸡蛋,蛋黄,蛋白"});
        CHARACTER_DATA.put("奶", new String[]{"nǎi", "milk", "牛奶,奶粉,奶油"});
        CHARACTER_DATA.put("果", new String[]{"guǒ", "fruit", "水果,果实,果汁"});
        CHARACTER_DATA.put("糖", new String[]{"táng", "sugar", "糖果,白糖,红糖"});
        CHARACTER_DATA.put("茶", new String[]{"chá", "tea", "茶水,茶叶,茶杯"});
        CHARACTER_DATA.put("今", new String[]{"jīn", "today", "今天,今年,今日"});
        CHARACTER_DATA.put("明", new String[]{"míng", "tomorrow", "明天,明年,明日"});
        CHARACTER_DATA.put("早", new String[]{"zǎo", "morning", "早上,早安,早点"});
        CHARACTER_DATA.put("晚", new String[]{"wǎn", "evening", "晚上,晚安,晚餐"});
        CHARACTER_DATA.put("时", new String[]{"shí", "time", "时间,时候,时刻"});
        CHARACTER_DATA.put("年", new String[]{"nián", "year", "年份,年龄,年会"});
        CHARACTER_DATA.put("春", new String[]{"chūn", "spring", "春天,春季,春风"});
        CHARACTER_DATA.put("夏", new String[]{"xià", "summer", "夏天,夏季,夏日"});
        CHARACTER_DATA.put("秋", new String[]{"qiū", "autumn", "秋天,秋季,秋风"});
        CHARACTER_DATA.put("冬", new String[]{"dōng", "winter", "冬天,冬季,冬日"});
        CHARACTER_DATA.put("东", new String[]{"dōng", "east", "东方,东边,东风"});
        CHARACTER_DATA.put("西", new String[]{"xī", "west", "西方,西边,西风"});
        CHARACTER_DATA.put("南", new String[]{"nán", "south", "南方,南边,南风"});
        CHARACTER_DATA.put("北", new String[]{"běi", "north", "北方,北边,北风"});
        CHARACTER_DATA.put("左", new String[]{"zuǒ", "left", "左边,左手,左眼"});
        CHARACTER_DATA.put("右", new String[]{"yòu", "right", "右边,右手,右眼"});
        CHARACTER_DATA.put("好", new String[]{"hǎo", "good", "好人,好事,好景"});
        CHARACTER_DATA.put("坏", new String[]{"huài", "bad", "坏人,坏事,坏处"});
        CHARACTER_DATA.put("美", new String[]{"měi", "beautiful", "美丽,美好,美满"});
        CHARACTER_DATA.put("香", new String[]{"xiāng", "fragrant", "香气,香味,香水"});
        CHARACTER_DATA.put("甜", new String[]{"tián", "sweet", "甜蜜,甜美,甜点"});
        CHARACTER_DATA.put("苦", new String[]{"kǔ", "bitter", "苦味,苦难,辛苦"});
        CHARACTER_DATA.put("冷", new String[]{"lěng", "cold", "寒冷,冷冻,冷笑"});
        CHARACTER_DATA.put("热", new String[]{"rè", "hot", "炎热,热情,热闹"});
        CHARACTER_DATA.put("干", new String[]{"gān", "dry", "干燥,干净,干旱"});
        CHARACTER_DATA.put("湿", new String[]{"shī", "wet", "潮湿,湿润,湿气"});
        CHARACTER_DATA.put("软", new String[]{"ruǎn", "soft", "柔软,软弱,软件"});
        CHARACTER_DATA.put("硬", new String[]{"yìng", "hard", "坚硬,强硬,硬件"});
        CHARACTER_DATA.put("快", new String[]{"kuài", "fast", "快速,快乐,快捷"});
        CHARACTER_DATA.put("慢", new String[]{"màn", "slow", "缓慢,慢慢,慢条斯理"});
        CHARACTER_DATA.put("多", new String[]{"duō", "many", "多少,多余,多数"});
        CHARACTER_DATA.put("少", new String[]{"shǎo", "few", "少数,少量,缺少"});
        CHARACTER_DATA.put("有", new String[]{"yǒu", "have", "拥有,有关,有事"});
        CHARACTER_DATA.put("无", new String[]{"wú", "without", "无法,无论,无条件"});
        CHARACTER_DATA.put("是", new String[]{"shì", "yes", "是的,是否,是非"});
        CHARACTER_DATA.put("不", new String[]{"bù", "no", "不是,不要,不会"});
        CHARACTER_DATA.put("也", new String[]{"yě", "also", "也是,也许,也好"});
        CHARACTER_DATA.put("和", new String[]{"hé", "and", "和谐,和平,和睦"});
        CHARACTER_DATA.put("了", new String[]{"le", "particle", "好了,走了,吃了"});
        CHARACTER_DATA.put("在", new String[]{"zài", "at", "在这,在家,在吗"});
        CHARACTER_DATA.put("我", new String[]{"wǒ", "I", "我的,我们,我家"});
        CHARACTER_DATA.put("你", new String[]{"nǐ", "you", "你的,你们,你家"});
        CHARACTER_DATA.put("他", new String[]{"tā", "he", "他的,他们,他家"});
        CHARACTER_DATA.put("这", new String[]{"zhè", "this", "这个,这里,这样"});
        CHARACTER_DATA.put("那", new String[]{"nà", "that", "那个,那里,那样"});
        CHARACTER_DATA.put("谁", new String[]{"shuí", "who", "谁的,谁来,谁去"});
        CHARACTER_DATA.put("什", new String[]{"shén", "what", "什么,什锦,什物"});
        CHARACTER_DATA.put("么", new String[]{"me", "particle", "什么,这么,那么"});
        CHARACTER_DATA.put("的", new String[]{"de", "particle", "你的,我的,他的"});
        CHARACTER_DATA.put("地", new String[]{"de", "particle", "高兴地,快乐地"});
        CHARACTER_DATA.put("得", new String[]{"de", "particle", "跑得快,飞得高"});
        CHARACTER_DATA.put("着", new String[]{"zhe", "particle", "走着,看着,听着"});
        CHARACTER_DATA.put("过", new String[]{"guò", "particle", "去过,吃过,看过"});
        CHARACTER_DATA.put("就", new String[]{"jiù", "just", "就是,就来,就去"});
        CHARACTER_DATA.put("都", new String[]{"dōu", "all", "都是,都有,都在"});
        CHARACTER_DATA.put("把", new String[]{"bǎ", "particle", "把书,把钱,把门"});
        CHARACTER_DATA.put("被", new String[]{"bèi", "by", "被打,被杀,被骂"});
        CHARACTER_DATA.put("给", new String[]{"gěi", "give", "给我,给他,给你"});
        CHARACTER_DATA.put("对", new String[]{"duì", "correct", "对错,对象,对待"});
        CHARACTER_DATA.put("家", new String[]{"jiā", "home", "家庭,家人,家乡"});
        CHARACTER_DATA.put("门", new String[]{"mén", "door", "门口,门窗,门禁"});
        CHARACTER_DATA.put("足", new String[]{"zú", "foot", "足球,足够,足迹"});
        CHARACTER_DATA.put("牙", new String[]{"yá", "tooth", "牙齿,牙膏,牙刷"});
        CHARACTER_DATA.put("脸", new String[]{"liǎn", "face", "脸面,脸色,脸型"});
        CHARACTER_DATA.put("发", new String[]{"fà", "hair", "头发,发型,发质"});
        CHARACTER_DATA.put("身", new String[]{"shēn", "body", "身体,身材,身高"});
        CHARACTER_DATA.put("体", new String[]{"tǐ", "body", "体育,体检,体会"});
        CHARACTER_DATA.put("宝", new String[]{"bǎo", "treasure", "宝贝,宝石,宝藏"});
        CHARACTER_DATA.put("娃", new String[]{"wá", "baby", "娃娃,女娃,男娃"});
        CHARACTER_DATA.put("生", new String[]{"shēng", "born", "生日,生命,生活"});
        CHARACTER_DATA.put("男", new String[]{"nán", "male", "男人,男孩,男生"});
        CHARACTER_DATA.put("她", new String[]{"tā", "she", "她的,她们,她家"});
        CHARACTER_DATA.put("它", new String[]{"tā", "it", "它的,它们"});
        CHARACTER_DATA.put("睛", new String[]{"jīng", "eye", "眼睛,点睛之笔"});
        CHARACTER_DATA.put("鼻", new String[]{"bí", "nose", "鼻子,鼻音,鼻祖"});
        CHARACTER_DATA.put("嘴", new String[]{"zuǐ", "mouth", "嘴巴,嘴角,嘴唇"});
        CHARACTER_DATA.put("舌", new String[]{"shé", "tongue", "舌头,舌苔,舌尖"});
        CHARACTER_DATA.put("指", new String[]{"zhǐ", "finger", "手指,指甲,指点"});
        CHARACTER_DATA.put("腿", new String[]{"tuǐ", "leg", "大腿,小腿,腿部"});
        CHARACTER_DATA.put("肚", new String[]{"dù", "belly", "肚子,肚皮,肚脐"});
        CHARACTER_DATA.put("背", new String[]{"bèi", "back", "背部,背后,背包"});
        CHARACTER_DATA.put("雪", new String[]{"xuě", "snow", "雪花,雪人,雪景"});
        CHARACTER_DATA.put("雷", new String[]{"léi", "thunder", "雷声,雷电,雷雨"});
        CHARACTER_DATA.put("电", new String[]{"diàn", "electricity", "电器,电力,电话"});
        CHARACTER_DATA.put("光", new String[]{"guāng", "light", "光明,光线,光芒"});
        CHARACTER_DATA.put("河", new String[]{"hé", "river", "河流,河水,河岸"});
        CHARACTER_DATA.put("江", new String[]{"jiāng", "river", "长江,江水,江岸"});
        CHARACTER_DATA.put("海", new String[]{"hǎi", "sea", "海洋,海水,海边"});
        CHARACTER_DATA.put("枝", new String[]{"zhī", "branch", "树枝,枝条,枝桠"});
        CHARACTER_DATA.put("瓜", new String[]{"guā", "melon", "西瓜,南瓜,黄瓜"});
        CHARACTER_DATA.put("桃", new String[]{"táo", "peach", "桃子,桃花,桃树"});
        CHARACTER_DATA.put("鸡", new String[]{"jī", "chicken", "鸡肉,鸡蛋,鸡腿"});
        CHARACTER_DATA.put("鸭", new String[]{"yā", "duck", "鸭肉,鸭蛋,鸭腿"});
        CHARACTER_DATA.put("鹅", new String[]{"é", "goose", "鹅肉,鹅蛋,白鹅"});
        CHARACTER_DATA.put("虾", new String[]{"xiā", "shrimp", "虾仁,虾皮,龙虾"});
        CHARACTER_DATA.put("兔", new String[]{"tù", "rabbit", "兔子,兔肉,兔毛"});
        CHARACTER_DATA.put("鼠", new String[]{"shǔ", "mouse", "老鼠,鼠年,鼠疫"});
        CHARACTER_DATA.put("虎", new String[]{"hǔ", "tiger", "老虎,虎年,虎口"});
        CHARACTER_DATA.put("象", new String[]{"xiàng", "elephant", "大象,象牙,象鼻"});
        CHARACTER_DATA.put("熊", new String[]{"xióng", "bear", "熊猫,熊胆,熊市"});
        CHARACTER_DATA.put("猴", new String[]{"hóu", "monkey", "猴子,猴年,猴王"});
        CHARACTER_DATA.put("燕", new String[]{"yàn", "swallow", "燕子,燕窝,燕麦"});
        CHARACTER_DATA.put("灰", new String[]{"huī", "gray", "灰色,灰尘,灰心"});
        CHARACTER_DATA.put("紫", new String[]{"zǐ", "purple", "紫色,紫外线,紫气"});
        CHARACTER_DATA.put("短", new String[]{"duǎn", "short", "长短,短期,短处"});
        CHARACTER_DATA.put("低", new String[]{"dī", "low", "高低,低头,低调"});
        CHARACTER_DATA.put("粗", new String[]{"cū", "thick", "粗细,粗心,粗糙"});
        CHARACTER_DATA.put("细", new String[]{"xì", "thin", "细心,细致,细节"});
        CHARACTER_DATA.put("蹦", new String[]{"bèng", "jump", "蹦跳,蹦跶,蹦极"});
        CHARACTER_DATA.put("爬", new String[]{"pá", "climb", "爬行,爬山,爬树"});
        CHARACTER_DATA.put("游", new String[]{"yóu", "swim", "游泳,游戏,游玩"});
        CHARACTER_DATA.put("站", new String[]{"zhàn", "stand", "站立,站岗,站台"});
        CHARACTER_DATA.put("坐", new String[]{"zuò", "sit", "坐下,座位,坐席"});
        CHARACTER_DATA.put("睡", new String[]{"shuì", "sleep", "睡觉,睡眠,睡梦"});
        CHARACTER_DATA.put("醒", new String[]{"xǐng", "wake", "醒来,觉醒,醒目"});
        CHARACTER_DATA.put("咬", new String[]{"yǎo", "bite", "咬一口,咬伤,咬字"});
        CHARACTER_DATA.put("吹", new String[]{"chuī", "blow", "吹风,吹牛,吹气"});
        CHARACTER_DATA.put("问", new String[]{"wèn", "ask", "问题,问号,问候"});
        CHARACTER_DATA.put("答", new String[]{"dá", "answer", "回答,答案,答辩"});
        CHARACTER_DATA.put("拿", new String[]{"ná", "take", "拿起,拿走,拿手"});
        CHARACTER_DATA.put("放", new String[]{"fàng", "put", "放下,放手,放松"});
        CHARACTER_DATA.put("开", new String[]{"kāi", "open", "开门,开心,开放"});
        CHARACTER_DATA.put("关", new String[]{"guān", "close", "关门,关心,关注"});
        CHARACTER_DATA.put("回", new String[]{"huí", "return", "回来,回家,回忆"});
        CHARACTER_DATA.put("进", new String[]{"jìn", "enter", "进入,进去,进步"});
        CHARACTER_DATA.put("到", new String[]{"dào", "arrive", "到达,到来,到底"});
        CHARACTER_DATA.put("打", new String[]{"dǎ", "hit", "打球,打架,打击"});
        CHARACTER_DATA.put("拍", new String[]{"pāi", "pat", "拍打,拍照,拍子"});
        CHARACTER_DATA.put("洗", new String[]{"xǐ", "wash", "洗脸,洗手,洗澡"});
        CHARACTER_DATA.put("穿", new String[]{"chuān", "wear", "穿衣,穿鞋,穿戴"});
        CHARACTER_DATA.put("脱", new String[]{"tuō", "take off", "脱衣,脱鞋,脱帽"});
        CHARACTER_DATA.put("喜", new String[]{"xǐ", "happy", "喜欢,喜悦,喜庆"});
        CHARACTER_DATA.put("怒", new String[]{"nù", "angry", "愤怒,怒火,怒视"});
        CHARACTER_DATA.put("服", new String[]{"fú", "clothes", "衣服,服装,服从"});
        CHARACTER_DATA.put("袜", new String[]{"wà", "sock", "袜子,袜筒,袜底"});
        CHARACTER_DATA.put("裤", new String[]{"kù", "pants", "裤子,裤腿,裤腰"});
        CHARACTER_DATA.put("被", new String[]{"bèi", "quilt", "被子,被单,被套"});
        CHARACTER_DATA.put("柜", new String[]{"guì", "cabinet", "柜子,衣柜,书柜"});
        CHARACTER_DATA.put("盘", new String[]{"pán", "plate", "盘子,光盘,算盘"});
        CHARACTER_DATA.put("瓶", new String[]{"píng", "bottle", "瓶子,瓶装,瓶颈"});
        CHARACTER_DATA.put("壶", new String[]{"hú", "pot", "茶壶,水壶,壶嘴"});
        CHARACTER_DATA.put("扇", new String[]{"shàn", "fan", "扇子,风扇,扇风"});
        CHARACTER_DATA.put("钟", new String[]{"zhōng", "clock", "钟表,时钟,钟声"});
        CHARACTER_DATA.put("表", new String[]{"biǎo", "watch", "手表,钟表,表现"});
        CHARACTER_DATA.put("镜", new String[]{"jìng", "mirror", "镜子,眼镜,镜面"});
        CHARACTER_DATA.put("伞", new String[]{"sǎn", "umbrella", "雨伞,遮阳伞"});
        CHARACTER_DATA.put("包", new String[]{"bāo", "bag", "书包,背包,包裹"});
        CHARACTER_DATA.put("机", new String[]{"jī", "machine", "飞机,机器,机会"});
        CHARACTER_DATA.put("球", new String[]{"qiú", "ball", "足球,篮球,排球"});
        CHARACTER_DATA.put("本", new String[]{"běn", "book", "书本,本子,本来"});
        CHARACTER_DATA.put("剪", new String[]{"jiǎn", "scissors", "剪刀,剪子,剪纸"});
        CHARACTER_DATA.put("针", new String[]{"zhēn", "needle", "针线,针对,指针"});
        CHARACTER_DATA.put("线", new String[]{"xiàn", "thread", "线条,线段,线索"});
        CHARACTER_DATA.put("琴", new String[]{"qín", "piano", "钢琴,琴声,琴弦"});
        CHARACTER_DATA.put("棋", new String[]{"qí", "chess", "下棋,棋子,棋盘"});
        CHARACTER_DATA.put("歌", new String[]{"gē", "song", "歌曲,歌声,歌手"});
        CHARACTER_DATA.put("舞", new String[]{"wǔ", "dance", "舞蹈,跳舞,舞台"});
        CHARACTER_DATA.put("戏", new String[]{"xì", "play", "戏剧,戏曲,戏法"});
        CHARACTER_DATA.put("粥", new String[]{"zhōu", "porridge", "粥品,米粥,稀饭"});
        CHARACTER_DATA.put("汤", new String[]{"tāng", "soup", "汤品,汤汁,汤药"});
        CHARACTER_DATA.put("盐", new String[]{"yán", "salt", "食盐,盐巴,盐水"});
        CHARACTER_DATA.put("油", new String[]{"yóu", "oil", "油脂,油画,油条"});
        CHARACTER_DATA.put("酒", new String[]{"jiǔ", "wine", "酒精,酒杯,酒驾"});
        CHARACTER_DATA.put("饼", new String[]{"bǐng", "cake", "饼干,饼子,月饼"});
        CHARACTER_DATA.put("梨", new String[]{"lí", "pear", "梨子,梨花,梨树"});
        CHARACTER_DATA.put("苹", new String[]{"píng", "apple", "苹果,苹果树"});
        CHARACTER_DATA.put("蕉", new String[]{"jiāo", "banana", "香蕉,芭蕉,美人蕉"});
        CHARACTER_DATA.put("橘", new String[]{"jú", "orange", "橘子,柑橘,橘红"});
        CHARACTER_DATA.put("午", new String[]{"wǔ", "noon", "中午,午饭,午休"});
        CHARACTER_DATA.put("夜", new String[]{"yè", "night", "夜晚,夜宵,夜班"});
        CHARACTER_DATA.put("分", new String[]{"fēn", "minute", "分钟,分数,分开"});
        CHARACTER_DATA.put("秒", new String[]{"miǎo", "second", "秒钟,秒杀,秒表"});
        CHARACTER_DATA.put("周", new String[]{"zhōu", "week", "星期,周围,周期"});
        CHARACTER_DATA.put("季", new String[]{"jì", "season", "季节,季度,季风"});
        CHARACTER_DATA.put("里", new String[]{"lǐ", "inside", "里面,公里,里路"});
        CHARACTER_DATA.put("外", new String[]{"wài", "outside", "外面,外交,外语"});
        CHARACTER_DATA.put("边", new String[]{"biān", "side", "旁边,边缘,边境"});
        CHARACTER_DATA.put("旁", new String[]{"páng", "side", "旁边,旁观,旁人"});
        CHARACTER_DATA.put("前", new String[]{"qián", "front", "前面,前进,前方"});
        CHARACTER_DATA.put("后", new String[]{"hòu", "behind", "后面,后退,后方"});
        CHARACTER_DATA.put("丑", new String[]{"chǒu", "ugly", "丑陋,丑恶,丑角"});
        CHARACTER_DATA.put("臭", new String[]{"chòu", "smelly", "臭味,臭气,臭豆腐"});
        CHARACTER_DATA.put("酸", new String[]{"suān", "sour", "酸味,酸甜,酸奶"});
        CHARACTER_DATA.put("辣", new String[]{"là", "spicy", "辣味,辣椒,辣妹"});
        CHARACTER_DATA.put("温", new String[]{"wēn", "warm", "温暖,温柔,温和"});
        CHARACTER_DATA.put("凉", new String[]{"liáng", "cool", "凉爽,凉快,凉菜"});
        CHARACTER_DATA.put("重", new String[]{"zhòng", "heavy", "重量,重要,重点"});
        CHARACTER_DATA.put("新", new String[]{"xīn", "new", "新鲜,新年,新闻"});
        CHARACTER_DATA.put("旧", new String[]{"jiù", "old", "旧物,旧车,旧书"});
        CHARACTER_DATA.put("错", new String[]{"cuò", "wrong", "错误,错别,错过"});
        CHARACTER_DATA.put("真", new String[]{"zhēn", "true", "真实,真诚,真心"});
        CHARACTER_DATA.put("全", new String[]{"quán", "all", "全部,完全,全面"});
        CHARACTER_DATA.put("半", new String[]{"bàn", "half", "一半,半天,半年"});
        CHARACTER_DATA.put("空", new String[]{"kōng", "empty", "空气,天空,空格"});
        CHARACTER_DATA.put("满", new String[]{"mǎn", "full", "满意,满足,满分"});
        CHARACTER_DATA.put("能", new String[]{"néng", "can", "能力,能够,能量"});
        CHARACTER_DATA.put("会", new String[]{"huì", "will", "会议,会面,会计"});
        CHARACTER_DATA.put("要", new String[]{"yào", "want", "要求,要点,要点"});
        CHARACTER_DATA.put("该", new String[]{"gāi", "should", "应该,该当,该是"});
        CHARACTER_DATA.put("因", new String[]{"yīn", "because", "因为,原因,因素"});
        CHARACTER_DATA.put("果", new String[]{"guǒ", "fruit", "结果,果实,果断"});
        CHARACTER_DATA.put("同", new String[]{"tóng", "same", "同学,同事,同样"});
        CHARACTER_DATA.put("或", new String[]{"huò", "or", "或者,或许,或许"});
        CHARACTER_DATA.put("但", new String[]{"dàn", "but", "但是,但愿,但凡"});
        CHARACTER_DATA.put("只", new String[]{"zhǐ", "only", "只有,只要,只好"});
        CHARACTER_DATA.put("每", new String[]{"měi", "every", "每天,每年,每月"});
        CHARACTER_DATA.put("自", new String[]{"zì", "self", "自己,自由,自然"});
        CHARACTER_DATA.put("从", new String[]{"cóng", "from", "从来,从前,从此"});
        CHARACTER_DATA.put("向", new String[]{"xiàng", "to", "方向,向前,向上"});
        CHARACTER_DATA.put("路", new String[]{"lù", "road", "道路,路线,路人"});
        CHARACTER_DATA.put("安", new String[]{"ān", "safe", "安全,安心,安静"});
        CHARACTER_DATA.put("危", new String[]{"wēi", "dangerous", "危险,危机,危害"});
        CHARACTER_DATA.put("校", new String[]{"xiào", "school", "学校,校园,校友"});
        CHARACTER_DATA.put("师", new String[]{"shī", "teacher", "老师,师资,师徒"});
        CHARACTER_DATA.put("友", new String[]{"yǒu", "friend", "朋友,友谊,友好"});
        CHARACTER_DATA.put("学", new String[]{"xué", "learn", "学习,学校,学生"});
        CHARACTER_DATA.put("习", new String[]{"xí", "practice", "学习,练习,习惯"});
        CHARACTER_DATA.put("乐", new String[]{"lè", "happy", "快乐,乐园,乐观"});
        CHARACTER_DATA.put("园", new String[]{"yuán", "garden", "花园,公园,幼儿园"});
        CHARACTER_DATA.put("场", new String[]{"chǎng", "field", "场地,场所,场合"});
        CHARACTER_DATA.put("医", new String[]{"yī", "doctor", "医生,医院,医疗"});
        CHARACTER_DATA.put("院", new String[]{"yuàn", "institution", "医院,学院,研究院"});
        // Default entry for unknown characters
        CHARACTER_DATA.put("default", new String[]{"", "", ""});
    }

    public Lesson getLesson(Long id) {
        return lessonRepository.findById(id).orElse(null);
    }

    public Lesson getRandomLesson() {
        List<Lesson> all = lessonRepository.findAll();
        if (all.isEmpty())
            return null;
        return all.get(new Random().nextInt(all.size()));
    }

    public Lesson getRandomLessonByTextbookCategory(String textbookCategory, Long userId) {
        List<Lesson> lessons = lessonRepository.findByTextbookCategory(textbookCategory);
        if (lessons.isEmpty())
            return null;
        
        // If userId is provided, filter out lessons that the user has already learned
        if (userId != null) {
            // Get all lessons the user has already learned
            List<LearningRecord> learnedRecords = learningRecordRepository.findByUserId(userId);
            List<Long> learnedLessonIds = learnedRecords.stream()
                    .map(record -> record.getLesson().getId())
                    .toList();
            
            // Filter out learned lessons
            List<Lesson> availableLessons = lessons.stream()
                    .filter(lesson -> !learnedLessonIds.contains(lesson.getId()))
                    .collect(Collectors.toList());
            
            // If there are available lessons after filtering, return a random one
            if (!availableLessons.isEmpty()) {
                return availableLessons.get(new Random().nextInt(availableLessons.size()));
            }
            // Otherwise, return a random lesson from the original list (all lessons have been learned)
        }
        
        // Return a random lesson from the original list if no userId provided or all lessons learned
        return lessons.get(new Random().nextInt(lessons.size()));
    }

    @Transactional
    public Lesson createLesson(Lesson lesson) {
        Lesson saved = lessonRepository.save(lesson);
        if (lesson.getStyles() != null) {
            for (LessonStyle style : lesson.getStyles()) {
                style.setLesson(saved);
                lessonStyleRepository.save(style);
            }
        }
        return saved;
    }
    @Autowired
    private com.gewujie.zibian.repository.UserRepository userRepository;

    public void recordLearning(Long userId, Long lessonId) {
        // Create learning record with initial review date
        com.gewujie.zibian.model.LearningRecord record = new com.gewujie.zibian.model.LearningRecord();
        record.setUser(userRepository.findById(userId).orElse(null));
        record.setLesson(getLesson(lessonId));
        // Set initial review date: 1 day from now for first review
        record.setNextReviewDate(LocalDateTime.now().plusDays(1));
        learningRecordRepository.save(record);
    }

    public long getDailyNewWords(Long userId) {
        // Return count of distinct characters learned today
        java.time.LocalDateTime today = java.time.LocalDate.now().atStartOfDay();
        return learningRecordRepository.findDistinctCharactersByUserIdAndLearnedAtAfter(userId, today).size();
    }

    @Transactional
    public boolean isDataReady() {
        // Check if we have at least some data in the database
        // Return true if we have more than 1 lesson (the initial '永' character)
        return lessonRepository.count() > 1;
    }

    @Transactional
    public void deleteAllLessons() {
        lessonStyleRepository.deleteAll();
        lessonRepository.deleteAll();
    }

    @Transactional
    public Lesson findByCharacter(String character) {
        List<Lesson> lessons = lessonRepository.findAllByCharacter(character);
        if (lessons.isEmpty())
            return null;
        // Return first one; if there are duplicates, delete extras
        if (lessons.size() > 1) {
            System.out.println(
                    "Found " + lessons.size() + " duplicates for character: " + character + ", cleaning up...");
            for (int i = 1; i < lessons.size(); i++) {
                Lesson dup = lessons.get(i);
                // Delete by lesson ID to avoid lazy loading styles
                lessonStyleRepository.deleteByLessonId(dup.getId());
                lessonRepository.deleteById(dup.getId());
            }
        }
        return lessons.get(0);
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    @Transactional(readOnly = true)
    public List<String> getLearnedCharacters(Long userId) {
        return learningRecordRepository.findByUserId(userId).stream()
                .map(record -> record.getLesson().getCharacter())
                .distinct()
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLearningRecords(Long userId) {
        List<LearningRecord> records = learningRecordRepository.findByUserId(userId);
        return records.stream()
                .map(record -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("learnedAt", record.getLearnedAt());
                    data.put("character", record.getLesson().getCharacter());
                    data.put("lessonId", record.getLesson().getId());
                    return data;
                })
                .toList();
    }
    
    @Transactional(readOnly = true)
    public Map<String, String> getLearningDates(Long userId) {
        // Get all learning records (learned new words)
        List<LearningRecord> records = learningRecordRepository.findByUserId(userId);
        Map<String, String> dateStatus = new HashMap<>();
        
        // First, mark all days with new words as "learned"
        records.forEach(record -> {
            String dateKey = record.getLearnedAt().toLocalDate().toString();
            dateStatus.put(dateKey, "learned"); // 学习了新词
        });
        
        // Then, get all check-in records
        List<SignInRecord> signInRecords = signInRecordRepository.findByUserId(userId);
        
        // For days that were checked in but no new words were learned, mark as "checkedin"
        signInRecords.forEach(record -> {
            String dateKey = record.getSignInTime().toLocalDate().toString();
            // Only set to "checkedin" if not already "learned"
            dateStatus.putIfAbsent(dateKey, "checkedin");
        });
        
        return dateStatus;
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLearningTrend(Long userId) {
        // Get all learning records for the user
        List<LearningRecord> records = learningRecordRepository.findByUserId(userId);
        
        // Group by date and count the number of words learned each day
        Map<String, Long> dateCountMap = records.stream()
            .collect(Collectors.groupingBy(
                record -> record.getLearnedAt().toLocalDate().toString(),
                Collectors.counting()
            ));
        
        // Convert to list of maps with date and count
        return dateCountMap.entrySet().stream()
            .map(entry -> {
                Map<String, Object> data = new HashMap<>();
                data.put("date", entry.getKey());
                data.put("count", entry.getValue());
                return data;
            })
            .sorted(Comparator.comparing(map -> (String) map.get("date")))
            .toList();
    }

    public List<Lesson> getLessonsByTextbookCategory(String textbookCategory) {
        return lessonRepository.findByTextbookCategoryOrderByCharacter(textbookCategory);
    }
    
    @Transactional
    public boolean checkIn(Long userId) {
        // Check if already checked in today
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);
        
        boolean alreadyCheckedIn = signInRecordRepository
            .findByUserIdAndSignInTimeBetween(userId, startOfDay, endOfDay)
            .isPresent();
        
        if (!alreadyCheckedIn) {
            SignInRecord signInRecord = new SignInRecord();
            signInRecord.setUserId(userId);
            signInRecordRepository.save(signInRecord);
            return true;
        }
        return false;
    }
    
    @Transactional(readOnly = true)
    public boolean isCheckedInToday(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);
        
        return signInRecordRepository
            .findByUserIdAndSignInTimeBetween(userId, startOfDay, endOfDay)
            .isPresent();
    }

    @Transactional
    public void importCharactersFromFile(String filePath, String textbookCategory) throws IOException {
        File file = new File(filePath);
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        
        // Split characters by Chinese comma
        String[] characters = content.split("、");
        
        // Remove duplicates
        HashSet<String> uniqueChars = new HashSet<>();
        for (String charStr : characters) {
            charStr = charStr.trim();
            if (charStr.length() == 1) {
                uniqueChars.add(charStr);
            }
        }
        
        // Import each character
        for (String character : uniqueChars) {
            Lesson lesson;
            if (existsByCharacter(character)) {
                // Update existing lesson with character data
                lesson = findByCharacter(character);
                System.out.println("Updating character: " + character);
            } else {
                // Create new lesson
                lesson = new Lesson();
                lesson.setCharacter(character);
                
                // Add default styles for new lessons only
                List<LessonStyle> styles = new ArrayList<>();
                styles.add(createStyle("甲骨文", "https://via.placeholder.com/150?text=Oracle"));
                styles.add(createStyle("金文", "https://via.placeholder.com/150?text=Bronze"));
                styles.add(createStyle("小篆", "https://via.placeholder.com/150?text=SmallSeal"));
                styles.add(createStyle("隶书", "https://via.placeholder.com/150?text=Clerical"));
                styles.add(createStyle("楷书", "https://via.placeholder.com/150?text=Regular"));
                styles.add(createStyle("行书", "https://via.placeholder.com/150?text=SemiCursive"));
                styles.add(createStyle("草书", "https://via.placeholder.com/150?text=Cursive"));
                lesson.setStyles(styles);
                
                System.out.println("Importing new character: " + character + " (" + textbookCategory + ")");
            }
            
            // Get character data from mapping first
            String[] charData = CHARACTER_DATA.get(character);
            
            // If not found in mapping, use DictionaryService to get from API
            if (charData == null) {
                System.out.println("Fetching character info from API: " + character);
                charData = dictionaryService.getCharacterInfo(character);
                
                // If API returns empty data, use default
                if (charData == null || charData.length < 3 || ("").equals(charData[0]) && ("").equals(charData[1])) {
                    charData = CHARACTER_DATA.get("default");
                }
            }
            
            // Set character data
            lesson.setPinyin(charData[0]);
            lesson.setDefinition(charData[1]);
            lesson.setWords(charData[2]);
            lesson.setTextbookCategory(textbookCategory);
            
            // Save lesson (create or update)
            lessonRepository.save(lesson);
        }
    }

    private LessonStyle createStyle(String name, String imageUrl) {
        LessonStyle style = new LessonStyle();
        style.setName(name);
        style.setImgUrl(imageUrl);
        return style;
    }

    public boolean existsByCharacter(String character) {
        return lessonRepository.existsByCharacter(character);
    }

    /**
     * Update character data (pinyin, definition, words) for all existing lessons
     */
    @Transactional
    public void updateAllLessonsCharacterData() {
        // Get all lessons from database
        List<Lesson> allLessons = lessonRepository.findAll();
        
        int updatedCount = 0;
        int totalCount = allLessons.size();
        
        System.out.println("Starting to update character data for " + totalCount + " lessons...");
        
        for (Lesson lesson : allLessons) {
            String character = lesson.getCharacter();
            boolean needsUpdate = false;
            
            // Check if current data is mock data
            String currentPinyin = lesson.getPinyin();
            String currentDefinition = lesson.getDefinition();
            String currentWords = lesson.getWords();
            
            // Mock data patterns to check
            String mockPinyinPattern = character + "-pinyin";
            String mockDefinitionPattern = character + " definition";
            String mockWordsPattern = character + "词1," + character + "词2," + character + "词3";
            
            // Determine if update is needed
            if ((currentPinyin != null && currentPinyin.equals(mockPinyinPattern)) ||
                (currentDefinition != null && currentDefinition.equals(mockDefinitionPattern)) ||
                (currentWords != null && currentWords.equals(mockWordsPattern))) {
                needsUpdate = true;
            }
            
            // Only update if data is mock or empty
            if (needsUpdate || currentPinyin == null || currentDefinition == null || currentWords == null ||
                currentPinyin.isEmpty() || currentDefinition.isEmpty() || currentWords.isEmpty()) {
                // Get character data from mapping first
                String[] charData = CHARACTER_DATA.get(character);
            
            // If not found in mapping, use DictionaryService to get from API
            if (charData == null) {
                System.out.println("Fetching character info from API: " + character);
                charData = dictionaryService.getCharacterInfo(character);
                
                // If API returns empty data, use default
                if (charData == null || charData.length < 3 || ("").equals(charData[0]) && ("").equals(charData[1])) {
                    charData = CHARACTER_DATA.get("default");
                }
            }
            
            // Update fields
            lesson.setPinyin(charData[0]);
            lesson.setDefinition(charData[1]);
            lesson.setWords(charData[2]);
            
            // Save updated lesson
            lessonRepository.save(lesson);
            
            updatedCount++;
            
            // Print progress every 100 characters
            if (updatedCount % 100 == 0) {
                System.out.println("Updated " + updatedCount + " out of " + totalCount + " lessons");
            }
            }
        }
        
        System.out.println("Update completed! Updated " + updatedCount + " lessons.");
    }
}
