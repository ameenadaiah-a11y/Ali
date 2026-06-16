package com.example.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST Models ---

data class GeminiRequest(
    @SerializedName("contents") val contents: List<GeminiContent>,
    @SerializedName("systemInstruction") val systemInstruction: GeminiInstruction? = null,
    @SerializedName("generationConfig") val generationConfig: GeminiGenConfig? = null
)

data class GeminiInstruction(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiContent(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String
)

data class GeminiGenConfig(
    @SerializedName("temperature") val temperature: Float = 0.7f,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 1024
)

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun askGemini(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
