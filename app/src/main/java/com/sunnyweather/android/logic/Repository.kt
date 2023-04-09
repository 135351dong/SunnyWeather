package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers

object Repository {
    //搜索城市，返回一个liveData对象
    fun searchPlace(query:String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)//联网搜索城市数据
            if (placeResponse.status == "ok"){//服务器响应成功
                val places = placeResponse.places
                Result.success(places)//包装获取的数据
            }else{
                Result.failure(java.lang.RuntimeException("response status is ${placeResponse.status}"))//包装异常信息
            }
        }catch (e:Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)//发送包装的异常信息
    }
}