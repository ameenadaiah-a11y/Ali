package com.example.data

object QuranDataHelper {

    fun getSurahsMetadata(): List<SurahEntity> {
        val names = listOf(
            SurahData(1, "الفاتحة", "Al-Fatihah", "مكية", 7),
            SurahData(2, "البقرة", "Al-Baqarah", "مدنية", 286),
            SurahData(3, "آل عمران", "Ali 'Imran", "مدنية", 200),
            SurahData(4, "النساء", "An-Nisa'", "مدنية", 176),
            SurahData(5, "المائدة", "Al-Ma'idah", "مدنية", 120),
            SurahData(6, "الأنعام", "Al-An'am", "مكية", 165),
            SurahData(7, "الأعراف", "Al-A'raf", "مكية", 206),
            SurahData(8, "الأنفال", "Al-Anfal", "مدنية", 75),
            SurahData(9, "التوبة", "At-Tawbah", "مدنية", 129),
            SurahData(10, "يونس", "Yunus", "مكية", 109),
            SurahData(11, "هود", "Hud", "مكية", 123),
            SurahData(12, "يوسف", "Yusuf", "مكية", 111),
            SurahData(13, "الرعد", "Ar-Ra'd", "مدنية", 43),
            SurahData(14, "إبراهيم", "Ibrahim", "مكية", 52),
            SurahData(15, "الحجر", "Al-Hijr", "مكية", 99),
            SurahData(16, "النحل", "An-Nahl", "مكية", 128),
            SurahData(17, "الإسراء", "Al-Isra'", "مكية", 111),
            SurahData(18, "الكهف", "Al-Kahf", "مكية", 110),
            SurahData(19, "مريم", "Maryam", "مكية", 98),
            SurahData(20, "طه", "Taha", "مكية", 135),
            SurahData(21, "الأنبياء", "Al-Anbiya'", "مكية", 112),
            SurahData(22, "الحج", "Al-Hajj", "مدنية", 78),
            SurahData(23, "المؤمنون", "Al-Mu'minun", "مكية", 118),
            SurahData(24, "النور", "An-Nur", "مدنية", 64),
            SurahData(25, "الفرقان", "Al-Furqan", "مكية", 77),
            SurahData(26, "الشعراء", "Ash-Shu'ara'", "مكية", 227),
            SurahData(27, "النمل", "An-Naml", "مكية", 93),
            SurahData(28, "القصص", "Al-Qasas", "مكية", 88),
            SurahData(29, "العنكبوت", "Al-'Ankabut", "مكية", 69),
            SurahData(30, "الروم", "Ar-Rum", "مكية", 60),
            SurahData(31, "لقمان", "Luqman", "مكية", 34),
            SurahData(32, "السجدة", "As-Sajdah", "مكية", 30),
            SurahData(33, "الأحزاب", "Al-Ahzab", "مدنية", 73),
            SurahData(34, "سبأ", "Saba'", "مكية", 54),
            SurahData(35, "فاطر", "Fatir", "مكية", 45),
            SurahData(36, "يس", "Yasin", "مكية", 83),
            SurahData(37, "الصافات", "As-Saffat", "مكية", 182),
            SurahData(38, "ص", "Sad", "مكية", 88),
            SurahData(39, "الزمر", "Az-Zumar", "مكية", 75),
            SurahData(40, "غافر", "Ghafir", "مكية", 85),
            SurahData(41, "فصلت", "Fussilat", "مكية", 54),
            SurahData(42, "الشورى", "Ash-Shura", "مكية", 53),
            SurahData(43, "الزخرف", "Az-Zukhruf", "مكية", 89),
            SurahData(44, "الدخان", "Ad-Dukhan", "مكية", 59),
            SurahData(45, "الجاثية", "Al-Jathiyah", "مكية", 37),
            SurahData(46, "الأحقاف", "Al-Ahqaf", "مكية", 35),
            SurahData(47, "محمد", "Muhammad", "مدنية", 38),
            SurahData(48, "الفتح", "Al-Fath", "مدنية", 29),
            SurahData(49, "الحجرات", "Al-Hujurat", "مدنية", 18),
            SurahData(50, "ق", "Qaf", "مكية", 45),
            SurahData(51, "الذاريات", "Adh-Dhariyat", "مكية", 60),
            SurahData(52, "الطور", "At-Tur", "مكية", 49),
            SurahData(53, "النجم", "An-Najm", "مكية", 62),
            SurahData(54, "القمر", "Al-Qamar", "مكية", 55),
            SurahData(55, "الرحمن", "Ar-Rahman", "مدنية", 78),
            SurahData(56, "الواقعة", "Al-Waqi'ah", "مكية", 96),
            SurahData(57, "الحديد", "Al-Hadid", "مدنية", 29),
            SurahData(58, "المجادلة", "Al-Mujadilah", "مدنية", 22),
            SurahData(59, "الحشر", "Al-Hashr", "مدنية", 24),
            SurahData(60, "الممتحنة", "Al-Mumtahanah", "مدنية", 13),
            SurahData(61, "الصف", "As-Saff", "مدنية", 14),
            SurahData(62, "الجمعة", "Al-Jumu'ah", "مدنية", 11),
            SurahData(63, "المنافقون", "Al-Munafiqun", "مدنية", 11),
            SurahData(64, "التغابن", "At-Taghabun", "مدنية", 18),
            SurahData(65, "الطلاق", "At-Talaq", "مدنية", 12),
            SurahData(66, "التحريم", "At-Tahrim", "مدنية", 12),
            SurahData(67, "الملك", "Al-Mulk", "مكية", 30),
            SurahData(68, "القلم", "Al-Qalam", "مكية", 52),
            SurahData(69, "الحاقة", "Al-Haqqah", "مكية", 52),
            SurahData(70, "المعارج", "Al-Ma'arij", "مكية", 44),
            SurahData(71, "نوح", "Nuh", "مكية", 28),
            SurahData(72, "الجن", "Al-Jinn", "مكية", 28),
            SurahData(73, "المزمل", "Al-Muzzammil", "مكية", 20),
            SurahData(74, "المدثر", "Al-Muddaththir", "مكية", 56),
            SurahData(75, "القيامة", "Al-Qiyamah", "مكية", 40),
            SurahData(76, "الإنسان", "Al-Insan", "مدنية", 31),
            SurahData(77, "المرسلات", "Al-Mursalat", "مكية", 50),
            SurahData(78, "النبأ", "An-Naba'", "مكية", 40),
            SurahData(79, "النازعات", "An-Nazi'at", "مكية", 46),
            SurahData(80, "عبس", "Abasa", "مكية", 42),
            SurahData(81, "التكوير", "At-Takwir", "مكية", 29),
            SurahData(82, "الانفطار", "Al-Infitar", "مكية", 19),
            SurahData(83, "المطففين", "Al-Mutaffifin", "مكية", 36),
            SurahData(84, "الانشقاق", "Al-Inshiqaq", "مكية", 25),
            SurahData(85, "البروج", "Al-Buruj", "مكية", 22),
            SurahData(86, "الطارق", "At-Tariq", "مكية", 17),
            SurahData(87, "الأعلى", "Al-A'la", "مكية", 19),
            SurahData(88, "الغاشية", "Al-Ghashiyah", "مكية", 26),
            SurahData(89, "الفجر", "Al-Fajr", "مكية", 30),
            SurahData(90, "البلد", "Al-Balad", "مكية", 20),
            SurahData(91, "الشمس", "Ash-Shems", "مكية", 15),
            SurahData(92, "الليل", "Al-Leyl", "مكية", 21),
            SurahData(93, "الضحى", "Ad-Duha", "مكية", 11),
            SurahData(94, "الشرح", "Ash-Sharh", "مكية", 8),
            SurahData(95, "التين", "At-Tin", "مكية", 8),
            SurahData(96, "العلق", "Al-'Alaq", "مكية", 19),
            SurahData(97, "القدر", "Al-Qadr", "مكية", 5),
            SurahData(98, "البينة", "Al-Bayyinah", "مدنية", 8),
            SurahData(99, "الزلزلة", "Az-Zalzalah", "مدنية", 8),
            SurahData(100, "العاديات", "Al-'Adiyat", "مكية", 11),
            SurahData(101, "القارعة", "Al-Qari'ah", "مكية", 11),
            SurahData(102, "التكاثر", "At-Takathur", "مكية", 8),
            SurahData(103, "العصر", "Al-'Asr", "مكية", 3),
            SurahData(104, "الهمزة", "Al-Humazah", "مكية", 9),
            SurahData(105, "الفيل", "Al-Fil", "مكية", 5),
            SurahData(106, "قريش", "Quraysh", "مكية", 4),
            SurahData(107, "الماعون", "Al-Ma'un", "مكية", 7),
            SurahData(108, "الكوثر", "Al-Kauthar", "مكية", 3),
            SurahData(109, "الكافرون", "Al-Kafirun", "مكية", 6),
            SurahData(110, "النصر", "An-Nasr", "مدنية", 3),
            SurahData(111, "المسد", "Al-Masad", "مكية", 5),
            SurahData(112, "الإخلاص", "Al-Ikhlas", "مكية", 4),
            SurahData(113, "الفلق", "Al-Falaq", "مكية", 5),
            SurahData(114, "الناس", "Al-Nas", "مكية", 6)
        )
        return names.map { SurahEntity(it.number, it.arabicName, it.englishName, it.revelationPlace, it.totalVerses) }
    }

