package ru.shelq.nework.ui.event

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
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.IdArg
import ru.shelq.nework.util.MediaLifecycleObserver
import ru.shelq.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventFragment : Fragment() {


    @Inject
    lateinit var appAuth: AppAuth
    private val mediaObserver = MediaLifecycleObserver()
    val viewModel: EventViewModel by activityViewModels()

    companion object {
        var Bundle.id by IdArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EventFragmentBinding.inflate(inflater, container, false)
        lifecycle.addObserver(mediaObserver)

        val adapter = EventAdapter(object : EventOnInteractionListener {
            override fun onLike(event: Event) {
                if (appAuth.authenticated()) {
                    viewModel.likeByEvent(event)
                } else {
                    AndroidUtils.showSignInDialog(this@EventFragment)
                }
            }

            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onEdit(event: Event) {
                viewModel.edit(event)
                findNavController().navigate(
                    R.id.action_eventFragment_to_eventNewFragment,
                    Bundle().also { it.id = event.id })
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

            override fun onParticipate(event: Event) {
                if (appAuth.authenticated()) {
                    viewModel.participateByEvent(event)
                } else {
                    AndroidUtils.showSignInDialog(this@EventFragment)
                }
            }

        })

        binding.listEventView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefresh.isRefreshing =
                        state.refresh is LoadState.Loading ||
                                state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.newerEventCount.collectLatest {
                binding.newEvent.isVisible = it > 0
            }
        }


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        binding.newEvent.setOnClickListener {
            viewModel.readNewEvents()
            binding.newEvent.isVisible = false
            binding.listEventView.smoothScrollToPosition(0)
        }

        binding.addNewEventIB.setOnClickListener {
            if (appAuth.authenticated()) {
                viewModel.edit(null)
                viewModel.reset()
                findNavController().navigate(R.id.action_eventFragment_to_eventNewFragment)
            } else {
                AndroidUtils.showSignInDialog(this)
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)
        return binding.root
    }

}