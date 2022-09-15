package com.intelnet.omniwallet.ui.addWallet.createWallet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.databinding.ItemWordConfirmBinding

class RandomPhraseAdapter(
    private val lstWord: MutableList<String> = mutableListOf()
) :
    RecyclerView.Adapter<RandomPhraseAdapter.RandomPhraseViewHolder>() {

    var callBackItemClick: ((String) -> Unit)? = null

    fun addAll(listItem: List<String>) {
        lstWord.clear()
        lstWord.addAll(listItem)
        notifyDataSetChanged()
    }

    inner class RandomPhraseViewHolder(val binding: ItemWordConfirmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.txtWordNumber.isVisible = false

            binding.txtNameWord.text = item
            binding.txtWordNumber.text = "${adapterPosition + 1}"

            binding.txtNameWord.background =
                ContextCompat.getDrawable(binding.root.context, R.drawable.bg_btn_wallet2)

            itemView.setOnClickListener {
                callBackItemClick?.invoke(lstWord[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomPhraseViewHolder {
        return RandomPhraseViewHolder(
            ItemWordConfirmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RandomPhraseViewHolder, position: Int) {
        holder.bind(lstWord[position])
    }

    override fun getItemCount(): Int {
        return lstWord.size
    }
}