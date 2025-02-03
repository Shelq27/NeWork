package ru.shelq.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.shelq.nework.R
import ru.shelq.nework.databinding.UserCardBinding
import ru.shelq.nework.dto.User

interface UserOnInteractionListener {
    fun onUserClick(user: User) {}


}
class UserAdapter(
    private val onInteractionListener: UserOnInteractionListener,
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserViewHolder(
            UserCardBinding.inflate(layoutInflater, parent, false),
            onInteractionListener
        )
    }

}

class UserViewHolder(
    private val binding: UserCardBinding,
    private val onInteractionListener: UserOnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {
            Glide.with(AvatarIV)
                .load(user.avatar)
                .placeholder(R.drawable.ic_downloading_100dp)
                .error(R.drawable.ic_error_outline_100dp)
                .timeout(10_000)
                .circleCrop()
                .into(binding.AvatarIV)
            AuthorTV.text = user.name
            LoginTV.text = user.login

            itemView.setOnClickListener {
                onInteractionListener.onUserClick(user)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}