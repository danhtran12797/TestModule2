package com.intelnet.omniwallet.ui.home.adapter

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.view.Identicon
import com.intelnet.omniwallet.databinding.ItemChooseTokenBinding
import com.intelnet.omniwallet.databinding.ItemImportTokenBinding
import com.intelnet.omniwallet.databinding.ItemTokenBinding
import kotlinx.parcelize.Parcelize

class ItemTokenAdapter(
    private val lstToken: MutableList<ItemToken> = mutableListOf(),
    private val callBackTokenClick: (Int, ItemToken) -> Unit,
    private val callBackImportToken: (() -> Unit)? = null,
    private val callBackTokenLongClick: ((Int, ItemToken) -> Unit)? =null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addAll(listItem: List<ItemToken>) {
        lstToken.clear()
        lstToken.addAll(listItem)
        notifyDataSetChanged()
    }

    fun clearAll(){
        lstToken.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemToken.ITEM_DATA ->
                ItemTokenViewHolder(
                    ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            ItemToken.ITEM_FOOTER ->
                ItemFooterViewHolder(
                    ItemImportTokenBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else ->
                ItemChooseViewHolder(
                    ItemChooseTokenBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ItemToken.ITEM_DATA -> (holder as ItemTokenViewHolder).bind(lstToken[position])
            ItemToken.ITEM_FOOTER -> (holder as ItemFooterViewHolder).bind()
            else -> (holder as ItemChooseViewHolder).bind(lstToken[position])
        }
    }

    override fun getItemCount(): Int {
        return if (lstToken.isEmpty()) 0 else lstToken.size
    }

    override fun getItemViewType(position: Int): Int {
        if (callBackImportToken == null)
            return ItemToken.ITEM_CHOOSE
        return if (lstToken.size - 1 != position) ItemToken.ITEM_DATA else ItemToken.ITEM_FOOTER
    }

    inner class ItemTokenViewHolder(val binding: ItemTokenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemToken) {
           val balanceFormat = StringBuilder().append(item.amount)
                .append(" ${item.symbol}").toString()
            binding.txtNameToken.text = balanceFormat
            Identicon(
                binding.imgToken,
                item.address
            )

            itemView.setOnClickListener {
                callBackTokenClick.invoke(adapterPosition, item)
            }

            itemView.setOnLongClickListener{
                callBackTokenLongClick?.invoke(adapterPosition, item)
                true
            }
        }
    }

    inner class ItemFooterViewHolder(val binding: ItemImportTokenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            itemView.setOnClickListener {
                callBackImportToken?.invoke()
            }
        }
    }

    inner class ItemChooseViewHolder(val binding: ItemChooseTokenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemToken) {

            binding.txtNameToken.text = item.symbol
            binding.txtAmountToken.text = item.amount
            Identicon(
                binding.imgToken,
                item.address
            )

            itemView.setOnClickListener {
                callBackTokenClick.invoke(adapterPosition , item)
            }
        }
    }
}

@Parcelize
data class ItemToken(
    var symbol: String = "",
    var amount: String = "",
    var address: String = "",
    var type: Int = 0
) : Parcelable {
    companion object {
        const val ITEM_DATA = 0
        const val ITEM_FOOTER = 1
        const val ITEM_CHOOSE = 2

        fun generateFooterItem() = ItemToken(type = ITEM_FOOTER)

        fun generateHeadItem(
            symbol: String,
            amount: String,
            type: Int = ITEM_DATA
        ) = ItemToken(symbol = symbol, amount = amount, type = type)
    }
}
