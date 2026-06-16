package com.example.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class QuranSurahResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: QuranSurahData
)

data class QuranSurahData(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String,
    @SerializedName("englishName") val englishName: String,
    @SerializedName("revelationType") val revelationType: String,
    @SerializedName("numberOfAyahs") val numberOfAyahs: Int,
    @SerializedName("ayahs") val ayahs: List<QuranAyahData>
)

data class QuranAyahData(
    @SerializedName("number") val number: Int,
    @SerializedName("text") val text: String,
    @SerializedName("numberInSurah") val numberInSurah: Int,
    @SerializedName("juz") val juz: Int,
    @SerializedName("page") val page: Int
)

interface QuranApiService {
    @GET("v1/surah/{surahNumber}/quran-simple")
    suspend fun getArabicSurah(@Path("surahNumber") surahNumber: Int): QuranSurahResponse

    @GET("v1/surah/{surahNumber}/en.sahih")
    suspend fun getEnglishSurah(@Path("surahNumber") surahNumber: Int): QuranSurahResponse

    @GET("v1/surah/{surahNumber}/ar.muyassar")
    suspend fun getMuyassarTafsir(@Path("surahNumber") surahNumber: Int): QuranSurahResponse
}

object QuranApiClient {
    private const val BASE_URL = "https://api.alquran.cloud/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuranApiService::class.java)
    }
}
