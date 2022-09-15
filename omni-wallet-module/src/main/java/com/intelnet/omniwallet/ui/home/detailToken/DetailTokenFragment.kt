package com.intelnet.omniwallet.ui.home.detailToken

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.databinding.CustomSnackbarViewBinding
import com.intelnet.omniwallet.databinding.FragmentDetailTokenBinding
import com.intelnet.omniwallet.ui.home.HomeViewModel
import com.intelnet.omniwallet.ui.home.adapter.ItemToken
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemHistoryTokenAdapter
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemMenu
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemTransaction
import com.intelnet.omniwallet.util.Status
import com.intelnet.omniwallet.util.dpToPx
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class DetailTokenFragment : BaseFragment<FragmentDetailTokenBinding, HomeViewModel>() {

    private var handler: Handler?= null

    val viewModel2: DetailTokenViewModel by viewModels()
    override val viewModel: HomeViewModel by activityViewModels()

    private val args: DetailTokenFragmentArgs by navArgs()

    var snackbar: Snackbar? = null

    private val index: Int by lazy {
        args.indexToken
    }

    private val lstItemMenu: MutableList<ItemMenu> by lazy {
        val lstItem = mutableListOf(
            ItemMenu(
                name = getString(R.string.view_on_block_explorer),
                action = ItemMenu.ACTION_VIEW_ON_EXPLORER
            )
        )
        if (index != 0)
            lstItem.add(
                ItemMenu(
                    name = getString(R.string.token_details),
                    action = ItemMenu.ACTION_TOKEN_DETAIL
                )
            )
        lstItem
    }

    private val callBackItemHistory: (ItemTransaction) -> Unit = {
        DetailTransactionDialogFragment.newInstance(fManager, it, viewListener = {
            val url = StringBuilder(viewModel.getScanUrlNetworkDefault()).append("/tx/")
                .append(it.hash)
            navigate(
                DetailTokenFragmentDirections.actionDetailTokenFragmentToWebViewFragment(url.toString())
            )
        })
    }
    private val callBackViewAll: () -> Unit = {
        val url = StringBuilder(viewModel.getScanUrlNetworkDefault()).append("/address/")
            .append(addressWallet)
        navigate(
            DetailTokenFragmentDirections.actionDetailTokenFragmentToWebViewFragment(url.toString())
        )
    }

    private val callBackReceive: () -> Unit = {
        navigate(
            DetailTokenFragmentDirections.actionDetailTokenFragmentToReceiveTokenDialogFragment(
                addressWallet
            )
        )
    }

    private val callBackSend: () -> Unit = {
        navigate(
            DetailTokenFragmentDirections.actionDetailTokenFragmentToSendTokenFragment(
                index,
                null
            )
        )
    }

    private val itemHistoryAdapter: ItemHistoryTokenAdapter by lazy {
        ItemHistoryTokenAdapter(
            callBackItemHistory = callBackItemHistory,
            callBackViewAll = callBackViewAll,
            callBackReceive = callBackReceive,
            callBackSend = callBackSend
        )
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDetailTokenBinding.inflate(inflater, container, false)

    override fun initControl() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
            viewModel2.loadTransaction(addressWallet)
        }

        binding.imgBack.setOnClickListener {
            backToPrevious()
        }

        binding.imgMenu.setOnClickListener {
            ChooseMenuDialogFragment.newInstance(
                fManager,
                lstItemMenu,
                callbackAction = {
                    when (it) {
                        ItemMenu.ACTION_VIEW_ON_EXPLORER -> {
                            val url = StringBuilder(viewModel.getScanUrlNetworkDefault())
                            if (index != 0)
                                url.append("/token/").append(viewModel.lstToken[index].address)
                            navigate(
                                DetailTokenFragmentDirections.actionDetailTokenFragmentToWebViewFragment(
                                    url.toString()
                                )
                            )
                        }
                        else -> {
                            navigate(
                                DetailTokenFragmentDirections.actionDetailTokenFragmentToSmartContractDetailFragment(
                                    index
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    override fun initUI() {
        initDefaultNetwork()
        initBalanceUiFormat(viewModel.lstToken[index])

        binding.rvHistoryTransaction.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemHistoryAdapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
            )
        }

        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.blue500
            )
        )
    }

    private fun initDefaultNetwork() {
        val itemNetwork = viewModel.getItemNetworkDefault()
        binding.txtSymbolToken.text = viewModel.lstToken[index].symbol
        itemNetwork?.let {
            binding.txtDot.setBackgroundColor(it.color)
            binding.txtNet.text = it.name
        }
    }

    override fun initEvent() {
        viewModel.fetchLiveData.observe(viewLifecycleOwner) {
            when (it.responseType) {
                Status.ERROR -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                Status.LOADING -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }

                Status.SUCCESSFUL -> {
                    binding.swipeRefreshLayout.isRefreshing = false
//                    initBalanceUiFormat(viewModel.lstToken[index])
                    viewModel2.lstItemTransaction[0] =
                        ItemTransaction.generateItemHeader(viewModel.lstToken[index])
                    itemHistoryAdapter.addHeader(
                        viewModel2.lstItemTransaction[0]
                    )
                }
            }
        }

        viewModel2.transactionLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { data ->
                when (data.responseType) {
                    Status.LOADING -> {
//                        showLoadingDialog()
                    }
                    Status.SUCCESSFUL -> {
//                        hideDialog()
                        data.data?.let {
                            itemHistoryAdapter.addAll(
                                viewModel2.parseToItemTransaction(
                                    response = it,
                                    lstToken = viewModel.lstToken,
                                    walletAddress = addressWallet,
                                    symbolDefault = viewModel.getSymbolNetworkDefault(),
                                    contractAddress = viewModel.lstToken[index].address
                                )
                            )
                        }
                    }
                    Status.ERROR -> {
//                        hideDialog()
                        data.error?.message?.let {
                            showToast("Error: $it")
                        }
                    }
                }
            }
        }

        viewModel.listenLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { data ->
                when (data.responseType) {
                    Status.ERROR -> {
                        data.error?.message?.let {
//                            showToast("Error: $it")
                        }
                    }

                    Status.LOADING -> {

                    }

                    Status.SUCCESSFUL -> {
                        data.data?.let {
                            Timber.d("FROM: ${it.from}")
                            Timber.d("TO: ${it.to}")

                            if (addressWallet == it.from || addressWallet == it.to) {
                                snackbar?.dismiss()
                                Timber.d("GOOD JOB")
                                viewModel.refresh()
                                viewModel2.loadTransaction(addressWallet)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun showSnackbar(duration:Int) {
        if(snackbar==null){
            snackbar = Snackbar.make(binding.root, "", duration)

            val customSnackView1: View = CustomSnackbarViewBinding.inflate(layoutInflater, binding.root, false).root

            val snackbarLayout = snackbar?.view as Snackbar.SnackbarLayout

            snackbarLayout.setPadding(16.dpToPx, 16.dpToPx, 16.dpToPx, 16.dpToPx)

            snackbarLayout.addView(customSnackView1, 0)
        }
        snackbar?.show()
    }

    private fun initBalanceUiFormat(item: ItemToken) {
        if (viewModel2.lstItemTransaction.isEmpty()) {
            viewModel2.lstItemTransaction.add(
                ItemTransaction.generateItemHeader(item)
            )
            itemHistoryAdapter.addAll(viewModel2.lstItemTransaction)
        }
    }

    override fun initConfig() {
        if (!firstCallApi) {
            firstCallApi = true
            if(args.awaitTransaction){
                showSnackbar(4000)
            }
            handler= Handler()
            handler?.postDelayed(object : Runnable {
                override fun run() {
                    snackbar?.dismiss()
                    viewModel.refresh()
                    viewModel2.loadTransaction(addressWallet)

                    handler?.postDelayed(this, 10000)
                }
            }, 10000)
            viewModel.refresh()
            viewModel2.loadTransaction(addressWallet)
        }
    }

    override fun onDestroyView() {
        handler?.removeCallbacksAndMessages(null)
        handler=null
        snackbar=null
        binding.rvHistoryTransaction.adapter = null
        super.onDestroyView()
    }

}