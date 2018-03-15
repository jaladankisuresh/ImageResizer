package com.imnotout.app.NetworkIO

import android.util.Log
import android.webkit.CookieManager
import com.imnotout.imageresizer.LOG_APP_TAG
import com.imnotout.imageresizer.NetworkIO.StreamRequestBody
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import okhttp3.*
import java.io.InputStream
import java.util.*
import okhttp3.RequestBody
import okhttp3.MultipartBody
import java.text.SimpleDateFormat


typealias AppMediaType = com.imnotout.imageresizer.Models.MediaType
const val BASE_WEB_API_URL = "http://192.168.11.9:3000/"
//const val BASE_WEB_API_URL = "http://10.0.2.2:3000/"
//const val BASE_WEB_API_URL = "https://imnotout.com/api/"

class HttpService {
    companion object {
//        val httpClient = OkHttpClient()
        val httpClient = OkHttpClient.Builder()
        .cookieJar(WebviewCookieHandler())
        .build()
        suspend fun get(uri: String) : String {
            Log.i(LOG_APP_TAG, "HttpService WebAPI request timestamp in milli seconds :${System.currentTimeMillis()}")
            val request = Request.Builder()
                    .url(uri)
                    .build()
            val response =  makeIORequest(request).await()
            Log.i(LOG_APP_TAG, "HttpService WebAPI response timestamp in milli seconds :${System.currentTimeMillis()}")
            return response
        }
        suspend fun post(url: String, jsonBody: String) : String {
            val JsonType = MediaType.parse("application/json; charset=utf-8")
            val requestBody = RequestBody.create(JsonType, jsonBody)
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
            return makeIORequest(request).await()
        }
//        https://github.com/square/okhttp/wiki/Recipes
        suspend fun post(url: String, mediaType: AppMediaType, stream: InputStream) : String {
            val okhttpMediaType = MediaType.parse(mediaType.value)
//            val requestBody = StreamRequestBody(ImageType, stream)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val mediaFileName = when(mediaType) {
                AppMediaType.IMAGE -> "${timeStamp}_IMAGE.jpg"
                else -> "${timeStamp}_VIDEO.mp4"
            }
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", mediaFileName,
                        StreamRequestBody(okhttpMediaType, stream))
                .build()
            val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
            return makeIORequest(request).await()
        }
        private suspend fun makeIORequest(request: Request) : Call = async(CommonPool) {
            // Runs in background
            httpClient.newCall(request)
        }.await()
    }
// Cookie jar that handles syncing okhttp cookies with Webview cookie manager
// https://gist.github.com/justinthomas-syncbak/cd29feebd6837d5b45f6576c73faedac
    class WebviewCookieHandler : CookieJar {
        private val webviewCookieManager = CookieManager.getInstance()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val urlString = url.toString()
            cookies.forEach { webviewCookieManager.setCookie(urlString, it.toString()) }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val urlString = url.toString()
            val cookiesString = webviewCookieManager.getCookie(urlString)

            if(cookiesString == null) return Collections.emptyList()
            return cookiesString.split(";").mapNotNull { Cookie.parse(url, it) }
        }
    }
}