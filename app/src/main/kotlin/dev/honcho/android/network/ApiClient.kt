package dev.honcho.android.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.honcho.android.BuildConfig
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    private var _service: HonchoService? = null
    private var currentBaseUrl = ""
    private var currentToken = ""

    fun service(baseUrl: String, token: String): HonchoService {
        if (_service == null || baseUrl != currentBaseUrl || token != currentToken) {
            currentBaseUrl = baseUrl
            currentToken = token
            _service = buildService(baseUrl, token)
        }
        return _service!!
    }

    fun invalidate() {
        _service = null
        currentBaseUrl = ""
        currentToken = ""
    }

    private fun buildService(baseUrl: String, token: String): HonchoService {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val clientBuilder = OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Accept", "application/json")
                        .build()
                )
            }

        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(clientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HonchoService::class.java)
    }
}
