package ru.shelq.nework.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.databinding.EventDetailsFragmentBinding
import ru.shelq.nework.util.AndroidUtils.loadImgCircle
import ru.shelq.nework.util.idArg
import ru.shelq.nework.viewmodel.EventViewModel

class EventDetailsFragment : Fragment() {
    companion object {
        var Bundle.id by idArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = EventDetailsFragmentBinding.inflate(inflater, container, false)
        val viewModelEvent: EventViewModel by activityViewModels()
        val eventId = arguments?.id ?: -1
        viewModelEvent.getEventById(eventId)
        viewModelEvent.selectedEvent.observe(viewLifecycleOwner) { event ->
            if (event != null)
                binding.apply {

                    AuthorTV.text = event.author
                    AvatarIV.loadImgCircle(event.authorAvatar)
                    TextEventTV.text = event.content
                    DateEventTV.text = event.datetime
                    EventDetailsMTB.setNavigationOnClickListener {
                        findNavController().navigateUp()
                    }

                    LikeB.text = event.likeOwnerIds.toString()


                }
        }
        return binding.root
    }
}