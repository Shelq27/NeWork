package ru.shelq.nework.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.util.IdArg
import ru.shelq.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
    val viewModel: PostViewModel by activityViewModels()
    private val mediaObserver = MediaLifecycleObserver()

    companion object {
        var Bundle.id by IdArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PostFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)
        viewModel.reset()
        viewModel.edit(null)
        val adapter = PostAdapter(object : PostOnInteractionListener {
            override fun onLike(post: Post) {
                if (appAuth.authenticated()) {
                    viewModel.likeByPost(post)
                } else {
                    AndroidUtils.showSignInDialog(this@PostFragment)
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_postFragment_to_postNewFragment,
                    Bundle().also {
                        it.id = post.id
                    })
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(
                    R.id.action_postFragment_to_postDetailsFragment,
                    Bundle().also { it.id = post.id })

            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.share))
                startActivity(shareIntent)
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
            if (appAuth.authenticated()) {
                viewModel.edit(null)
                viewModel.reset()
                findNavController().navigate(R.id.action_postFragment_to_postNewFragment)
            } else {
                AndroidUtils.showSignInDialog(this)
            }
        }

        binding.SwipeRefresh.setOnRefreshListener(adapter::refresh)

        return binding.root
    }


}