package com.mlkit.demo.kotlin.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mlkit.demo.BuildConfig
import com.mlkit.demo.kotlin.network.RetrofitFactory.createService
import com.mlkit.demo.kotlin.service.VisionLabsService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitFactory {

    inline fun <reified T> createService(debug: Boolean, baseUrl: String): T {
        val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val interceptor = (Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.header("Luna-Account-ID", "6d071cca-fda5-4a03-84d5-5bea65904480")
            requestBuilder.header("Content-Type", "image/jpeg")
            chain.proceed(requestBuilder.build())
        })
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(interceptor)

        if (debug) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.HEADERS
            logging.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(logging)
        }
        return Retrofit.Builder()
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(baseUrl)
            .client(httpClient.build())
            .build()
            .create(T::class.java)
    }
}

data class NetworkResult<R>(val status: Status, val data: R?, val message: String?) {
    companion object {
        fun <R> success(data: R?): NetworkResult<R> {
            return NetworkResult(Status.SUCCESS, data, null)
        }

        fun <R> error(errorMessage: String?): NetworkResult<R> {
            return NetworkResult(Status.ERROR, null, errorMessage)
        }

        fun <R> loading(): NetworkResult<R> {
            return NetworkResult(Status.LOADING, null, null)
        }
    }
}


object UserService {

    val visionImageService = createService<VisionLabsService>(BuildConfig.DEBUG, BASE_URL)
}

const val BASE_URL = "http://10.180.11.157:5000/6/handlers/4d60b7dd-8ab4-4a57-8fe3-e1abebee9e28/"

enum class Status {
    SUCCESS, ERROR, LOADING
}