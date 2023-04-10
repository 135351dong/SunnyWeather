package com.sunnyweather.android.ui.place

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.databinding.FragmentPlaceBinding
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment:Fragment() {
    //定义binding方便全局使用，正常binding变量只有在onCreateView与onDestroyView才是可用的
    private var _binding : FragmentPlaceBinding? =null

    private val binding get() = _binding!!

    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }//被调用时获取当前ViewModel

    private lateinit var adapter: PlaceAdapter//延迟初始化

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBinding.inflate(layoutInflater,container,false)//进行视图绑定
        //val binding = _binding
        return binding.root


        //return inflater.inflate(R.layout.fragment_place,container,false)//加载fragment_place布局
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().lifecycle.addObserver(object :DefaultLifecycleObserver{//开启附属activity的生命周期监听
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)

            //储存搜索信息，避免每次进入都要重新搜索
            if (viewModel.isPlaceSaved()){
                val place = viewModel.getSavedPlace()
                val intent = Intent(context,WeatherActivity::class.java).apply {
                    putExtra("location_lng",place.location.lng)
                    putExtra("location_lat",place.location.lat)
                    putExtra("place_name",place.name)
                }
                startActivity(intent)
                activity?.finish()
                return
            }

                val layoutManager = LinearLayoutManager(activity)
                binding.recyclerView.layoutManager = layoutManager//定义布局管理器（线性布局）
                adapter = PlaceAdapter(this@PlaceFragment,viewModel.placeList)//配置适配器，将PlaceFragment与place_item绑定
                binding.recyclerView.adapter = adapter
                binding.searchPlaceEdit.addTextChangedListener {editable ->//监听搜索框数据变化，并随之做出ui更新
                    val content = editable.toString()
                    if(content.isNotEmpty()){
                        //Log.d("input","$editable")
                        viewModel.searchPlaces(content)//根据传入数据搜索地点，发起搜索城市数据的网络请求
                    }else{
                        binding.recyclerView.visibility = View.GONE//布局不可见，并取消占用空间重新布局
                        binding.bgImageView.visibility = View.VISIBLE//显示背景图
                        viewModel.placeList.clear()//删除缓存
                        adapter.notifyDataSetChanged()//通知数据变化，ui更新
                    }
                }
            //获取服务器响应的数据，并观察placeLiveData的变化并执行相关逻辑
            viewModel.placeLiveData.observe(this@PlaceFragment, Observer { result->
                val places = result.getOrNull()
                if(places!=null){//如果搜索结果不为空
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.bgImageView.visibility = View.GONE
                    //重置placeList
                    viewModel.placeList.clear()
                    viewModel.placeList.addAll(places)
                    adapter.notifyDataSetChanged()//通知数据变化，ui更新
                }else{
                    Toast.makeText(activity,"未能查询到地点",Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            })
            owner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null//binding置空，防止内存泄露。原因：fragment生命周期大于activity
    }
}