package ru.shelq.nework.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.databinding.EventEditFragmentBinding
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.StringArg
import ru.shelq.nework.viewmodel.PostViewModel

class EventEditFragment : Fragment() {
    companion object {
        var Bundle.text by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = EventEditFragmentBinding.inflate(inflater, container, false)
        val bindingEdit = binding.EventEditFragment
        val viewModelPost: PostViewModel by activityViewModels()

        val text = arguments?.text
        if (text != null) {
            bindingEdit.ContentEventET.setText(text)

        }
        bindingEdit.NewEventTTB.setOnClickListener {
            findNavController().navigateUp()
        }
        bindingEdit.NewEventTTB.setOnMenuItemClickListener {
            val content = bindingEdit.ContentEventET.text.toString()
            viewModelPost.changeContentAndSave(content)
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }

        return binding.root
    }
}