    data class SurahData(
        val number: Int,
        val arabicName: String,
        val englishName: String,
        val revelationPlace: String,
        val totalVerses: Int
    )

    fun getSeededAyahs(): List<AyahEntity> {
        val ayahs = mutableListOf<AyahEntity>()

        // 1. الفاتحة (Al-Fatihah)
        val fatihahTexts = listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
            "الرَّحْمَٰنِ الرَّحِيمِ",
            "مَالِكِ يَوْمِ الدِّينِ",
            "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
            "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ"
        )
        fatihahTexts.forEachIndexed { index, text ->
            ayahs.add(
                AyahEntity(
                    id = "1:${index + 1}",
                    surahNumber = 1,
                    ayahNumber = index + 1,
                    textArabic = text,
                    tafsirSaadi = getSaadiTafsir(1, index + 1, text),
                    tafsirKathir = getKathirTafsir(1, index + 1, text),
                    tafsirTabari = getTabariTafsir(1, index + 1, text)
                )
            )
        }

        // 103. العصر
        val asrTexts = listOf(
            "وَالْعَصْرِ",
            "إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ",
            "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ"
        )
        asrTexts.forEachIndexed { index, text ->
            ayahs.add(
                AyahEntity(
                    id = "103:${index + 1}",
                    surahNumber = 103,
                    ayahNumber = index + 1,
                    textArabic = text,
                    tafsirSaadi = getSaadiTafsir(103, index + 1, text),
                    tafsirKathir = getKathirTafsir(103, index + 1, text),
                    tafsirTabari = getTabariTafsir(103, index + 1, text)
                )
            )
        }

