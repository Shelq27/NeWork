package ru.shelq.nework.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.adapter.UserAdapter
import ru.shelq.nework.adapter.UserOnInteractionListener
import ru.shelq.nework.databinding.UserFragmentBinding
import ru.shelq.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class MentionedFragment : Fragment() {
    private val postViewModel: PostViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = UserFragmentBinding.inflate(inflater, container, false)
        val adapter = UserAdapter(object : UserOnInteractionListener {
        })

        binding.list.adapter = adapter

        postViewModel.mentioned.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }
        return binding.root
    }
}