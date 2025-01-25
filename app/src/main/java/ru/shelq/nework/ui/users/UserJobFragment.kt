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
import ru.shelq.nework.R
import ru.shelq.nework.adapter.JobAdapter
import ru.shelq.nework.adapter.JobOnInteractionListener
import ru.shelq.nework.databinding.UserJobFragmentBinding
import ru.shelq.nework.dto.Job
import ru.shelq.nework.viewmodel.JobViewModel
import ru.shelq.nework.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserJobFragment  : Fragment() {

    private val userViewModel: UserViewModel by viewModels(ownerProducer = ::requireActivity)
    @Inject
    lateinit var factory: JobViewModel.Factory

    private val jobViewModel: JobViewModel by viewModels {
        JobViewModel.provideJobViewModelFactory(
            factory,
            userViewModel.selectedUser.value!!
        )
    }
    private lateinit var binding: UserJobFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserJobFragmentBinding.inflate(layoutInflater, container, false)
        userViewModel.selectedUser.observe(viewLifecycleOwner) {user ->
            jobViewModel.loadJobs()
        }
        val adapter = JobAdapter(object : JobOnInteractionListener {

            override fun onJobDelete(job: Job) {
                jobViewModel.removeById(job)
            }

        })

        binding.list.adapter = adapter

        jobViewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.data)
        }

        jobViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        jobViewModel.loadJobs()
                    }
                    .show()
                jobViewModel.resetError()
            }
        }


        binding.swiperefresh.setOnRefreshListener {
            jobViewModel.loadJobs()
            binding.swiperefresh.isRefreshing = false
        }
        return binding.root
    }


}