package ru.shelq.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.shelq.nework.databinding.JobCardBinding
import ru.shelq.nework.dto.Jobs
import ru.shelq.nework.util.AndroidUtils

interface JobOnInteractionListener {
    fun onJobDelete(jobs: Jobs) {}

}
class JobAdapter(private val onInteractionListener: JobOnInteractionListener) : ListAdapter<Jobs, JobViewHolder>(JobDiffCallback()){


    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return JobViewHolder(
            JobCardBinding.inflate(layoutInflater, parent, false),
            onInteractionListener
        )
    }

}

class JobViewHolder(
    private val binding: JobCardBinding,
    private val onInteractionListener: JobOnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(jobs: Jobs) {
        binding.apply {
            nameJobTV.text = jobs.name
            positionJobTV.text = jobs.position
            periodJobTV.text = AndroidUtils.dateRangeToText(jobs.start, jobs.finish)
            linkJobTV.text = jobs.link
            deleteJobIB.visibility = if (jobs.ownedByMe) View.VISIBLE else View.INVISIBLE
            deleteJobIB.setOnClickListener{
                onInteractionListener.onJobDelete(jobs)
            }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Jobs>() {
    override fun areItemsTheSame(oldItem: Jobs, newItem: Jobs): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Jobs, newItem: Jobs): Boolean {
        return oldItem == newItem
    }
}