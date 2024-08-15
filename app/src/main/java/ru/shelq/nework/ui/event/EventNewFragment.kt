package ru.shelq.nework.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.databinding.EventNewFragmentBinding
import ru.shelq.nework.databinding.PostNewFragmentBinding
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.viewmodel.EventViewModel
import ru.shelq.nework.viewmodel.PostViewModel

class EventNewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModelEvent by activityViewModels<EventViewModel>()
        val binding = EventNewFragmentBinding.inflate(layoutInflater)
        binding.ContentEventET.requestFocus()
        binding.NewEventTTB.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.NewEventTTB.setOnMenuItemClickListener {
            val content = binding.ContentEventET.text.toString()
            if (content.isBlank()) {
                Toast.makeText(context, "Error : can't empty", Toast.LENGTH_LONG).show()
            }
            viewModelEvent.changeContentAndSave(content)
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }
        return binding.root
    }
}