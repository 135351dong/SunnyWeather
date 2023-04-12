package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastItemBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    lateinit var binding: ActivityWeatherBinding//绑定视图，全局调用

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }//绑定viewModel拿数据

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)//初始化binding

        //设置状态栏透明
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }

        setContentView(binding.root)//加载布局

        //将intent的相关信息赋值到WeatherViewModel的相应变量中
        if (viewModel.locationLng.isEmpty())
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""//获取上一个activity传递的数据
        if (viewModel.locationLat.isEmpty())
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        if (viewModel.placeName.isEmpty())
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""

        //对weatherLiveData进行观察
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) showWeatherInfo(weather)//显示天气信息
            else Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
            binding.swipeRefresh.isRefreshing = false//检测到数据刷新关闭进度条
        })
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)//设置加载图标颜色
        refreshWeather()//自动刷新一次天气数据用于观察
        binding.swipeRefresh.setOnRefreshListener {//下拉刷新天气
            refreshWeather()
        }
        //viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)//刷新一次天气数据便于观察

        //开启滑动窗口
        binding.now.navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        //观察滑动窗口行为
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {//在窗口关闭时关闭输入法
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })
    }

    fun refreshWeather() {//刷新天气方法
        viewModel.refreshWeather(
            viewModel.locationLng,
            viewModel.locationLat
        )//调用WeatherViewModel中的refreshWeather()方法
        binding.swipeRefresh.isRefreshing = true//显示下方刷新进度条，在检测到数据变化时消失
    }

    //将天气信息填入到相应的xml布局中并显示出来
    private fun showWeatherInfo(weather: Weather) {

        val realtime = weather.realtime
        val daily = weather.daily

        //填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()}℃"
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        binding.now.placeName.text = viewModel.placeName
        binding.now.currentTemp.text = currentTempText
        binding.now.currentSky.text = getSky(realtime.skycon).info
        binding.now.currentAQI.text = currentPM25Text
        binding.now.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        //填充forecast.xml布局中的数据
        binding.forecast.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {//对未来今天循环
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
//            val view = LayoutInflater.from(this)
//                .inflate(R.layout.forecast_item, binding.forecast.forecastLayout, false)
            val forecastItemBinding = ForecastItemBinding.inflate(
                LayoutInflater.from(this),
                binding.forecast.forecastLayout,
                false
            )
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sky = getSky(skycon.value)
            val tempText = "${temperature.min.toInt()}~${temperature.max.toInt()}℃"
            forecastItemBinding.dateInfo.text = simpleDateFormat.format(skycon.date)
            forecastItemBinding.skyInfo.text = sky.info
            forecastItemBinding.skyIcon.setImageResource(sky.icon)
            forecastItemBinding.temperatureInfo.text = tempText
            binding.forecast.forecastLayout.addView(forecastItemBinding.root)
        }

        //填充life_index.xml布局中的数据，当天的生活数据
        val lifeIndex = daily.lifeIndex
        binding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc//感冒风险
        binding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc//穿衣
        binding.lifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc//紫外线强度
        binding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE//将scrollView设为可见状态
    }
}