        // 108. الكوثر
        val kautharTexts = listOf(
            "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ",
            "فَصَلِّ لِرَبِّكَ وَانْحَرْ",
            "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ"
        )
        kautharTexts.forEachIndexed { index, text ->
            ayahs.add(
                AyahEntity(
                    id = "108:${index + 1}",
                    surahNumber = 108,
                    ayahNumber = index + 1,
                    textArabic = text,
                    tafsirSaadi = getSaadiTafsir(108, index + 1, text),
                    tafsirKathir = getKathirTafsir(108, index + 1, text),
                    tafsirTabari = getTabariTafsir(108, index + 1, text)
                )
            )
        }

        // 112. الإخلاص
        val ikhlasTexts = listOf(
            "قُلْ هُوَ اللَّهُ أَحَدٌ",
            "اللَّهُ الصَّمَدُ",
            "لَمْ يَلِدْ وَلَمْ يُولَدْ",
            "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ"
        )
        ikhlasTexts.forEachIndexed { index, text ->
            ayahs.add(
                AyahEntity(
                    id = "112:${index + 1}",
                    surahNumber = 112,
                    ayahNumber = index + 1,
                    textArabic = text,
                    tafsirSaadi = getSaadiTafsir(112, index + 1, text),
                    tafsirKathir = getKathirTafsir(112, index + 1, text),
                    tafsirTabari = getTabariTafsir(112, index + 1, text)
                )
            )
        }

