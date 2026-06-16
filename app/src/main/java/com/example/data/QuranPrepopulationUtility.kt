package com.example.data

import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileWriter
import java.net.URL
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

data class FullQuranResponse(
    val code: Int,
    val status: String,
    val data: FullQuranData
)

data class FullQuranData(
    val surahs: List<QuranSurahData>
)

object QuranPrepopulationUtility {
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== بدء تحميل وتجهيز بيانات المصحف الشريف للأوفلاين (الوضع السريع المتوازي) ===")
        
        val assetsDir = File("app/src/main/assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
            println("تم إنشاء مجلد الأصول: ${assetsDir.absolutePath}")
        }
        
        val outputFile = File(assetsDir, "quran_offline.tsv")
        if (outputFile.exists()) {
            outputFile.delete()
        }
        
        val gson = Gson()
        
        // Use a customized OkHttpClient for large downloads with robust timeouts
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
            
        fun downloadUrl(urlStr: String): String {
            val request = okhttp3.Request.Builder().url(urlStr).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw java.io.IOException("Unexpected code $response")
                return response.body()?.string() ?: ""
            }
        }
        
        runBlocking {
            try {
                println("تحميل المصحف كاملًا بشكل متوازي...")
                
                val arabicJob = async(kotlinx.coroutines.Dispatchers.IO) {
                    println("جاري تنزيل النص العربي العثماني...")
                    val json = downloadUrl("https://api.alquran.cloud/v1/quran/quran-uthmani")
                    gson.fromJson(json, FullQuranResponse::class.java)
                }
                
                val tafsirJob = async(kotlinx.coroutines.Dispatchers.IO) {
                    println("جاري تنزيل التفسير الميسر...")
                    val json = downloadUrl("https://api.alquran.cloud/v1/quran/ar.muyassar")
                    gson.fromJson(json, FullQuranResponse::class.java)
                }
                
                val arabicQuran = arabicJob.await()
                val tafsirQuran = tafsirJob.await()
                
                println("تم تحميل جميع البيانات بنجاح في الذاكرة. جاري الدمج وصياغة ملف TSV...")
                
                val writer = FileWriter(outputFile, false)
                
                val arabicSurahs = arabicQuran.data.surahs
                val tafsirSurahs = tafsirQuran.data.surahs
                
                for (sIndex in arabicSurahs.indices) {
                    val aSurah = arabicSurahs[sIndex]
                    val tSurah = tafsirSurahs.getOrNull(sIndex)
                    
                    val surahNum = aSurah.number
                    val aAyahs = aSurah.ayahs
                    val tAyahs = tSurah?.ayahs ?: emptyList()
                    
                    for (aIndex in aAyahs.indices) {
                        val aAyah = aAyahs[aIndex]
                        val tAyah = tAyahs.getOrNull(aIndex)
                        
                        val ayahNum = aAyah.numberInSurah
                        val page = aAyah.page
                        val juz = aAyah.juz
                        
                        val arabicText = aAyah.text.replace("\n", " ").replace("\t", " ").trim()
                        val englishText = ""
                        val tafsirText = (tAyah?.text ?: "").replace("\n", " ").replace("\t", " ").trim()
                        
                        writer.write("$surahNum\t$ayahNum\t$page\t$juz\t$arabicText\t$englishText\t$tafsirText\n")
                    }
                }
                
                writer.flush()
                writer.close()
                println("=== تم تجهيز ملف الأوفلاين للقرآن الكريم بنجاح! طاقة البيانات كاملة ومرشحة ===")
                println("المساحة الإجمالية للملف: ${outputFile.length() / 1024} كيلوبايت")
            } catch (e: Exception) {
                println("فشل أثناء دمج وتحويل البيانات الكلية: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
