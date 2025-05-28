package com.example.educards2

import com.example.educards2.database.Card
import com.example.educards2.database.Deck
import okhttp3.OkHttpClient
import retrofit2.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CardApiService {
    @GET("/api/cards/decks")
    suspend fun getDecks(): Response<List<Deck>>

    @GET("/api/cards/decks/{deckId}")
    suspend fun getCards(
        @Path("deckId") deckId: Long,
        @Query("showArchived") showArchived: Boolean = false
    ): Response<List<Card>>

    @POST("/api/cards/{cardId}/archive")
    suspend fun archiveCard(@Path("cardId") cardId: Long)


    @GET("server/time")
    suspend fun getServerTime(): Response<Long>

    @GET("/api/cards/{cardId}")
    suspend fun getCard(@Path("cardId") cardId: Long): Response<Card>

    @PUT("/api/cards/{cardId}")
    suspend fun updateCard(@Path("cardId") cardId: Long, @Body card: Card): Response<Card>
}


object RetrofitClient {
    private const val BASE_URL = "http://172.20.10.14:8080"

    val instance: CardApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        retrofit.create(CardApiService::class.java)
    }
}