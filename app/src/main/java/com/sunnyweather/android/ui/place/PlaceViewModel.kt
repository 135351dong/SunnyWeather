package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel: ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()//用于暂时缓存界面上的城市数据

    //返回可以观察的liveData对象
    val placeLiveData = Transformations.switchMap(searchLiveData){query->
        Repository.searchPlace(query)
    }

    fun searchPlaces(query: String){//传入搜索参数搜索地名
        searchLiveData.value=query
    }

    //存储数据相关方法
    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved() = Repository.isPlaceSaved()
}