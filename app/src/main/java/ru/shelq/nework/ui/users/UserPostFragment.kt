package ru.shelq.nework.ui.users

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
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
import ru.shelq.nework.viewmodel.UserViewModel
import ru.shelq.nework.viewmodel.WallViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserPostFragment : Fragment() {
    @Inject
    lateinit var auth: AppAuth
    private val userViewModel: UserViewModel by viewModels(ownerProducer = ::requireActivity)

    @Inject
    lateinit var factory: WallViewModel.Factory
    private val wallViewModel: WallViewModel by viewModels {
        WallViewModel.provideWallViewModelFactory(
            factory,
            userViewModel.selectedUser.value!!
        )
    }
    private lateinit var binding: PostFragmentBinding
    private val mediaObserver = MediaLifecycleObserver()
    private var postPlaying: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PostFragmentBinding.inflate(layoutInflater, container, false)

        val adapter = PostAdapter(object : PostOnInteractionListener {
            override fun onEdit(post: Post) {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://newPostFragment?longArg=${post.id}".toUri())
                    .build()
                findNavController().navigate(request)
            }

            override fun onLike(post: Post) {
                if (auth.authenticated()) {
                    wallViewModel.likeByPost(post)
                } else AndroidUtils.showSignInDialog(this@UserPostFragment)
            }

            override fun onRemove(post: Post) {
                wallViewModel.removeById(post.id)
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

            override fun onOpen(post: Post) {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://postDetailsFragment?id=${post.id}".toUri())
                    .build()
                findNavController().navigate(request)
            }

            override fun onPlayAudio(post: Post, seekBar: SeekBar, playAudio: ImageButton) {
                mediaObserver.playAudio(post.attachment!!, seekBar, playAudio)
                postPlaying = post
            }


        })

        binding.ListPostView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wallViewModel.data.collectLatest(adapter::submitData)
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

        wallViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
                wallViewModel.resetError()
            }
        }
        binding.AddNewPostIB.visibility = View.GONE
        binding.SwipeRefresh.setOnRefreshListener(adapter::refresh)
        return binding.root
    }


}