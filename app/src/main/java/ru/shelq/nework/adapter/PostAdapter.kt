package ru.shelq.nework.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.shelq.nework.R
import ru.shelq.nework.databinding.PostCardBinding
import ru.shelq.nework.dto.Post
import ru.shelq.nework.util.AndroidUtils
import ru.shelq.nework.util.AndroidUtils.loadImgCircle


interface PostOnInteractionListener {
    fun onLike(post: Post) {}
    fun onRemove(post: Post) {}
    fun onEdit(post: Post) {}
    fun onOpen(post: Post) {}

}

class PostAdapter(
    private val onInteractionListener: PostOnInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bing(post)
    }

}


class PostViewHolder(
    private val binding: PostCardBinding,
    private val onInteractionListener: PostOnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bing(post: Post) {
        binding.apply {
            CardPost.setOnClickListener {
                onInteractionListener.onOpen(post)
            }

            AuthorTV.text = post.author
            AvatarIV.loadImgCircle(post.authorAvatar)
            PublishedPostTV.text = AndroidUtils.dateFormatToText(post.published, root.context)
            TextPostTV.text = post.content
            LinkPostTV.text = post.link
            LikeIB.text = post.likeOwnerIds.size.toString()
            LikeIB.isChecked = post.likedByMe

            LikeIB.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            MenuIB.isVisible = post.ownedByMe
            MenuIB.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options_card)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
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

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}