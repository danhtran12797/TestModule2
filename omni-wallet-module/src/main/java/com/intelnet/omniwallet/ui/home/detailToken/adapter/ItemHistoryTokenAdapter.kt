package com.intelnet.omniwallet.ui.home.detailToken.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.view.Identicon
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.databinding.ItemEmptyTransactionBinding
import com.intelnet.omniwallet.databinding.ItemHeaderTransactionBinding
import com.intelnet.omniwallet.databinding.ItemHistoryTokenBinding
import com.intelnet.omniwallet.databinding.ItemViewAllHistoryBinding
import com.intelnet.omniwallet.ui.home.adapter.ItemToken
import com.intelnet.omniwallet.util.BalanceUtil

class ItemHistoryTokenAdapter(
    private val lstItems: MutableList<ItemTransaction> = mutableListOf(),
    private val callBackItemHistory: (ItemTransaction) -> Unit,
    private val callBackViewAll: (() -> Unit)? = null,
    private val callBackReceive: (() -> Unit)? = null,
    private val callBackSend: (() -> Unit)? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addAll(listItem: List<ItemTransaction>) {
        lstItems.clear()
        lstItems.addAll(listItem)
        notifyDataSetChanged()
    }

    fun addHeader(item: ItemTransaction) {
        if (lstItems.isNotEmpty()) {
            lstItems[0] = item
        } else {
            lstItems.add(item)
        }
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_HEADER ->
                ItemHeaderHolder(
                    ItemHeaderTransactionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ITEM_DATA ->
                ItemHistoryViewHolder(
                    ItemHistoryTokenBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ITEM_FOOTER ->
                ItemViewAllHolder(
                    ItemViewAllHistoryBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> // ITEM_FOOTER
                EmptyTransactionHolder(
                    ItemEmptyTransactionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_HEADER -> (holder as ItemHeaderHolder).bind(lstItems[position].itemToken)
            ITEM_DATA -> (holder as ItemHistoryViewHolder).bind(lstItems[position])
            ITEM_FOOTER -> (holder as ItemViewAllHolder).bind()
            else -> {}
        }
    }

    override fun getItemCount(): Int {
        return lstItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return lstItems[position].type
    }

    inner class ItemHistoryViewHolder(val binding: ItemHistoryTokenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemTransaction) {
            binding.apply {
                val colorTint = if (item.status != FAILED) R.color.blue500 else R.color.red1

                val (icon, title, content) = when (item.status) {
                    SELF or CONFIRMED -> Triple(
                        R.drawable.ic_send_24,
                        root.context.getString(R.string.sent_to, item.symbol),
                        root.context.getString(R.string.confirmed)
                    )
                    RECEIVED -> Triple(
                        R.drawable.ic_receive_24,
                        root.context.getString(R.string.receive_from, item.symbol),
                        root.context.getString(R.string.confirmed)
                    )
                    CONFIRMED -> Triple(
                        R.drawable.ic_send_24,
                        root.context.getString(R.string.sent_to, item.symbol),
                        root.context.getString(R.string.confirmed)
                    )
                    else -> Triple(
                        R.drawable.ic_send_24,
                        root.context.getString(R.string.sent_to, item.symbol),
                        root.context.getString(R.string.failed)
                    )
                }

                txtName.text = title

                txtDateTime.text =
                    StringBuilder("#${item.nonce}").append(" - ").append(item.formatDateTime)

                txtAmount.text = BalanceUtil.formatBalanceWithSymbol(item.symbol, item.amount)

                txtStatus.text = content

                txtStatus.setTextColor(
                    ContextCompat.getColorStateList(
                        root.context,
                        if (item.status != FAILED) R.color.green_1 else R.color.red1
                    )
                )

                imgAction.setImageDrawable(ContextCompat.getDrawable(root.context, icon))

                imgAction.strokeColor = ContextCompat.getColorStateList(
                    root.context,
                    colorTint
                )

                imgAction.setColorFilter(
                    ContextCompat.getColor(
                        binding.root.context,
                        colorTint
                    )
                )
            }

            itemView.setOnClickListener {
                callBackItemHistory.invoke(item)
            }
        }
    }

    inner class ItemViewAllHolder(val binding: ItemViewAllHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            itemView.setOnClickListener {
                callBackViewAll?.invoke()
            }
        }
    }

    inner class EmptyTransactionHolder(val binding: ItemEmptyTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ItemHeaderHolder(val binding: ItemHeaderTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemToken?) {
            item?.let {
                val balanceFormat = StringBuilder().append(it.amount)
                    .append(" ${it.symbol}").toString()
                binding.txtAmountToken.text = balanceFormat
                Identicon(
                    binding.imgToken,
                    it.address
                )
            }
            binding.viewReceive.setOnClickListener {
                callBackReceive?.invoke()
            }

            binding.viewSend.setOnClickListener {
                callBackSend?.invoke()
            }
        }
    }

    companion object {
        const val ITEM_DATA = 0
        const val ITEM_FOOTER = 1
        const val ITEM_HEADER = 2
        const val ITEM_EMPTY = 3

        const val CONFIRMED = 0
        const val FAILED = 1
        const val RECEIVED = 2
        const val SELF = 3
    }
}

data class ItemTransaction(
    val hash: String = "",
    val nonce: String = "",
    val symbol: String = "",
    val symbolNative: String = "",
    val amount: String = "",
    val estimate: String = "",
    val from: String = "",
    val to: String = "",
    val formatDateTime: String = "",
    val action: String = "send",
    val status: Int = 0,
    val type: Int,

    val itemToken: ItemToken? = null
) {
    companion object {
        fun generateItemFooter() = ItemTransaction(
            type = ItemHistoryTokenAdapter.ITEM_FOOTER
        )

        fun generateItemEmpty() = ItemTransaction(
            type = ItemHistoryTokenAdapter.ITEM_EMPTY
        )

        fun generateItemHeader(item: ItemToken) = ItemTransaction(
            type = ItemHistoryTokenAdapter.ITEM_HEADER,
            itemToken = item
        )
    }

}
