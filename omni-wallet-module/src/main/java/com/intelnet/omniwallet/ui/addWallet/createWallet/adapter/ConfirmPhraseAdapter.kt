package com.intelnet.omniwallet.ui.addWallet.createWallet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.BuildConfig
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.databinding.ItemWordConfirmBinding
import com.intelnet.omniwallet.entity.WordItem

class ConfirmPhraseAdapter(
    private val lstWord: MutableList<WordItem> = mutableListOf()
) :
    RecyclerView.Adapter<ConfirmPhraseAdapter.ConfirmPhraseViewHolder>() {

    var callBackValidate: ((Boolean, Boolean) -> Unit)? = null

    var selectedPos = 0

    fun addAll(listItem: List<WordItem>) {
        lstWord.clear()
        lstWord.addAll(listItem)
        notifyDataSetChanged()
    }

    inner class ConfirmPhraseViewHolder(val binding: ItemWordConfirmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WordItem) {
            binding.txtWordNumber.isVisible = true

            binding.txtNameWord.text = if(BuildConfig.DEBUG) item.name else item.fill
//            binding.txtNameWord.text = item.fill
            binding.txtWordNumber.text = "${adapterPosition + 1}"

            binding.txtNameWord.background = when {
                selectedPos == adapterPosition  -> ContextCompat.getDrawable(binding.root.context, R.drawable.bg_btn_selected)
                item.fill.isNotEmpty() -> ContextCompat.getDrawable(binding.root.context, R.drawable.bg_btn_wallet2)
                else -> ContextCompat.getDrawable(binding.root.context, R.drawable.bg_btn_wallet3)
            }

            binding.txtNameWord.setOnClickListener {
                selected(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfirmPhraseViewHolder {
        return ConfirmPhraseViewHolder(
            ItemWordConfirmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ConfirmPhraseViewHolder, position: Int) {
        holder.bind(lstWord[position])
    }

    override fun getItemCount(): Int {
        return lstWord.size
    }

    fun addWord(word:String){
        if(selectedPos==-1)
            return
        lstWord[selectedPos].fill = word
        selectedPos = lstWord.indexOfFirst { it.fill.isEmpty() }
        notifyDataSetChanged()
        if(selectedPos!=-1)
            callBackValidate?.invoke(false, true)
        else
            callBackValidate?.invoke(
                checkValidate(), false
            )
    }

    fun selected(position:Int){
        if(selectedPos!=position){
            selectedPos = position
            lstWord[position].fill = ""
            notifyDataSetChanged()
            callBackValidate?.invoke(false, true)
        }
    }

    fun checkValidate()=!lstWord.any { it.name!=it.fill }


}