package id.andre002wp.ReceiptScanner.Backend

import androidx.viewbinding.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class ApiConfig {
    companion object {
        fun getApiService(): ApiService {
            val loggingInterceptor = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
            }

            // trial
            val stringInterceptor = Interceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                val source = response.body?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer()
                var responseString = buffer?.clone()?.readString(Charset.forName("UTF-8"))
                if (responseString != null && responseString.length > 2) {
                    val lastTwo = responseString.takeLast(2)
                    if (lastTwo != "}}") {
                        val lastOne = responseString.takeLast(1)
                        responseString = if (lastOne != "}") {
                            "$responseString}}"
                        } else {
                            "$responseString}"
                        }
                    }
                }
                val contentType = response.body?.contentType()
                val body = ResponseBody.create(contentType, responseString ?: "")
                return@Interceptor response.newBuilder().body(body).build()
            }

            val client = OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()
            val retrofit = Retrofit.Builder().baseUrl("http://192.168.191.1:8080/")
                .addConverterFactory(GsonConverterFactory.create()).client(client).build()
            return retrofit.create(ApiService::class.java)
        }

        fun getBackupApiService(): ApiService {
            val loggingInterceptor = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
            }

            // trial
            val stringInterceptor = Interceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                val source = response.body?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer()
                var responseString = buffer?.clone()?.readString(Charset.forName("UTF-8"))
                if (responseString != null && responseString.length > 2) {
                    val lastTwo = responseString.takeLast(2)
                    if (lastTwo != "}}") {
                        val lastOne = responseString.takeLast(1)
                        responseString = if (lastOne != "}") {
                            "$responseString}}"
                        } else {
                            "$responseString}"
                        }
                    }
                }
                val contentType = response.body?.contentType()
                val body = ResponseBody.create(contentType, responseString ?: "")
                return@Interceptor response.newBuilder().body(body).build()
            }

            val client = OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()
            val retrofit = Retrofit.Builder().baseUrl("http://192.168.191.1:8080/")
                .addConverterFactory(GsonConverterFactory.create()).client(client).build()
            return retrofit.create(ApiService::class.java)
        }
    }
}