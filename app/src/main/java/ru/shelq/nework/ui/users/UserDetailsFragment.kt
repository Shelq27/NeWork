package ru.shelq.nework.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.adapter.FragmentPageAdapter
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.UserDetailsFragmentBinding
import ru.shelq.nework.util.IdArg
import ru.shelq.nework.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailsFragment : Fragment() {
    @Inject
    lateinit var auth: AppAuth
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    private val userViewModel: UserViewModel by viewModels(ownerProducer = ::requireActivity)

    companion object {
        var Bundle.id by IdArg
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = UserDetailsFragmentBinding.inflate(layoutInflater, container, false)
        val collapsingToolbarLayout = binding.collapsingToolbar
        val toolbar = binding.toolbar
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setupWithNavController(
            toolbar, navController, appBarConfiguration
        )

        setupWithNavController(collapsingToolbarLayout, toolbar, navController)
        val userId = arguments?.id ?: -1
        userViewModel.selectUser(userId)
        userViewModel.data.observe(viewLifecycleOwner) { users ->
            val user = users.find { it.id == userId }

            if (user != null) {
                binding.apply {
                    Glide.with(UserAvatarIV)
                        .load(user.avatar)
                        .placeholder(R.drawable.ic_downloading_100dp)
                        .error(R.drawable.ic_error_outline_100dp)
                        .centerCrop()
                        .timeout(10_000)
                        .into(binding.UserAvatarIV)
                }
                collapsingToolbarLayout.isTitleEnabled = true
                collapsingToolbarLayout.title = "${user.name}/${user.login}"
            }

        }

        tabLayout = binding.UserDetailsTL
        viewPager2 = binding.viewPager
        adapter = FragmentPageAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tabs_wall)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tabs_jobs)))
        viewPager2.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager2) { tab, index ->
            when (index) {
                0 -> {
                    tab.text = getString(R.string.tabs_wall)
                    binding.AddNewJobsIB.visibility = View.GONE
                }

                1 -> {
                    tab.text = getString(R.string.tabs_jobs)
                    binding.AddNewJobsIB.visibility = View.VISIBLE
                }

                else -> Unit
            }

        }.attach()
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.AddNewJobsIB.visibility = View.GONE
                    }

                    1 -> {
                        binding.AddNewJobsIB.visibility =
                            if (userId == auth.authState.value.id) View.VISIBLE else View.GONE
                    }

                    else -> Unit
                }
            }
        })

        binding.toolbar.apply {
            if (userId == auth.authState.value.id) {
                menu.findItem(R.id.profile).isVisible = true
                setOnMenuItemClickListener {
                    auth.removeAuth()
                    menu.findItem(R.id.profile).isVisible = false
                    true
                }
            } else
                menu.findItem(R.id.profile).isVisible = false
        }

        binding.AddNewJobsIB.setOnClickListener {
            findNavController().navigate(R.id.jobNewFragment)
        }
        return binding.root
    }

}