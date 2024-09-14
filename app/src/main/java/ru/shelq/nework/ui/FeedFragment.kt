package ru.shelq.nework.ui


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
import ru.shelq.nework.adapter.EventAdapter
import ru.shelq.nework.adapter.EventOnInteractionListener
import ru.shelq.nework.adapter.PostAdapter
import ru.shelq.nework.adapter.PostOnInteractionListener
import ru.shelq.nework.databinding.FeedFragmentBinding
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Post
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.EventViewModel
import ru.shelq.nework.viewmodel.PostViewModel

class FeedFragment : Fragment() {
    companion object {
        var Bundle.text by StringArg
        var Bundle.id by idArg

    }

    val viewModelPost: PostViewModel by activityViewModels()
    val viewModelEvent: EventViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FeedFragmentBinding.inflate(inflater, container, false)

        val adapterPost = PostAdapter(object : PostOnInteractionListener {
            override fun onLike(post: Post) {
                viewModelPost.likeByPost(post)
            }

            override fun onRemove(post: Post) {
                viewModelPost.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_postEditFragment,
                    Bundle().also { it.text = post.content })
                viewModelPost.edit(post)
            }

            override fun onOpen(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postDetailsFragment,
                    Bundle().also { it.id = post.id })

            }

        })

        val adapterEvent = EventAdapter(object : EventOnInteractionListener {
            override fun onLike(event: Event) {
                viewModelEvent.likeByEvent(event)
            }

            override fun onRemove(event: Event) {
                viewModelEvent.removeById(event.id)
            }

            override fun onEdit(event: Event) {
                findNavController().navigate(R.id.action_feedFragment_to_eventEditFragment,
                    Bundle().also { it.text = event.content })
                viewModelEvent.edit(event)
            }

            override fun onOpen(event: Event) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_eventDetailsFragment,
                    Bundle().also { it.id = event.id })

            }
        })

        binding.listAppFragment.adapter = adapterPost

        when (binding.AppBN.selectedItemId) {
            R.id.Posts -> {
                viewModelPost.newerPostCount.observe(viewLifecycleOwner){
                    binding.NewItem.isVisible = it > 0
                    println(it)
                }
                viewModelPost.data.observe(viewLifecycleOwner) { state ->
                    adapterPost.submitList(state.data)

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
            }

            R.id.Events -> {
                viewModelEvent.newerEventCount.observe(viewLifecycleOwner){
                    binding.NewItem.isVisible = it > 0
                    println(it)
                }
                viewModelEvent.data.observe(viewLifecycleOwner) { state ->
                    adapterEvent.submitList(state.data)

                }
                viewModelEvent.dataState.observe(viewLifecycleOwner) { state ->
                    binding.ProgressBar.isVisible = state.loading
                    binding.SwipeRefresh.isRefreshing = state.refreshing
                    if (state.error) {
                        Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry_loading) { viewModelEvent.loadEvent() }
                            .show()
                    }
                }
            }

            R.id.Users -> {
                TODO()
            }
        }


        binding.NewItem.setOnClickListener {
            when (binding.AppBN.selectedItemId) {
                R.id.Posts -> {
                    viewModelPost.readNewPosts()

                }

                R.id.Events -> {
                    viewModelEvent.readNewEvents()
                }

                R.id.Users -> {
                    TODO()
                }
            }
            binding.NewItem.isVisible = false
            binding.listAppFragment.smoothScrollToPosition(0)
        }


        binding.SwipeRefresh.setOnRefreshListener {
            when (binding.AppBN.selectedItemId) {
                R.id.Posts -> {
                    viewModelPost.refreshPosts()
                }

                R.id.Events -> {
                    viewModelEvent.refreshEvents()
                }

                R.id.Users -> {
                    TODO()
                }
            }
            viewModelPost.refreshPosts()
        }





        binding.AppBN.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Posts -> {
                    binding.listAppFragment.adapter = adapterPost
                    true
                }

                R.id.Events -> {
                    binding.listAppFragment.adapter = adapterEvent
                    true
                }

                else -> false
            }
        }
        binding.AddNewPostEventIB.setOnClickListener {
            when (binding.AppBN.selectedItemId) {
                R.id.Posts -> {
                    findNavController().navigate(R.id.action_feedFragment_to_postNewFragment)
                }

                R.id.Events -> {
                    findNavController().navigate(R.id.action_feedFragment_to_eventNewFragment)
                }

                R.id.Users -> {
                    TODO()
                }
            }
        }

        return binding.root
    }
}