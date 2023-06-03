package com.websarva.wings.android.weathercheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.websarva.wings.android.weathercheck.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}