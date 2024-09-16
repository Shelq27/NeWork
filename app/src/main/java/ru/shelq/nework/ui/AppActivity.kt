package ru.shelq.nework.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.shelq.nework.R
import ru.shelq.nework.databinding.AppActivityBinding
import ru.shelq.nework.viewmodel.EventViewModel
import ru.shelq.nework.viewmodel.PostViewModel

class AppActivity : AppCompatActivity() {
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var binding: AppActivityBinding
    private val PostViewModel: PostViewModel by viewModels()
    private val EventViewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = AppActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topbar = binding.AppTB
        val bottomNavigation = binding.AppBN

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment

        navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.AppBN)
            .setupWithNavController(navController)

    }
}

