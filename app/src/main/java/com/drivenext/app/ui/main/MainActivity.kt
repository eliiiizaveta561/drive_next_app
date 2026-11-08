package com.drivenext.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drivenext.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
    }
}
