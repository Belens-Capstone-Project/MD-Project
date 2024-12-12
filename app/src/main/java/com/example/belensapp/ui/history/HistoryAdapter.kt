package com.example.belensapp.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.belensapp.R
import com.example.belensapp.api.PredictResponse
import com.example.belensapp.databinding.ItemHistoryBinding

class HistoryAdapter(private var historyList: List<PredictResponse>, private val onItemClicked: (PredictResponse) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(predictResponse: PredictResponse) {
            with(binding) {
                tvPrediction.text = predictResponse.data?.prediction ?: "No prediction available"

                val imageUrl = predictResponse.data?.fileUrl
                Glide.with(ivImageHistory.context)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_camera)
                    .error(R.drawable.ic_camera)
                    .into(ivImageHistory)

                gradeA.visibility = View.GONE
                gradeB.visibility = View.GONE
                gradeC.visibility = View.GONE
                gradeD.visibility = View.GONE
                gradeE.visibility = View.GONE

                when (predictResponse.data?.gizi?.grade?.lowercase()) {
                    "a" -> {
                        gradeA.visibility = View.VISIBLE
                        binding.root.strokeColor = binding.root.context.getColor(R.color.a)
                    }
                    "b" -> {
                        gradeB.visibility = View.VISIBLE
                        binding.root.strokeColor = binding.root.context.getColor(R.color.b)
                    }
                    "c" -> {
                        gradeC.visibility = View.VISIBLE
                        binding.root.strokeColor = binding.root.context.getColor(R.color.c)
                    }
                    "d" -> {
                        gradeD.visibility = View.VISIBLE
                        binding.root.strokeColor = binding.root.context.getColor(R.color.d)
                    }
                    "e" -> {
                        gradeE.visibility = View.VISIBLE
                        binding.root.strokeColor = binding.root.context.getColor(R.color.e)
                    }
                    else -> {
                        binding.root.strokeColor = binding.root.context.getColor(R.color.graywel)
                    }
                }

                itemView.setOnClickListener {
                    onItemClicked(predictResponse)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount() = historyList.size

    fun updateHistoryList(newHistoryList: List<PredictResponse>) {
        historyList = newHistoryList
        notifyDataSetChanged()
    }
}
