package ru.shelq.nework.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
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
import ru.shelq.nework.viewmodel.EventViewModel
import ru.shelq.nework.viewmodel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: AuthViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()
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
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

        navController = navHostFragment.navController
        val appTopBar = binding.appMTB
        setSupportActionBar(appTopBar)
        val bottomNavigation = binding.appBN



        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.postFragment,
                R.id.userFragment
                -> {
                    postViewModel.reset()
                }

                R.id.eventFragment -> {
                    eventViewModel.reset()
                }
            }

            when (destination.id) {
                R.id.postFragment,
                R.id.eventFragment,
                R.id.userFragment,
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

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.postNavigation, R.id.eventNavigation, R.id.userNavigation)

        )
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
                    supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.signInFragment)
                true
            }

            R.id.signUp -> {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.signUpFragment)
                true
            }

            R.id.signOut -> {
                appAuth.removeAuth()
                true
            }

            R.id.profile -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://userDetailsFragment?id=${viewModel.data.value!!.id}".toUri())
                    .build()
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(request)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

}