package ru.shelq.nework.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.AppActivityBinding
import ru.shelq.nework.viewmodel.AuthViewModel
import javax.inject.Inject


@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: AppActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment

        navController = navHostFragment.navController
        val appTopBar = binding.AppMTB
        setSupportActionBar(appTopBar)
        val bottomNavigation = binding.AppBN



        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.postNavigation, R.id.eventNavigation, R.id.userNavigation)

        )
        navController.addOnDestinationChangedListener { _, destination, _ ->


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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app_top_bar, menu)

        menu?.let {
            it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signIn -> {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.signInFragment)
                true
            }

            R.id.signUp -> {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.NavHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.signUpFragment)
                true
            }

            R.id.signOut -> {
                appAuth.removeAuth()
                true
            }

            R.id.profile -> {
                // TODO()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

}