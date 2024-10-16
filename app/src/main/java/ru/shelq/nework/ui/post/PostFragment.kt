package ru.shelq.nework.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.shelq.nework.R
import ru.shelq.nework.adapter.PostAdapter
import ru.shelq.nework.adapter.PostOnInteractionListener
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.PostFragmentBinding
import ru.shelq.nework.dto.Post
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
    val viewModel: PostViewModel by activityViewModels()
    private val mediaObserver = MediaLifecycleObserver()

    companion object {
        var Bundle.text by StringArg
        var Bundle.id by idArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PostFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)
        val adapter = PostAdapter(object : PostOnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeByPost(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_postFragment_to_postEditFragment,
                    Bundle().also { it.text = post.content })
                viewModel.edit(post)
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(
                    R.id.action_postFragment_to_postDetailsFragment,
                    Bundle().also { it.id = post.id })

            }
        })



        binding.ListPostView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.SwipeRefresh.isRefreshing =
                        state.refresh is LoadState.Loading ||
                                state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.newerPostCount.collectLatest {
                binding.NewPost.isVisible = it > 0
                println(it)
            }
        }


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.ProgressBar.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        binding.NewPost.setOnClickListener {
            viewModel.readNewPosts()
            binding.NewPost.isVisible = false
            binding.ListPostView.smoothScrollToPosition(0)
        }

        binding.AddNewPostIB.setOnClickListener {
            findNavController().navigate(R.id.action_postFragment_to_postNewFragment)
        }

        binding.SwipeRefresh.setOnRefreshListener(adapter::refresh)
        return binding.root
    }

}