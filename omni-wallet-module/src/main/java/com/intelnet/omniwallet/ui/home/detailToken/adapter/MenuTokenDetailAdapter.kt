package com.intelnet.omniwallet.ui.home.detailToken.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.databinding.ItemMenuTokenDetailBinding

class MenuTokenDetailAdapter(
    private val lstItem: MutableList<ItemMenu> = mutableListOf(),
    private val callBackAction: (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addAll(listItem: List<ItemMenu>) {
        lstItem.clear()
        lstItem.addAll(listItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemNetViewHolder(
            ItemMenuTokenDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemNetViewHolder).bind(lstItem[position])
    }

    override fun getItemCount(): Int {
        return lstItem.size
    }

    inner class ItemNetViewHolder(val binding: ItemMenuTokenDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemMenu) {
            binding.txtNameAction.text = item.name
            val color = ContextCompat.getColorStateList(
                binding.root.context,
                if (item.action != ItemMenu.ACTION_HIDE_TOKEN) R.color.blue500 else R.color.red1
            )
            binding.txtNameAction.setTextColor(color)
            itemView.setOnClickListener {
                callBackAction.invoke(item.action)
            }
        }
    }
}

data class ItemMenu(
    var name: String,
    var action: Int
) {
    companion object {
        const val ACTION_VIEW_ON_EXPLORER = 0
        const val ACTION_TOKEN_DETAIL = 1
        const val ACTION_HIDE_TOKEN = 2
    }
}
