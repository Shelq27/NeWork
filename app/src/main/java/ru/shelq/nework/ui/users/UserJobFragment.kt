package ru.shelq.nework.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import ru.shelq.nework.R
import ru.shelq.nework.adapter.JobAdapter
import ru.shelq.nework.adapter.JobOnInteractionListener
import ru.shelq.nework.databinding.UserJobFragmentBinding
import ru.shelq.nework.dto.Jobs
import ru.shelq.nework.viewmodel.JobViewModel
import ru.shelq.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class UserJobFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels(ownerProducer = ::requireActivity)

    private val jobViewModel: JobViewModel by viewModels(

        extrasProducer = {

            defaultViewModelCreationExtras.withCreationCallback<JobViewModel.Factory> { factory ->

                factory.create(requireNotNull(userViewModel.selectedUser.value))

            }

        }

    )
    private lateinit var binding: UserJobFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserJobFragmentBinding.inflate(layoutInflater, container, false)

        userViewModel.selectedUser.observe(viewLifecycleOwner) {
            jobViewModel.loadJobs()
        }
        val adapter = JobAdapter(object : JobOnInteractionListener {

            override fun onJobDelete(jobs: Jobs) {
                jobViewModel.removeById(jobs)
            }

        })

        binding.list.adapter = adapter

        jobViewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.data)
        }

        jobViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        jobViewModel.loadJobs()
                    }
                    .show()
                jobViewModel.resetError()
            }
        }


        binding.swipeRefresh.setOnRefreshListener {
            jobViewModel.loadJobs()
            binding.swipeRefresh.isRefreshing = false
        }
        return binding.root
    }


}