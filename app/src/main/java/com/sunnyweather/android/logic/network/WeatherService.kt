package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {
    @GET("v2.6/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String)://使用@Path注解向请求接口中动态传入经纬度的坐标
            retrofit2.Call<RealtimeResponse>

    @GET("v2.6/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily.json")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            retrofit2.Call<DailyResponse>
}