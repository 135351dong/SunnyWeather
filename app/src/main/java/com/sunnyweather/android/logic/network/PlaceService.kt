package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")//当调用服务时向该地址发送get请求
    fun searchPlaces(@Query("query") query:String):retrofit2.Call<PlaceResponse>//将服务器返回的JSON数据自动解析为PlaceResponse对象
}