package com.websarva.wings.android.weathercheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.websarva.wings.android.weathercheck.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val WEATHER_INFO_URL = "https://api.openweathermap.org/data/2.5/weather?"
        private const val API_KEY = BuildConfig.API_KEY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    @UiThread
    fun getWeatherInformation(view: View) {
        Log.d("APP_DEBUG", "getWeatherInformation開始")
        val test = "$WEATHER_INFO_URL&q=NIIGATA&appid=$API_KEY"
        lifecycleScope.launch {
            val result = requestWeatherInformation(test)
            val weather = judgeWeather(result)
        }

        Log.d("APP_DEBUG", "getWeatherInformation終了")
    }

    @WorkerThread
    private suspend fun requestWeatherInformation(url: String): String {
        Log.d("APP_DEBUG", "リクエスト開始")
        val returnVal = withContext(Dispatchers.IO) {
            var result = ""
            val url = URL(url)
            val con = url.openConnection() as? HttpURLConnection
            con?.let {
                try {
                    it.connectTimeout = 1000
                    it.readTimeout = 10000
                    it.requestMethod = "GET"
                    it.connect()
                    val stream = it.inputStream
                    result = is2String(stream)
                    stream.close()
                } catch (ex: SocketTimeoutException) {
                    Log.w("APP_DEBUG", "通信タイムアウト", ex)
                }

                it.disconnect()

            }
            result
        }
        Log.d("APP_DEBUG", "リクエスト終了")
        return returnVal
    }

    @UiThread
    private fun is2String(stream: InputStream): String {
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var line = reader.readLine()
        while (line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    @UiThread
    private fun judgeWeather(result: String) {
        Log.d("APP_DEBUG", "レスポンス処理開始")
        val rootJSON = JSONObject(result)
        val weatherJSONArray = rootJSON.getJSONArray("weather")
        val weatherJSON = weatherJSONArray.getJSONObject(0)
        val weather = weatherJSON.getString("main")
        val icon = weatherJSON.getString("icon")
        when (icon.substring(0, 2)) {
            "01", "02" -> binding.weatherImage.setImageResource(R.drawable.weather_sun)
            "03", "04" -> binding.weatherImage.setImageResource(R.drawable.weather_cloud)
            "09", "10", "11", "50" -> binding.weatherImage.setImageResource(R.drawable.weather_rain)
            "13" -> binding.weatherImage.setImageResource(R.drawable.weather_snow)
        }
        Log.d("APP_DEBUG", "${weather} : ${icon}")
        Log.d("APP_DEBUG", "レスポンス処理終了")
    }
}