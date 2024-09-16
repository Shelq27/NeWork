package ru.shelq.nework.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ru.shelq.nework.R
import ru.shelq.nework.databinding.AppActivityBinding

class AppActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: AppActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = AppActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment

        navController = navHostFragment.navController
        val appTopBar = binding.AppTB
        val bottomNavigation = binding.AppBN

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.postNavigation, R.id.eventNavigation, R.id.userNavigation)
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//                R.id.postFragment -> navController.navigate(R.id.postNavigation)
//                R.id.eventFragment -> navController.navigate(R.id.eventNavigation)
//            }

            when (destination.id) {
                R.id.postFragment,
                R.id.eventFragment,
                -> {

                    bottomNavigation.isVisible = true
                    appTopBar.isVisible = true
                }

                else -> {
                    bottomNavigation.isGone = true
                    appTopBar.isGone = true
                }
            }

        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigation.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

}

