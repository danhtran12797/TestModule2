package com.intelnet.omniwallet.ui.addWallet.createWallet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.databinding.ItemWordBinding

class MemorizePhraseAdapter(
    private val lstWord: MutableList<String> = mutableListOf()
) :
    RecyclerView.Adapter<MemorizePhraseAdapter.MemorizePhraseViewHolder>() {

    fun addAll(listItem: List<String>) {
        lstWord.clear()
        lstWord.addAll(listItem)
        notifyDataSetChanged()
    }

    inner class MemorizePhraseViewHolder(val binding: ItemWordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.txtNameWord.text="${adapterPosition+1}. $item"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemorizePhraseViewHolder {
        return MemorizePhraseViewHolder(
            ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MemorizePhraseViewHolder, position: Int) {
        holder.bind(lstWord[position])
    }

    override fun getItemCount(): Int {
        return lstWord.size
    }
}