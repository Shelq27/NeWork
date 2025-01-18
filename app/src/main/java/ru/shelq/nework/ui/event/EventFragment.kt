package ru.shelq.nework.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ru.shelq.nework.adapter.EventAdapter
import ru.shelq.nework.adapter.EventOnInteractionListener
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.databinding.EventFragmentBinding
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Post
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventFragment : Fragment() {
    companion object {
        var Bundle.text by StringArg
        var Bundle.id by idArg

    }
    @Inject
    lateinit var appAuth: AppAuth
    private val mediaObserver = MediaLifecycleObserver()
    val viewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EventFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)
        val adapter = EventAdapter(object : EventOnInteractionListener {
            override fun onLike(event: Event) {
                viewModel.likeByEvent(event)
            }

            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onEdit(event: Event) {
                findNavController().navigate(
                    R.id.action_eventFragment_to_eventEditFragment,
                    Bundle().also { it.text = event.content })
                viewModel.edit(event)
            }

            override fun onOpen(event: Event) {
                findNavController().navigate(
                    R.id.action_eventFragment_to_eventDetailsFragment,
                    Bundle().also { it.id = event.id })

            }

            override fun onShare(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.share))
                startActivity(shareIntent)
            }

        })

        binding.ListEventView.adapter = adapter

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
            viewModel.newerEventCount.collectLatest {
                binding.NewEvent.isVisible = it > 0
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

        binding.NewEvent.setOnClickListener {
            viewModel.readNewEvents()
            binding.NewEvent.isVisible = false
            binding.ListEventView.smoothScrollToPosition(0)
        }

        binding.AddNewEventIB.setOnClickListener {
            findNavController().navigate(R.id.action_eventFragment_to_eventNewFragment)
        }

        binding.SwipeRefresh.setOnRefreshListener(adapter::refresh)
        return binding.root
    }

}