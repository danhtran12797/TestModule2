package com.intelnet.omniwallet.ui.home.send.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.view.Identicon
import com.intelnet.omniwallet.databinding.ItemAddressRecentlyBinding
import com.intelnet.omniwallet.util.formatAddressWallet

class AddressRecentlyAdapter(
    private val lstAddress: MutableList<String> = mutableListOf(),
    private val callBackItemClick: (String) -> Unit,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addAll(listItem: List<String>) {
        lstAddress.clear()
        lstAddress.addAll(listItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemAddressViewHolder(
            ItemAddressRecentlyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemAddressViewHolder).bind(lstAddress[position])
    }

    override fun getItemCount(): Int {
        return lstAddress.size
    }

    inner class ItemAddressViewHolder(val binding: ItemAddressRecentlyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.apply {
                txtAddress.text = item.formatAddressWallet()
                Identicon(imgAddress, item)
            }
            itemView.setOnClickListener {
                callBackItemClick.invoke(item)
            }
        }
    }

}
