package ru.shelq.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.shelq.nework.R
import ru.shelq.nework.databinding.EventCardBinding
import ru.shelq.nework.dto.Event

interface EventOnInteractionListener {
    fun onLike(event: Event) {}
    fun onRemove(event: Event) {}
    fun onEdit(event: Event) {}
    fun onOpen(event: Event) {}

}

class EventAdapter(
    private val onInteractionListener: EventOnInteractionListener
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
}


class EventViewHolder(
    private val binding: EventCardBinding,
    private val onInteractionListener: EventOnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        binding.apply {
            AuthorTV.text = event.author
            DateEventPostTV.text = event.published
            TextEventTV.text = event.content
            TextEventTV.setOnClickListener {
                onInteractionListener.onOpen(event)
            }
            LinkEventTV.text = event.type
            LikeIB.text = event.likeOwnerIds.toString()
            LikeIB.isChecked = event.likedByMe
            LikeIB.setOnClickListener {
                onInteractionListener.onLike(event)
            }
            MenuIB.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_card)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(event)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }


}