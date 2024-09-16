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
import ru.shelq.nework.adapter.EventAdapter
import ru.shelq.nework.adapter.EventOnInteractionListener
import ru.shelq.nework.databinding.EventFragmentBinding
import ru.shelq.nework.dto.Event
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.EventViewModel


class EventFragment : Fragment() {
    companion object {
        var Bundle.text by StringArg
        var Bundle.id by idArg

    }

    val viewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = EventFragmentBinding.inflate(inflater, container, false)

        val adapter = EventAdapter(object : EventOnInteractionListener {
            override fun onLike(event: Event) {
                viewModel.likeByEvent(event)
            }

            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onEdit(event: Event) {
                findNavController().navigate(
                    R.id.action_postFragment_to_postEditFragment,
                    Bundle().also { it.text = event.content })
                viewModel.edit(event)
            }

            override fun onOpen(event: Event) {
                findNavController().navigate(
                    R.id.action_postFragment_to_postDetailsFragment,
                    Bundle().also { it.id = event.id })

            }
        })

        binding.ListEventView.adapter = adapter

        viewModel.newerEventCount.observe(viewLifecycleOwner) {
            binding.NewEvent.isVisible = it > 0
            println(it)
        }

        binding.NewEvent.setOnClickListener {
            viewModel.readNewEvents()
            binding.NewEvent.isVisible = false
            binding.ListEventView.smoothScrollToPosition(0)
        }
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.data)

        }
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.ProgressBar.isVisible = state.loading
            binding.SwipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadEvent() }
                    .show()
            }
        }
        binding.SwipeRefresh.setOnRefreshListener {
            viewModel.refreshEvents()
        }
        binding.AddNewEventIB.setOnClickListener {
            findNavController().navigate(R.id.action_postFragment_to_postNewFragment)

        }
        return binding.root
    }

}