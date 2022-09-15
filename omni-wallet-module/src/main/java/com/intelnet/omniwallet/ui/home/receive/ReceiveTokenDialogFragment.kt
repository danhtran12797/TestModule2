package com.intelnet.omniwallet.ui.home.receive

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseBottomSheetFragment
import com.intelnet.omniwallet.databinding.FragmentReceiveTokenBinding
import com.intelnet.omniwallet.util.formatAddressWallet
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReceiveTokenDialogFragment : BaseBottomSheetFragment<FragmentReceiveTokenBinding>() {

    private val args: ReceiveTokenDialogFragmentArgs by navArgs()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReceiveTokenBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val strAddressWallet = getString(R.string.address_demo)
        binding.txtAddress.text = args.address.formatAddressWallet()

        generateQRCode(args.address)?.run {
            binding.imgQR.setImageBitmap(this)
        }

        binding.imgShareAddress.setOnClickListener {

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, args.address)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.txtCopy.setOnClickListener {
            copyToClipboard(args.address)
            showToast(getString(R.string.toast_address_copied))
        }
    }

    private fun generateQRCode(text: String): Bitmap? {
        val writer = MultiFormatWriter()
        return try {
            val matrix = writer.encode(text, BarcodeFormat.QR_CODE, 350, 350)
            val encoder = BarcodeEncoder()
            val bitmap = encoder.createBitmap(matrix)
            bitmap
        } catch (e: WriterException) {
            null
        }
    }
}