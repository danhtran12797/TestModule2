package com.intelnet.omniwallet.ui.home.detailToken

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
import com.intelnet.omniwallet.databinding.DialogChooseMenuTokenDetailBinding
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemMenu
import com.intelnet.omniwallet.ui.home.detailToken.adapter.MenuTokenDetailAdapter

class ChooseMenuDialogFragment(
    private var menuItems: List<ItemMenu>,
    private var callbackAction: (Int) -> Unit
) : BaseBottomSheetFragment<DialogChooseMenuTokenDetailBinding>() {

    private val adapter: MenuTokenDetailAdapter by lazy {
        MenuTokenDetailAdapter(
            callBackAction = {
                callbackAction.invoke(it)
                this.dismiss()
            }
        )
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogChooseMenuTokenDetailBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.addAll(menuItems)
        initAdapter()

    }

    private fun initAdapter() {
        binding.rvChooseItem.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChooseMenuDialogFragment.adapter
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
            items: List<ItemMenu>,
            callbackAction: (Int) -> Unit
        ) {
            val dialog = ChooseMenuDialogFragment(items, callbackAction)
            dialog.show(fm, dialog.tag)
        }
    }
}