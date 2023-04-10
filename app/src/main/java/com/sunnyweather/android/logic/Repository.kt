package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {
    //搜索城市，返回一个liveData对象
    fun searchPlace(query: String) = fire(Dispatchers.IO) {
//        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)//联网搜索城市数据，向服务器发送请求
            if (placeResponse.status == "ok") {//服务器响应成功
                val places = placeResponse.places
                Result.success(places)//包装获取的数据
            } else {
                Result.failure(java.lang.RuntimeException("response status is ${placeResponse.status}"))//包装异常信息
            }
//        } catch (e: Exception) {
//            Result.failure<List<Place>>(e)
//        }
//        emit(result)//发送包装的异常信息
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
            //只需要分别在两个async函数中发起网络请求，然后再分别调用它们的await()
            //方法，就可以保证只有在两个网络请求都成功响应之后，才会进一步执行程序。另外，由于
            //async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个
            //协程作用域。
            coroutineScope {//开启协程
                //根据传入的坐标异步获取实时和未来天气数据
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)//向服务器发送请求
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                }
                //保存响应结果
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                //服务器响应成功后将数据包装至result
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather =
                        Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                    Result.success(weather)
                } else {
                    Result.failure(//服务器响应失败返回错误信息
                        java.lang.RuntimeException(
                            "realtime response status is ${realtimeResponse.status}" +
                                    "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
    }

    private fun <T> fire(context: CoroutineContext, block:suspend ()->Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            }catch (e:Exception){
                Result.failure<T>(e)
            }
            emit(result)
        }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}