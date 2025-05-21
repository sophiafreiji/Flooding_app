package com.example.flood.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Pour un appareil physique, utilisez l'adresse IP de votre serveur
    private const val BASE_URL = "http://10.0.2.2:5000/"
    
    // Intercepteur pour afficher les logs des requêtes
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configuration du client HTTP avec des timeouts plus longs
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)  // Augmente le timeout de connexion
        .readTimeout(30, TimeUnit.SECONDS)     // Augmente le timeout de lecture
        .writeTimeout(30, TimeUnit.SECONDS)    // Augmente le timeout d'écriture
        .build()

    val api: FloodAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FloodAPI::class.java)
    }
}