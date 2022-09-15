package com.intelnet.omniwallet.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.databinding.ViewStepCreateWalletBinding

class StepCreateWalletView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding =
        ViewStepCreateWalletBinding.inflate(LayoutInflater.from(context), this, true)

    private var type = 1

    init {
        binding.root
        initAttrs(context, attrs)
        initLayout()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArr =
            context.obtainStyledAttributes(attrs, R.styleable.StepCreateWalletView, 0, 0).apply {
                try {
                    type = getInt(R.styleable.StepCreateWalletView_type, 1)
                    initLayout()
                } catch (e: Exception) {

                }
            }
        typedArr.recycle()
    }

    private fun initLayout(){
        when(type){
            1-> step1InProcess()
            2-> step2InProcess()
            else -> step3InProcess() // step 3
        }
    }

    private fun step1InProcess(){
        binding.line1.background=ContextCompat.getDrawable(context, R.color.gray)
        binding.line2.background=ContextCompat.getDrawable(context, R.color.gray)

        binding.txtNumber1.background=ContextCompat.getDrawable(context, R.drawable.bg_step_in_process)
        binding.txtNumber2.background=ContextCompat.getDrawable(context, R.drawable.bg_step_inactive)
        binding.txtNumber3.background=ContextCompat.getDrawable(context, R.drawable.bg_step_inactive)

        binding.txtNumber1.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.txtNumber2.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.txtNumber3.setTextColor(ContextCompat.getColor(context, R.color.gray))

        binding.txtTextNumber1.setTextColor(ContextCompat.getColor(context, R.color.blue500))
        binding.txtTextNumber2.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.txtTextNumber3.setTextColor(ContextCompat.getColor(context, R.color.gray))
    }

    private fun step2InProcess(){
        binding.line1.background=ContextCompat.getDrawable(context, R.color.blue500)
        binding.line2.background=ContextCompat.getDrawable(context, R.color.gray)

        binding.txtNumber1.background=ContextCompat.getDrawable(context, R.drawable.bg_step_pass)
        binding.txtNumber2.background=ContextCompat.getDrawable(context, R.drawable.bg_step_in_process)
        binding.txtNumber3.background=ContextCompat.getDrawable(context, R.drawable.bg_step_inactive)

        binding.txtNumber1.setTextColor(ContextCompat.getColor(context, R.color.white))
        binding.txtNumber2.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.txtNumber3.setTextColor(ContextCompat.getColor(context, R.color.gray))

        binding.txtTextNumber1.setTextColor(ContextCompat.getColor(context, R.color.blue500))
        binding.txtTextNumber2.setTextColor(ContextCompat.getColor(context, R.color.blue500))
        binding.txtTextNumber3.setTextColor(ContextCompat.getColor(context, R.color.gray))
    }

    private fun step3InProcess(){
        binding.line1.background=ContextCompat.getDrawable(context, R.color.blue500)
        binding.line2.background=ContextCompat.getDrawable(context, R.color.blue500)

        binding.txtNumber1.background=ContextCompat.getDrawable(context, R.drawable.bg_step_pass)
        binding.txtNumber2.background=ContextCompat.getDrawable(context, R.drawable.bg_step_pass)
        binding.txtNumber3.background=ContextCompat.getDrawable(context, R.drawable.bg_step_in_process)

        binding.txtNumber1.setTextColor(ContextCompat.getColor(context, R.color.white))
        binding.txtNumber2.setTextColor(ContextCompat.getColor(context, R.color.white))
        binding.txtNumber3.setTextColor(ContextCompat.getColor(context, R.color.black))

        binding.txtTextNumber1.setTextColor(ContextCompat.getColor(context, R.color.blue500))
        binding.txtTextNumber2.setTextColor(ContextCompat.getColor(context, R.color.blue500))
        binding.txtTextNumber3.setTextColor(ContextCompat.getColor(context, R.color.blue500))
    }

    private fun resetLayout(){
        binding.line1.background=ContextCompat.getDrawable(context, R.color.gray)
        binding.line2.background=ContextCompat.getDrawable(context, R.color.gray)

        binding.txtNumber1.background=ContextCompat.getDrawable(context, R.drawable.bg_step_inactive)
        binding.txtNumber2.background=ContextCompat.getDrawable(context, R.drawable.bg_step_inactive)
        binding.txtNumber3.background=ContextCompat.getDrawable(context, R.drawable.bg_step_inactive)

        binding.txtNumber1.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.txtNumber2.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.txtNumber3.setTextColor(ContextCompat.getColor(context, R.color.gray))

        binding.txtTextNumber1.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.txtTextNumber2.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.txtTextNumber3.setTextColor(ContextCompat.getColor(context, R.color.gray))
    }

}