package ru.shelq.nework.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.shelq.nework.databinding.AppActivityBinding

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = AppActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}

