package ru.shelq.nework.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.shelq.nework.adapter.UserAdapter
import ru.shelq.nework.adapter.UserOnInteractionListener
import ru.shelq.nework.databinding.EventLikersFragmentBinding
import ru.shelq.nework.viewmodel.EventViewModel

@AndroidEntryPoint
class EventLikersFragment : Fragment() {
    private val eventViewModel: EventViewModel by viewModels(ownerProducer = ::requireActivity)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EventLikersFragmentBinding.inflate(inflater, container, false)
        val adapter = UserAdapter(object : UserOnInteractionListener {
        })

        binding.list.adapter = adapter

        eventViewModel.likers.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }
        return binding.root
    }
}