package ru.shelq.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.shelq.nework.databinding.JobCardBinding
import ru.shelq.nework.dto.Job
import ru.shelq.nework.dto.Post
import ru.shelq.nework.util.AndroidUtils

interface JobOnInteractionListener {
    fun onJobDelete(job: Job) {}

}
class JobAdapter(private val onInteractionListener: JobOnInteractionListener) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()){


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

    fun bind(job: Job) {
        binding.apply {
            NameJobTV.text = job.name
            PositionJobTV.text = job.position
            PeriodJobTV.text = AndroidUtils.dateRangeToText(job.start, job.finish)
            LinkJobTV.text = job.link
            DeleteJobIB.visibility = if (job.ownedByMe) View.VISIBLE else View.INVISIBLE
            DeleteJobIB.setOnClickListener{
                onInteractionListener.onJobDelete(job)
            }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}