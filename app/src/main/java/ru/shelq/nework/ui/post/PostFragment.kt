package ru.shelq.nework.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.shelq.nework.R
import ru.shelq.nework.adapter.PostAdapter
import ru.shelq.nework.adapter.PostOnInteractionListener
import ru.shelq.nework.databinding.PostFragmentBinding
import ru.shelq.nework.dto.Post
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.PostViewModel

class PostFragment : Fragment() {
    companion object {
        var Bundle.text by StringArg
        var Bundle.id by idArg

    }

    val viewModelPost: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = PostFragmentBinding.inflate(inflater, container, false)

        val adapter = PostAdapter(object : PostOnInteractionListener {
            override fun onLike(post: Post) {
                viewModelPost.likeByPost(post)
            }

            override fun onRemove(post: Post) {
                viewModelPost.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_postFragment_to_postEditFragment,
                    Bundle().also { it.text = post.content })
                viewModelPost.edit(post)
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(
                    R.id.action_postFragment_to_postDetailsFragment,
                    Bundle().also { it.id = post.id })

            }
        })

        binding.ListPostView.adapter = adapter

        viewModelPost.newerPostCount.observe(viewLifecycleOwner){
            binding.NewPost.isVisible = it > 0
            println(it)
        }

        binding.NewPost.setOnClickListener {
            viewModelPost.readNewPosts()
            binding.NewPost.isVisible = false
            binding.ListPostView.smoothScrollToPosition(0)
        }
        viewModelPost.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.data)

        }
        viewModelPost.dataState.observe(viewLifecycleOwner) { state ->
            binding.ProgressBar.isVisible = state.loading
            binding.SwipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModelPost.loadPost() }
                    .show()
            }
        }
        binding.AddNewPostIB.setOnClickListener {
            findNavController().navigate(R.id.action_postFragment_to_postNewFragment)

        }
        return binding.root
    }

}