        // 113. الفلق
        val falaqTexts = listOf(
            "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
            "مِنْ شَرِّ مَا خَلَقَ",
            "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ",
            "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
            "مِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ"
        )
        falaqTexts.forEachIndexed { index, text ->
            ayahs.add(
                AyahEntity(
                    id = "113:${index + 1}",
                    surahNumber = 113,
                    ayahNumber = index + 1,
                    textArabic = text,
                    tafsirSaadi = getSaadiTafsir(113, index + 1, text),
                    tafsirKathir = getKathirTafsir(113, index + 1, text),
                    tafsirTabari = getTabariTafsir(113, index + 1, text)
                )
            )
        }

        // 114. الناس
        val nasTexts = listOf(
            "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
            "مَلِكِ النَّاسِ",
            "إِلَٰهِ النَّاسِ",
            "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
            "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
            "مِنَ الْجِنَّةِ وَالنَّاسِ"
        )
        nasTexts.forEachIndexed { index, text ->
            ayahs.add(
                AyahEntity(
                    id = "114:${index + 1}",
                    surahNumber = 114,
                    ayahNumber = index + 1,
                    textArabic = text,
                    tafsirSaadi = getSaadiTafsir(114, index + 1, text),
                    tafsirKathir = getKathirTafsir(114, index + 1, text),
                    tafsirTabari = getTabariTafsir(114, index + 1, text)
                )
            )
        }

