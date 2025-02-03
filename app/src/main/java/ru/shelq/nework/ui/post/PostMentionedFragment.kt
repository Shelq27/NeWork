package ru.shelq.nework.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.R
import ru.shelq.nework.adapter.UserAdapter
import ru.shelq.nework.adapter.UserOnInteractionListener
import ru.shelq.nework.databinding.PostMentionedFragmentBinding
import ru.shelq.nework.dto.User
import ru.shelq.nework.ui.post.PostFragment.Companion.id
import ru.shelq.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class PostMentionedFragment : Fragment() {
    private val postViewModel: PostViewModel by viewModels(ownerProducer = ::requireActivity)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PostMentionedFragmentBinding.inflate(inflater, container, false)
        val adapter = UserAdapter(object : UserOnInteractionListener {
            override fun onUserClick(user: User) {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://userDetailsFragment?id=${user.id}".toUri())
                    .build()
               findNavController().navigate(request)
            }
        })

        binding.list.adapter = adapter

        postViewModel.mentioned.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }


        return binding.root
    }
}