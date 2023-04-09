package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import retrofit2.http.Query
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {
    private val placeService = ServiceCreator.create<PlaceService>()//创建PlaceService的动态代理对象

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()//发送城市数据请求

    private suspend fun <T> Call<T>.await():T{//声明挂起函数
        return suspendCoroutine { continuation ->//挂起协程
            enqueue(object :Callback<T>{//开启Http请求，返回结果到callback中
                override fun onResponse(call: Call<T>,response: Response<T>){
                    val body = response.body()
                    if(body!=null)continuation.resume(body)//恢复协程
                    else continuation.resumeWithException(
                        java.lang.RuntimeException("response body is null"))
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}