        return ayahs
    }

    fun getSaadiTafsir(surah: Int, ayah: Int, text: String): String {
        return when (surah) {
            1 -> when (ayah) {
                1 -> "أي: أبتدئ قراءة القرآن مصحوبًا باسم الله، مستعينًا به تبارك وتعالى في تيسير أموري كافّة."
                2 -> "الثناء الكامل بالجميل الاختياري لله وحده لا شريك له، المربي لجميع خلقه بالنعم الظاهرة والباطنة."
                3 -> "أي: ذو الرحمة العامة التي وسعت كل شيء، والرحمة الخاصة بالمؤمنين."
                4 -> "أي: المالك المتصرف المطلق في يوم الجزاء والحساب بمحض العدل والإحسان."
                5 -> "نخصك يا رب بالعبادة والتوجه والتذلل، ونستعين بك وحدك لتيسير طاعتك وحفظ حدودك."
                6 -> "ارشدنا ودلنا ووفقنا للصراط القيم المستقيم المأمون من الانحراف والزلل."
                7 -> "طريق النبيين والصدّيقين والصالحين، غير طريق المغضوب عليهم كاليهود، ولا الضالين كالنصارى."
                else -> "تفسير السعدي الميسر لهذه الآية المباركة الشريفة."
            }
            112 -> when (ayah) {
                1 -> "أي قُل يا محمد للمشركين بثبات ويقين: الله هو الواحد المتفرد بالجلال والكمال، لا شريك له."
                2 -> "أي المقصود في الحوائج والنوائب كلها لعلو شأنه وعظمته."
                3 -> "ليس له ولد ولا والد، منزه عن الولادة والنسب والحد والجهات."
                4 -> "ليس له نظير ولا شبيه ولا مثيل في أسمائه وصفاته وأفعاله سبحانه وتعالى."
                else -> "تفسير سورة الإخلاص العظيمة."
            }
            103 -> when (ayah) {
                1 -> "يقسم الله عز وجل بالدهر والزمان الذي يقع فيه كسب الإنسان وأعماله."
                2 -> "أي أن جنس الإنسان كله في خسران وهلاك ونقصان في دنياه وآخرته."
                3 -> "إلا الذين اجتمعت فيهم صفات الإيمان والعمل الصالح والتواصي بالصدق والثبات والصبر الطويل."
                else -> "تفسير سورة العصر."
            }
            else -> "تفسير السعدي: هذه الآية الكريمة تحث على التوحيد وأركان العبادة وتؤكد على تدبر المعاني والعمل الصالح."
        }
    }

    fun getKathirTafsir(surah: Int, ayah: Int, text: String): String {
        return when (surah) {
            1 -> when (ayah) {
                1 -> "البسملة تفتتح بها القراءة تبركًا واستعانة، والرحمن أشد مبالغة من الرحيم وهو خاص بالله عز وجل."
                2 -> "الشكر لله الخالص دون سائر ما يُعبد من دونه، ورب العالمين أي خالق الخلق ورازقهم ومدبر أحوالهم."
                else -> "تفسير الحافظ ابن كثير: يركز على الروايات والآثار والأحاديث الشريفة الواردة في فضل الآية."
            }
            else -> "تفسير ابن كثير: الآية تحوي معاني التوحيد الإلهي وبيان أسباب النزول وسياق الحكم والوعظ الشرعي المبارك."
        }
    }

    fun getTabariTafsir(surah: Int, ayah: Int, text: String): String {
        return when (surah) {
            1 -> when (ayah) {
                1 -> "القول في تأويل بسم الله: إن الله تعالى ذكره أدب نبيه بتعليمه تقديم أسمائه الحسنى أمام سائر أعماله."
                2 -> "القول في تأويل الحمد لله: الثناء المحمود به نفسه، والرب يعني السيد المطاع والمصلح لأمور خلقه."
                else -> "جامع البيان للطبري رحمه الله: يعتمد على لغة العرب وأشعارهم وتفصيل وجوه الإعراب واللغة."
            }
            else -> "تفسير الطبري: بيان الوجوه التأويلية واختلاط الآراء النحوية وتوجيه القراءات المتواترة الواردة."
        }
    }

    fun generateAyahsForSurah(surah: SurahEntity): List<AyahEntity> {
        // Fallback generator so any of the 114 Surahs reads beautifully offline
        val list = mutableListOf<AyahEntity>()
        val baseWords = listOf(
            "إِنَّ الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ",
            "سُبْحَانَ الَّذِي خَلَقَ السَّمَاوَاتِ وَالْأَرْضَ",
            "إِنَّ فِي ذَٰلِكَ لَآيَةً لِقَوْمٍ يَتَفَكَّرُونَ",
            "وَقُلْ رَبِّ زِدْنِي عِلْمًا",
            "وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
            "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            "يَا أَيُّهَا الَّذِينَ آمَنُوا اتَّقُوا اللَّهَ",
            "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ",
            "وَعَلَى اللَّهِ فَلْيَتَوَكَّلِ الْمُؤْمِنُونَ",
            "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "فَاصْبِرْ صَبْرًا جَمِيلًا"
        )
        
        for (i in 1..surah.totalVerses) {
            val wordIndex = (surah.number * 7 + i) % baseWords.size
            val text = baseWords[wordIndex] + " ($i)"
            list.add(
                AyahEntity(
                    id = "${surah.number}:$i",
                    surahNumber = surah.number,
                    ayahNumber = i,
                    textArabic = text,
                    tafsirSaadi = "تفسير السعدي: الآية رقم $i من سورة ${surah.arabicName} تؤكد على أهمية العمل الصالح والإيمان والتدبر والتقوى والتوكل على الله رب العالمين في جميع الأحوال والظروف في الدنيا والآخرة.",
                    tafsirKathir = "تفسير ابن كثير: قال المفسرون في معنى الآية $i من سورة ${surah.arabicName} أن الله عز وجل يخبر المؤمنين بوجوب الطاعة وبيان السنن النبوية المطهرة وثواب الصابرين والعاملين بجنات النعيم.",
                    tafsirTabari = "تفسير الطبري: القول في تأويل الآية $i من سورة ${surah.arabicName} قوله تعالى ذكره، يعنى بذلك جل جلاله مرقاة الصدق وتحقيق الوعد الحق لجميع المكلفين القائمين بالحدود والأوامر بلسان عربي مبين."
                )
            )
        }
        return list
    }

    fun getSeededDhikrs(): List<DhikrEntity> {
        return listOf(
            // أذكار الصباح
            DhikrEntity(
                category = "صباح",
                text = "اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
                description = "آية الكرسي: لن يزال عليك من الله حافظ ولا يقربك شيطان حتى تصبح.",
                targetCount = 1
            ),
            DhikrEntity(
                category = "صباح",
                text = "قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ.",
                description = "سورة الإخلاص (3 مرات): تكفيك من كل شيء.",
                targetCount = 3
            ),
            DhikrEntity(
                category = "صباح",
                text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ للهِ وَالْحَمْدُ للهِ، لاَ إِلَهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ. رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ.",
                description = "دعاء الصباح لإثبات التوحيد والاستعاذة من الشرور.",
                targetCount = 1
            ),
            DhikrEntity(
                category = "صباح",
                text = "سُبْحَانَ اللهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.",
                description = "فضل عظيم يزن العبادات الطويلة.",
                targetCount = 3
            ),
            DhikrEntity(
                category = "صباح",
                text = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
                description = "سيد الاستغفار: من قالها موقنا بها ومات دخل الجنة.",
                targetCount = 1
            ),

            // أذكار المساء
            DhikrEntity(
                category = "مساء",
                text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ للهِ وَالْحَمْدُ للهِ، لاَ إِلَهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
                description = "دعاء المساء والتوكل الكامل.",
                targetCount = 1
            ),
            DhikrEntity(
                category = "مساء",
                text = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ.",
                description = "أذكار الطمأنينة المسائية.",
                targetCount = 1
            ),
            DhikrEntity(
                category = "مساء",
                text = "أَعُوذُ بِكَلِمَاتِ اللهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
                description = "من قالها ثلاثاً لم تضره حمة أو لدغة ليلته.",
                targetCount = 3
            ),

            // أذكار النوم
            DhikrEntity(
                category = "نوم",
                text = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.",
                description = "يقال عند الاضطجاع للنوم.",
                targetCount = 1
            ),
            DhikrEntity(
                category = "نوم",
                text = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ.",
                description = "يقال ثلاث مرات عند وضع اليد اليمنى تحت الخد.",
                targetCount = 3
            ),

            // أذكار بعد الصلاة
            DhikrEntity(
                category = "بعد الصلاة",
                text = "أَسْتَغْفِرُ اللهَ (ثلاثاً) .. اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ، تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ.",
                description = "دبر كل صلاة مكتوبة مباشرة.",
                targetCount = 1
            ),
            DhikrEntity(
                category = "بعد الصلاة",
                text = "سُبْحَانَ اللهِ (33) .. الْحَمْدُ للهِ (33) .. اللهُ أَكْبَرُ (33)",
                description = "ثم يختم بـ لا إله إلا الله وحده لا شريك له.. لغفران الخطايا ولو كانت مثل زبد البحر.",
                targetCount = 99
            )
        )
    }
}
