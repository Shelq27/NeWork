package ru.shelq.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.shelq.nework.R
import ru.shelq.nework.databinding.UserCardChooseBinding
import ru.shelq.nework.dto.User

interface CheckOnInteractionListener{
    fun onCheck(user: User, checked: Boolean){}
}
class ChooseUserAdapter(
    private var checkedUsers: LongArray?,
    private val onCheckListener: CheckOnInteractionListener
) : ListAdapter<User, ChooseUserAdapter.ChooseUserViewHolder>(ChooseUserDiffCallback()) {

    override fun onBindViewHolder(holder: ChooseUserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseUserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ChooseUserViewHolder(
            UserCardChooseBinding.inflate(layoutInflater, parent, false),
            onCheckListener
        )
    }
    inner class ChooseUserViewHolder(
        private val binding: UserCardChooseBinding,
        private val onCheckListener: CheckOnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                    Glide.with(AvatarIV)
                        .load(user.avatar)
                        .placeholder(R.drawable.ic_downloading_100dp)
                        .error(R.drawable.ic_error_outline_100dp)
                        .timeout(10_000)
                        .circleCrop()
                        .into(AvatarIV)
                AuthorTV.text = user.name
                LoginTV.text = user.login

                ChooseUserCB.isChecked = checkedUsers?.contains(user.id) == true

                ChooseUserCB.setOnClickListener {
                    if (checkedUsers != null) {
                        if (ChooseUserCB.isChecked) {
                            if (checkedUsers?.contains(user.id) == false) checkedUsers =
                                checkedUsers!!.plus(user.id)
                        } else {
                            if (checkedUsers?.contains(user.id) == true) checkedUsers =
                                checkedUsers!!.takeIf { !it.equals(user.id) }
                        }
                    }
                    onCheckListener.onCheck(user, ChooseUserCB.isChecked)
                }
            }
        }
    }
}


class ChooseUserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}