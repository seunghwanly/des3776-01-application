package com.example.des3776

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.des3776.databinding.HypertensionsItemBinding

class HypertensionAdapter(private val context: Context) :
    RecyclerView.Adapter<HypertensionAdapter.HypertensionViewHolder>() {

    var data = mutableListOf<Hypertension>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HypertensionViewHolder {
        val binding = HypertensionsItemBinding.inflate(LayoutInflater.from(context), parent, false)

        return HypertensionViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: HypertensionViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    inner class HypertensionViewHolder(private val binding: HypertensionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(data: Hypertension) {
            binding.hypertension = data
        }

    }
}