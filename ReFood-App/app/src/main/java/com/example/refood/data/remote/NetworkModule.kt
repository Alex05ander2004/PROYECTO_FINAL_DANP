package com.example.refood.data.remote

import com.example.refood.BuildConfig
import com.example.refood.data.remote.dto.RefreshRequest
import com.example.refood.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente de red para Auth. El refresh de token usa un cliente "plano" sin el
 * interceptor/authenticator de abajo, para no disparar el propio Authenticator
 * en bucle cuando el refresh falla.
 */
class NetworkModule(private val sessionManager: SessionManager) {

    private val apiBaseUrl = "${BuildConfig.API_BASE_URL}api/"

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val plainRetrofit = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .client(OkHttpClient.Builder().addInterceptor(loggingInterceptor()).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val refreshApi: AuthApi = plainRetrofit.create(AuthApi::class.java)

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { sessionManager.getAccessToken() }
        val request = if (token != null) {
            chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val authenticator = Authenticator { _, response ->
        if (responseChainLength(response) >= 2) return@Authenticator null

        val refreshToken = runBlocking { sessionManager.getRefreshToken() } ?: return@Authenticator null
        runCatching {
            val newAccess = runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)).access }
            runBlocking { sessionManager.setTokens(newAccess, refreshToken) }
            response.request.newBuilder().header("Authorization", "Bearer $newAccess").build()
        }.getOrNull()
    }

    private fun responseChainLength(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private val authenticatedClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(authenticator)
        .addInterceptor(loggingInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .client(authenticatedClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val productApi: ProductApi = retrofit.create(ProductApi::class.java)
    val cartApi: CartApi = retrofit.create(CartApi::class.java)
    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
}
