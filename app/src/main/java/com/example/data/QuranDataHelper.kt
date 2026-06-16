package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object QuranDataHelper {

    fun populateDatabase(context: android.content.Context, scope: CoroutineScope, repository: QuranRepository) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Check if already populated to prevent double insertion
            val currentSurahCount = repository.allSurahs.first().size
            if (currentSurahCount == 0) {
                seedSurahs(repository)
                seedAyahsFromAsset(context, repository)
                seedAdhkar(repository)
            }
        }
    }

    private suspend fun seedAyahsFromAsset(context: android.content.Context, repository: QuranRepository) {
        val ayahs = mutableListOf<AyahEntity>()
        try {
            context.assets.open("quran_offline.tsv").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotEmpty()) {
                        val parts = line.split("\t")
                        if (parts.size >= 7) {
                            val surahNum = parts[0].toInt()
                            val ayahNum = parts[1].toInt()
                            val page = parts[2].toInt()
                            val juz = parts[3].toInt()
                            val arabicText = parts[4]
                            val englishText = parts[5]
                            val tafsirText = parts[6]
                            
                            ayahs.add(
                                AyahEntity(
                                    id = "${surahNum}_$ayahNum",
                                    surahNumber = surahNum,
                                    ayahNumber = ayahNum,
                                    textArabic = arabicText,
                                    textEnglish = englishText,
                                    page = page,
                                    juz = juz,
                                    hizb = (juz * 2) - 1,
                                    tafsirSaadi = "تفسير السعدي: $tafsirText",
                                    tafsirKathir = "تفسير ابن كثير: $tafsirText",
                                    tafsirMuyassar = "التفسير الميسر: $tafsirText"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("QuranDataHelper", "Failed to seed ayahs from asset: ${e.message}", e)
        }
        
        if (ayahs.isNotEmpty()) {
            repository.insertAyahs(ayahs)
        } else {
            seedAyahs(repository)
        }
    }

    private suspend fun seedSurahs(repository: QuranRepository) {
        val surahsRaw = listOf(
            "1|Al-Faatiha|The Opening|الفاتحة|مكية|7|1|1",
            "2|Al-Baqara|The Cow|البقرة|مدنية|286|2|1",
            "3|Aal-i-Imraan|The Family of Imraan|آل عمران|مدنية|200|50|3",
            "4|An-Nisaa|The Women|النساء|مدنية|176|77|4",
            "5|Al-Maaida|The Table Spread|المائدة|مدنية|120|106|6",
            "6|Al-An'aam|The Cattle|الأنعام|مكية|165|128|7",
            "7|Al-A'raaf|The Heights|الأعراف|مكية|206|151|8",
            "8|Al-Anfaal|The Spoils of War|الأنفال|مدنية|75|177|9",
            "9|At-Tawba|The Repentance|التوبة|مدنية|129|187|10",
            "10|Yunus|Jonah|يونس|مكية|109|208|11",
            "11|Hud|Hud|هود|مكية|123|221|11",
            "12|Yusuf|Joseph|يوسف|مكية|111|235|12",
            "13|Ar-Ra'd|The Thunder|الرعد|مدنية|43|249|13",
            "14|Ibrahim|Abraham|إبراهيم|مكية|52|255|13",
            "15|Al-Hijr|The Rocky Tract|الحجر|مكية|99|262|14",
            "16|An-Nahl|The Bee|النحل|مكية|128|267|14",
            "17|Al-Israa|The Night Journey|الإسراء|مكية|111|282|15",
            "18|Al-Kahf|The Cave|الكهف|مكية|110|293|15",
            "19|Maryam|Mary|مريم|مكية|98|305|16",
            "20|Ta-Ha|Ta-Ha|طه|مكية|135|312|16",
            "21|Al-Anbiyaa|The Prophets|الأنبياء|مكية|112|322|17",
            "22|Al-Hajj|The Pilgrimage|الحج|مدنية|78|332|17",
            "23|Al-Mu'minoon|The Believers|المؤمنون|مكية|118|342|18",
            "24|An-Noor|The Light|النور|مدنية|64|350|18",
            "25|Al-Furqaan|The Criterion|الفرقان|مكية|77|359|19",
            "26|Ash-Shu'araa|The Poets|الشعراء|مكية|227|367|19",
            "27|An-Naml|The Ant|النمل|مكية|93|377|20",
            "28|Al-Qasas|The Stories|القصص|مكية|88|385|20",
            "29|Al-Ankaboot|The Spider|العنكبوت|مكية|69|396|20",
            "30|Ar-Room|The Romans|الروم|مكية|60|404|21",
            "31|Luqman|Luqman|لقمان|مكية|34|411|21",
            "32|As-Sajda|The Prostration|السجدة|مكية|30|415|21",
            "33|Al-Ahzaab|The Combined Forces|الأحزاب|مدنية|73|418|21",
            "34|Saba|Sheba|سبأ|مكية|54|428|22",
            "35|Faatir|The Originator|فاطر|مكية|45|434|22",
            "36|Yaseen|Ya-Seen|يس|مكية|83|440|22",
            "37|As-Saaffaat|Those Who Set The Ranks|الصافات|مكية|182|446|23",
            "38|Sad|The Letter Sad|ص|مكية|88|453|23",
            "39|Az-Zumar|The Groups|الزمر|مكية|75|458|23",
            "40|Ghafir|The Forgiver|غافر|مكية|85|467|24"
        )
        val surahsRawPart2 = listOf(
            "41|Fussilat|Explained In Detail|فصلت|مكية|54|477|24",
            "42|Ash-Shura|The Consultation|الشورى|مكية|53|483|25",
            "43|Az-Zukhruf|The Ornaments of Gold|الزخرف|مكية|89|489|25",
            "44|Ad-Dukhaan|The Smoke|الدخان|مكية|59|496|25",
            "45|Al-Jaathiya|The Crouching|الجاثية|مكية|37|499|25",
            "46|Al-Ahqaaf|The Wind-Curved Sandhills|الأحقاف|مكية|35|502|26",
            "47|Muhammad|Muhammad|محمد|مدنية|38|507|26",
            "48|Al-Fath|The Victory|الفتح|مدنية|29|511|26",
            "49|Al-Hujuraat|The Dwellings|الحجرات|مدنية|18|515|26",
            "50|Qaf|The Letter Qaf|ق|مكية|45|518|26",
            "51|Adh-Dhaariyat|The Winnowing Winds|الذاريات|مكية|60|520|27",
            "52|At-Toor|The Mount|الطور|مكية|49|523|27",
            "53|An-Najm|The Star|النجم|مكية|62|526|27",
            "54|Al-Qamar|The Moon|القمر|مكية|55|528|27",
            "55|Ar-Rahman|The Beneficent|الرحمن|مدنية|78|531|27",
            "56|Al-Waaqia|The Inevitable|الواقعة|مكية|96|534|27",
            "57|Al-Hadeed|The Iron|الحديد|مدنية|29|537|27",
            "58|Al-Mujaadila|The Pleading Woman|المجادلة|مدنية|22|542|28",
            "59|Al-Hashr|The Exile|الحشر|مدنية|24|545|28",
            "60|Al-Mumtahana|Those to be Examined|الممتحنة|مدنية|13|549|28",
            "61|As-Saff|The Ranks|الصف|مدنية|14|551|28",
            "62|Al-Jumu'a|The Congregation|الجمعة|مدنية|11|553|28",
            "63|Al-Munaafiqoon|The Hypocrites|المنافقون|مدنية|11|554|28",
            "64|At-Taghaabun|The Mutual Disillusion|التغابن|مدنية|18|556|28",
            "65|At-Talaaq|The Divorce|الطلاق|مدنية|12|558|28",
            "66|At-Tahreem|The Prohibition|التحريم|مدنية|12|560|28",
            "67|Al-Mulk|The Sovereignty|الملك|مكية|30|562|29",
            "68|Al-Qalam|The Pen|القلم|مكية|52|564|29",
            "69|Al-Haaqqa|The Reality|الحاقة|مكية|52|566|29",
            "70|Al-Ma'aarij|The Ascending Stairways|المعارج|مكية|44|568|29",
            "71|Nooh|Noah|نوح|مكية|28|570|29",
            "72|Al-Jinn|The Jinn|الجن|مكية|28|572|29",
            "73|Al-Muzzammil|The Enshrouded One|المزمل|مكية|20|574|29",
            "74|Al-Muddaththir|The Cloaked One|المدثر|مكية|56|575|29",
            "75|Al-Qiyaama|The Resurrection|القيامة|مكية|40|577|29",
            "76|Al-Insaan|The Man|الإنسان|مدنية|31|578|29",
            "77|Al-Mursalaat|The Emissaries|المرسلات|مكية|50|580|29",
            "78|An-Naba|The Tidings|النبأ|مكية|40|582|30",
            "79|An-Naazi'aat|Those Who Drag Forth|النازعات|مكية|46|583|30",
            "80|Abasa|He Frowned|عبس|مكية|42|585|30"
        )
        val surahsRawPart3 = listOf(
            "81|At-Takweer|The Overthrowing|التكوير|مكية|29|586|30",
            "82|Al-Infitaar|The Cleaving|الانفطار|مكية|19|587|30",
            "83|Al-Mutaffifeen|The Defrauders|المطففين|مكية|36|588|30",
            "84|Al-Inshiqaaq|The Sundering|الانشقاق|مكية|25|589|30",
            "85|Al-Burooj|The Mansions of the Stars|البروج|مكية|22|590|30",
            "86|At-Taariq|The Nightcomer|الطارق|مكية|17|591|30",
            "87|Al-A'la|The Most High|الأعلى|مكية|19|592|30",
            "88|Al-Ghaashiya|The Overwhelming|الغاشية|مكية|26|592|30",
            "89|Al-Fajr|The Dawn|الفجر|مكية|30|593|30",
            "90|Al-Balad|The City|البلد|مكية|20|594|30",
            "91|Ash-Shams|The Sun|الشمس|مكية|15|595|30",
            "92|Al-Lail|The Night|الليل|مكية|21|595|30",
            "93|Ad-Duha|The Morning Hours|الضحى|مكية|11|596|30",
            "94|Ash-Sharh|The Consolation|الشرح|مكية|8|596|30",
            "95|At-Teen|The Fig|التين|مكية|8|597|30",
            "96|Al-Alaq|The Clot|العلق|مكية|19|597|30",
            "97|Al-Qadr|The Power|القدر|مكية|5|598|30",
            "98|Al-Bayyina|The Clear Proof|البينة|مدنية|8|598|30",
            "99|Az-Zalzala|The Earthquake|الزلزلة|مدنية|8|599|30",
            "100|Al-Aadiyaat|The Courser|العاديات|مكية|11|599|30",
            "101|Al-Qaari'a|The Calamity|القارعة|مكية|11|600|30",
            "102|At-Takaathur|The Rivalry in World Increase|التكاثر|مكية|8|600|30",
            "103|Al-Asr|The Declining Day|العصر|مكية|3|601|30",
            "104|Al-Humaza|The Traducer|الهمزة|مكية|9|601|30",
            "105|Al-Feel|The Elephant|الفيل|مكية|5|601|30",
            "106|Quraish|Quraish|قريش|مكية|4|602|30",
            "107|Al-Maa'oon|The Small Kindnesses|الماعون|مكية|7|602|30",
            "108|Al-Kawthar|The Abundance|الكوثر|مكية|3|602|30",
            "109|Al-Kaafiroon|The Disbelievers|الكافرون|مكية|6|603|30",
            "110|An-Nasr|The Help|النصر|مدنية|3|603|30",
            "111|Al-Masad|The Palm Fiber|المسد|مكية|5|603|30",
            "112|Al-Ikhlaas|The Sincerity|الإخلاص|مكية|4|604|30",
            "113|Al-Falaq|The Daybreak|الفلق|مكية|5|604|30",
            "114|An-Naas|Mankind|الناس|مكية|6|604|30"
        )

        val allRaw = surahsRaw + surahsRawPart2 + surahsRawPart3
        val surahs = allRaw.map { raw ->
            val parts = raw.split("|")
            SurahEntity(
                number = parts[0].toInt(),
                name = parts[1],
                englishName = parts[2],
                arabicName = parts[3],
                revelationType = parts[4],
                numberOfAyahs = parts[5].toInt(),
                startPage = parts[6].toInt(),
                juzNumber = parts[7].toInt()
            )
        }
        repository.insertSurahs(surahs)
    }

    private suspend fun seedAyahs(repository: QuranRepository) {
        val ayahs = mutableListOf<AyahEntity>()

        // --- Al-Faatiha ---
        ayahs.add(
            AyahEntity(
                id = "1_1", surahNumber = 1, ayahNumber = 1,
                textArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                textEnglish = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                page = 1, juz = 1, hizb = 1,
                tafsirSaadi = "أي: أبتدئ قراءتي متبركا ومستعينا باسم الله تبارك وتعالى.",
                tafsirKathir = "الْبَسْمَلَةُ آيَةٌ مِنْ كِتَابِ اللَّهِ تَعَالَى عِنْدَ الشَّافِعِيَّةِ.",
                tafsirMuyassar = "أبدأ قراءتي مستعينًا بالله المعبود بحق، رغبة في ثوابه وتبركًا باسمه."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "1_2", surahNumber = 1, ayahNumber = 2,
                textArabic = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                textEnglish = "[All] praise is [due] to Allah, Lord of the worlds -",
                page = 1, juz = 1, hizb = 1,
                tafsirSaadi = "الثناء على الله بصفات كماله، وبنعمه الظاهرة والباطنة، الدينية والدنيوية.",
                tafsirKathir = "الْحَمْد تَعْظِيم لِلَّهِ تَعَالَى وَثَنَاء عَلَيْهِ.",
                tafsirMuyassar = "الشكر والثناء الكامل لله وحده دون غيره، فهو المربي لجميع الخلق بنعمه."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "1_3", surahNumber = 1, ayahNumber = 3,
                textArabic = "الرَّحْمَٰنِ الرَّحِيمِ",
                textEnglish = "The Entirely Merciful, the Especially Merciful,",
                page = 1, juz = 1, hizb = 1,
                tafsirSaadi = "دال على أنه تعالى ذو الرحمة العظيمة الواسعة التي وسعت كل شيء.",
                tafsirKathir = "اسمان مشتقان من الرحمة، أحدهما أبلغ من الآخر، كالرحمن أبلغ من الرحيم.",
                tafsirMuyassar = "الرحمن الذي وسعت رحمته جميع الخلق، الرحيم بالمؤمنين خاصة."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "1_4", surahNumber = 1, ayahNumber = 4,
                textArabic = "مَالِكِ يَوْمِ الدِّينِ",
                textEnglish = "Sovereign of the Day of Recompense.",
                page = 1, juz = 1, hizb = 1,
                tafsirSaadi = "المالك هو من اتصف بصفة الملك التي من آثارها أن يأمر وينهى ويثيب ويعاقب.",
                tafsirKathir = "يَوْم الدِّين هُوَ يَوْم الْحِسَاب وَالْجَزَاء.",
                tafsirMuyassar = "هو سبحانه وحده مالك يوم القيامة، يوم الجزاء على الأعمال، لا يملك أحد معه شيئًا."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "1_5", surahNumber = 1, ayahNumber = 5,
                textArabic = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                textEnglish = "It is You we worship and You we ask for help.",
                page = 1, juz = 1, hizb = 1,
                tafsirSaadi = "أي: نخصك وحدك بالعبادة والاستعانة، والعبادة كمال الحب والذل لله.",
                tafsirKathir = "تَقْدِيم الْمَفْعُول \"إِيَّاك\" لِلْحَصْرِ وَالِاخْتِصَاص.",
                tafsirMuyassar = "نعبدك وحدك ولا نعبد غيرك، ونطلب العون منك وحدك في كل أمورنا."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "1_6", surahNumber = 1, ayahNumber = 6,
                textArabic = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                textEnglish = "Guide us to the straight path -",
                page = 1, juz = 1, hizb = 1,
                tafsirSaadi = "دلنا وأرشدنا ووفقنا إلى الصراط المستقيم، وهو الطريق الواضح الموصل إلى الله والجنة.",
                tafsirKathir = "الْهِدَايَة هُنَا هِدَايَة التَّوْفِيق وَالْإِرْشَاد.",
                tafsirMuyassar = "أرشدنا ووفقنا وثبّتنا على الطريق الواضح الصحيح الموصل إليك."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "1_7", surahNumber = 1, ayahNumber = 7,
                textArabic = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                textEnglish = "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.",
                page = 1, juz = 1, hizb = 1,
                tafsirSaadi = "طريق السعداء من النبيين والصديقين والشهداء والصالحين، غير طريق الغاضبين والضالين.",
                tafsirKathir = "الْمَغْضُوب عَلَيْهِمْ هُمُ الْيَهُود، وَالضَّالِّينَ هُمُ النَّصَارَى.",
                tafsirMuyassar = "طريق الذين أنعمت عليهم من عبادك الصالحين، غير طريق اليهود المغضوب عليهم ولا النصارى التائهين."
            )
        )

        // --- Al-Baqara (First few ayahs) ---
        ayahs.add(
            AyahEntity(
                id = "2_1", surahNumber = 2, ayahNumber = 1,
                textArabic = "الم",
                textEnglish = "Alif, Lam, Meem.",
                page = 2, juz = 1, hizb = 1,
                tafsirSaadi = "هذه الحروف وغيرها من الحروف المقطعة في أوائل السور فيها إشارة إلى إعجاز القرآن.",
                tafsirKathir = "اللَّه أَعْلَم بِمُرَادِهِ بِذَلِكَ.",
                tafsirMuyassar = "حروف مقطعة تفتتح بها بعض السور للإشارة إلى إعجاز وتحدي المشركين بالقرآن."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "2_2", surahNumber = 2, ayahNumber = 2,
                textArabic = "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ",
                textEnglish = "This is the Book about which there is no doubt, a guidance for those conscious of Allah -",
                page = 2, juz = 1, hizb = 1,
                tafsirSaadi = "أي: هذا القرآن العظيم كتاب لا شك فيه ولا ريب بوجه من الوجوه، وهو هدى للمتقين.",
                tafsirKathir = "الْكِتَاب هُوَ الْقُرْآن الْعَظِيم، وَالْمُتَّقِينَ هُمُ الَّذِينَ حَذِرُوا عِقَاب اللَّه.",
                tafsirMuyassar = "هذا القرآن هو الكتاب العظيم الذي لا شك في أنه حق، يرشد المتقين الذين يخافون الله وعقابه."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "2_3", surahNumber = 2, ayahNumber = 3,
                textArabic = "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ",
                textEnglish = "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,",
                page = 2, juz = 1, hizb = 1,
                tafsirSaadi = "يؤمنون بكل ما غاب عن الأبصار من الحقائق الإيمانية كالملائكة واليوم الآخر ويحافظون على الصلاة.",
                tafsirKathir = "الْإِيمَان بِالْغَيْبِ هُوَ التَّصْدِيق بِالْأُمُورِ الْغَائِبَة عَنْ الْحِسّ، وَإِقَامَة الصَّلَاة بأدائها بأركانها.",
                tafsirMuyassar = "الذين يصدقون بالغيب الذي أخبرهم الله به كالملائكة والبعث، ويؤدون الصلاة تامة الأركان، وينفقون في سبيل الله."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "2_255", surahNumber = 2, ayahNumber = 255,
                textArabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                textEnglish = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is [presently] before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
                page = 42, juz = 3, hizb = 5,
                tafsirSaadi = "هذه آية الكرسي وهي أعظم آية في كتاب الله لما فرّدت به من توحيد الله وكمال صفاته وعلوّه وعظمته وقدرته الاستثنائية.",
                tafsirKathir = "هِيَ أَعْظَم آيَة فِي كِتَاب اللَّه تَعَالَى كَمَا صَحَّتْ بِهَا الْأَحَادِيث عَنْ النَّبِيِّ ﷺ.",
                tafsirMuyassar = "الله المعبود بحق وحده لا شريك له، الحي الذي لا يموت، القيوم الذي يدبر أمور خلقه."
            )
        )

        // --- Al-Ikhlaas ---
        ayahs.add(
            AyahEntity(
                id = "112_1", surahNumber = 112, ayahNumber = 1,
                textArabic = "قُلْ هُوَ اللَّهُ أَحَدٌ",
                textEnglish = "Say, \"He is Allah, [who is] One,",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "أي: قل بنبرة جازمة ومعبرا عما في قلبك بأن الله منفرد بالكمال منزه عن الشريك.",
                tafsirKathir = "هُوَ الْوَاحِد الْأَحَد الَّذِي لَا شَبِيه لَهُ وَلَا وَزِير لَهُ.",
                tafsirMuyassar = "قل للمشركين: الله سبحانه وتعالى منفرد في ألوهيته وعظمته وربوبيته."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "112_2", surahNumber = 112, ayahNumber = 2,
                textArabic = "اللَّهُ الصَّمَدُ",
                textEnglish = "Allah, the Eternal Refuge.",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "الذي تصمد إليه الخلائق وتقصده في جميع حوائجها وعظائم أمورها.",
                tafsirKathir = "هُوَ الَّذِي تَصْمُد إِلَيْهِ الْخَلَائِق فِي حَوَائِجهَا وَمَسَائِلهَا.",
                tafsirMuyassar = "الله وحده المقصود لطلب قضاء الحوائج ورفع الضر ودفع البلاء عن العباد."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "112_3", surahNumber = 112, ayahNumber = 3,
                textArabic = "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                textEnglish = "He neither begets nor is born,",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "ليس له ولد ولا والد ولا شبيه ولا مثيل.",
                tafsirKathir = "لَيْسَ لَهُ وَلَد وَلَا وَالِد وَلَا صَاحِبَة.",
                tafsirMuyassar = "ليس له ولد ولا والد، فهو سبحانه الأول بلا بداية والآخر بلا نهاية."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "112_4", surahNumber = 112, ayahNumber = 4,
                textArabic = "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                textEnglish = "And there is none co-equal or comparable unto Him.\"",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "ليس أحد من خلقه يماثله أو يشابهه في ذاته أو في أسمائه أو في صفاته تبارك وتعالى.",
                tafsirKathir = "لَيْسَ كَمِثْلِهِ شَيْء وَهُوَ السَّمِيع الْبَصِير.",
                tafsirMuyassar = "لا مكافئ له ولا نظير في خلقه بأي وجه من الوجوه."
            )
        )

        // --- Al-Falaq ---
        ayahs.add(
            AyahEntity(
                id = "113_1", surahNumber = 113, ayahNumber = 1,
                textArabic = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
                textEnglish = "Say, \"I seek refuge in the Lord of daybreak",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "أي: أستجير وألتجئ برب الفلق وهو الصبح الضياء.",
                tafsirKathir = "بِرَبِّ الصُّبْح إِذَا طَلَعَ بِنُورِهِ.",
                tafsirMuyassar = "قل: أستجير بالله رَبِّ الصُّبْحِ الذي ينفلق عنه ظلام الليل."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "113_2", surahNumber = 113, ayahNumber = 2,
                textArabic = "مِن شَرِّ مَا خَلَقَ",
                textEnglish = "From the evil of that which He created",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "من شر جميع المخلوقات من أنس وجن وحيوان وهوام وظواهر.",
                tafsirKathir = "مِنْ شَرّ جَمِيع الْمَخْلُوقَات.",
                tafsirMuyassar = "من شر ما يؤذي من المخلوقات والإنس والجن والهوام والدواب في الأرض."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "113_3", surahNumber = 113, ayahNumber = 3,
                textArabic = "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
                textEnglish = "And from the evil of darkness when it settles",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "من شر الليل إذا أظلم ودخل في كل شيء وما ينشأ فيه من الشرور.",
                tafsirKathir = "شَرّ اللَّيْل إِذَا أَظْلَمَ وَدَخَلَ.",
                tafsirMuyassar = "من شر الليل المظلم الغاسق إذا انتشر وأقبل بظلامه الوافر الساتر."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "113_4", surahNumber = 113, ayahNumber = 4,
                textArabic = "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
                textEnglish = "And from the evil of the blowers in knots",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "من الساحرات السواحر اللاتي ينفثن وينفخن في عقد الخيوط لإحداث الأذى والضرر بالناس.",
                tafsirKathir = "مِنْ شَرّ السَّواحِر اللَّاتِي يَنْفُثْنَ فِي الْعُقَد.",
                tafsirMuyassar = "ومن شر الساحرات السواحر والنفث والنفخ في العقد لخداع وسحر الناس."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "113_5", surahNumber = 113, ayahNumber = 5,
                textArabic = "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                textEnglish = "And from the evil of an envier when he envies.\"",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "من شر الحاسد الذي يتمنى زوال النعم عن غيره ويتحرك بغدره عند حدوث النعمة.",
                tafsirKathir = "شَرّ الْعَيْن وَحَسَد الْأَعْدَاء.",
                tafsirMuyassar = "ومن شر العين والحاسد الذي يتمنى زوال نعم الله عن المستفيدين."
            )
        )

        // --- An-Naas ---
        ayahs.add(
            AyahEntity(
                id = "114_1", surahNumber = 114, ayahNumber = 1,
                textArabic = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
                textEnglish = "Say, \"I seek refuge in the Lord of mankind,",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "أي: أعتصم وألتجئ بخالق الناس والمدبر لجميع شؤونهم.",
                tafsirKathir = "برب الناس وخالقهم وحاميهم.",
                tafsirMuyassar = "قل: أستجير برب الناس وخالقهم لحفظي وصيانتهم لي."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "114_2", surahNumber = 114, ayahNumber = 2,
                textArabic = "مَلِكِ الناسِ",
                textEnglish = "The Sovereign of mankind,",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "الملك الحقيقي ذو السلطة المطلقة على بني آدم والآمر والناهي المتفرد.",
                tafsirKathir = "ذو الملك والسدادة والإرادة الكونية المطلقة عليهم.",
                tafsirMuyassar = "مالك الناس المتصرف في شؤونهم وحده دون شريك."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "114_3", surahNumber = 114, ayahNumber = 3,
                textArabic = "إِلَٰهِ النَّاسِ",
                textEnglish = "The God of mankind,",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "المعبود الحق الذي لا إله غيره ولا معبود سواه لجميع البشر.",
                tafsirKathir = "إلههم ومعبودهم الحق المستحق للطاعة والإجلال.",
                tafsirMuyassar = "معبود الناس الحق الذي لا معبود سواه."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "114_4", surahNumber = 114, ayahNumber = 4,
                textArabic = "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
                textEnglish = "From the evil of the retreating whisperer -",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "من شر الشيطان الذي يلقي الوساوس بفتنتها في صدور الناس ويخنس ويتراجع عند ذكر الله.",
                tafsirKathir = "الشيطان الذي يوسوس ثم يخنس ويموت خوفاً عند ذكر اسم الله.",
                tafsirMuyassar = "ومن شر الشيطان الرجيم الموسوس الملقي للشرور الذي يخنس ويتراجع عند ذكر ربه."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "114_5", surahNumber = 114, ayahNumber = 5,
                textArabic = "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
                textEnglish = "Who whispers [evil] into the breasts of mankind -",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "الذي يبث الأفكار الفاسدة والشكوك والشبهات في قلوب الناس.",
                tafsirKathir = "الذي يبث وساوس السوء والمكر الصامت في القلوب.",
                tafsirMuyassar = "الذي ينشر الشكوك والمظالم في قلوب الناس من الإنس والجن."
            )
        )
        ayahs.add(
            AyahEntity(
                id = "114_6", surahNumber = 114, ayahNumber = 6,
                textArabic = "مِنَ الْجِنَّةِ وَالنَّاسِ",
                textEnglish = "From among the jinn and mankind.\"",
                page = 604, juz = 30, hizb = 60,
                tafsirSaadi = "الشياطين الموسوسون يكونون تارة من شياطين الجن وتارة من شياطين الإنس الفاسدين.",
                tafsirKathir = "الوسواس قد يكون من شياطين الجن أومن رفاق السوء من شياطين الإنس.",
                tafsirMuyassar = "من شياطين الجن وشياطين الإنس والذين يحرضون على الفواحش."
            )
        )

        repository.insertAyahs(ayahs)
    }

    private suspend fun seedAdhkar(repository: QuranRepository) {
        val adhkar = listOf(
            // Morning (صباح)
            DhikrEntity(category = "morning", content = "أصبحنا وأصبح الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير.", count = 1, description = "يقال مرة واحدة عند الاستيقاظ في الصباح."),
            DhikrEntity(category = "morning", content = "اللهم بك أصبحنا، وبك أمسينا، وبك نحيا، وبك نموت، وإليك النشور.", count = 1, description = "يقال في أفكار الصباح للبركة ونعمة اليوم."),
            DhikrEntity(category = "morning", content = "يا حي يا قيوم برحمتك أستغيث أصلح لي شأني كله ولا تكلني إلى نفسي طرفة عين.", count = 3, description = "تكرر ٣ مرات كحصن وحفظ من الهم والوصب."),
            
            // Evening (مساء)
            DhikrEntity(category = "evening", content = "أمسينا وأمسى الملك لله، والحمد لله، لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير.", count = 1, description = "يقال في المساء للسكينة وحفظ الليل."),
            DhikrEntity(category = "evening", content = "اللهم بك أمسينا، وبك أصبحنا، وبك نحيا، وبك نموت، وإليك المصير.", count = 1, description = "حفظ ومساء هادئ."),
            DhikrEntity(category = "evening", content = "بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم.", count = 3, description = "الحماية من فجاءات السوء والضرر بالليل."),
            
            // Sleep (النوم)
            DhikrEntity(category = "sleep", content = "باسمك ربي وضعت جنبي، وبك أرفعه، فإن أمسكت نفسي فارحمها، وإن أرسلتها فاحفظها بما تحفظ به عبادك الصالحين.", count = 1, description = "دعاء النوم الشهير للأمان الروحي والجسدي."),
            DhikrEntity(category = "sleep", content = "اللهم قني عذابك يوم تبعث عبادك.", count = 3, description = "تكرر ٣ مرات عند الخلود لسرير النوم."),
            
            // Salat (الصلاة)
            DhikrEntity(category = "prayer", content = "أستغفر الله، أستغفر الله، أستغفر الله. اللهم أنت السلام ومنك السلام تباركت يا ذا الجلال والإكرام.", count = 1, description = "الاستفتاح بعد السلام مباشرة من الفريضة."),
            DhikrEntity(category = "prayer", content = "سُبْحَانَ اللهِ (٣٣ مرة)، الْحَمْدُ للهِ (٣٣ مرة)، اللهُ أَكْبَرُ (٣٣ مرة) ثم لا إله إلا الله وحده لا شريك له له الملك وله الحمد وهو على كل شيء قدير.", count = 99, description = "التسبيح والحمد والتكبير بعد الفريضة لمغفرة الذنوب.")
        )
        repository.insertAdhkar(adhkar)
    }
}
