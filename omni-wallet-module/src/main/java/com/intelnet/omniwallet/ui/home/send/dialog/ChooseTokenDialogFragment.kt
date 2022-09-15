package com.intelnet.omniwallet.ui.home.send.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseBottomSheetFragment
import com.intelnet.omniwallet.databinding.DialogChooseTokenBinding
import com.intelnet.omniwallet.ui.home.adapter.ItemToken
import com.intelnet.omniwallet.ui.home.adapter.ItemTokenAdapter

class ChooseTokenDialogFragment(
    private var tokenItems: List<ItemToken>,
    private var chooseTokenListener: ((Int, ItemToken) -> Unit)?
) : BaseBottomSheetFragment<DialogChooseTokenBinding>() {

    private val adapter: ItemTokenAdapter by lazy {
        ItemTokenAdapter(
            callBackTokenClick = { x, y->
                chooseTokenListener?.invoke(x, y)
                this.dismiss()
            }
        )
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogChooseTokenBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.addAll(tokenItems)
        initAdapter()

    }

    private fun initAdapter() {
        binding.rvChooseToken.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChooseTokenDialogFragment.adapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance(
            fm: FragmentManager,
            items: List<ItemToken>,
            chooseTokenListener: ((Int, ItemToken) -> Unit)?
        ) {
            val dialog = ChooseTokenDialogFragment(items, chooseTokenListener)
            dialog.show(fm, dialog.tag)
        }
    }
}