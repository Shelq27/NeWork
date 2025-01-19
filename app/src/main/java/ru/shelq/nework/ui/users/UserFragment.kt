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
import ru.shelq.nework.adapter.PostOnInteractionListener
import ru.shelq.nework.adapter.UserAdapter
import ru.shelq.nework.databinding.UserFragmentBinding
import ru.shelq.nework.dto.User
import ru.shelq.nework.viewmodel.UserViewModel

@AndroidEntryPoint
class UserFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = UserFragmentBinding.inflate(inflater, container, false)
        val adapter = UserAdapter(object : PostOnInteractionListener {
            override fun onUserClick(user: User) {
                TODO()
//                Переход на детальную карточку юзера
            }

        }, requireContext())

        binding.list.adapter = adapter

        userViewModel.data.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        userViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { userViewModel.loadUsers() }
                    .show()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            userViewModel.loadUsers()
            binding.swiperefresh.isRefreshing = false
        }
        return binding.root
    }

}