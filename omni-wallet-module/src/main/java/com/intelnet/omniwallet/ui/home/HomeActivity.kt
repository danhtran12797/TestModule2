package com.intelnet.omniwallet.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseActivity
import com.intelnet.omniwallet.databinding.ActivityHomeBinding
import com.intelnet.omniwallet.ui.addWallet.AddWalletActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class HomeActivity : BaseActivity() {

    lateinit var binding: ActivityHomeBinding

    private var isActivityVisible: Boolean = true
    private var outOfTime: Boolean = false

    private var mHandler: Handler? = null
    private var mHandlerThread: HandlerThread? = null
    private val runnable: Runnable by lazy {
        Runnable {
            Timber.d("TIMEOUT")
            outOfTime = true
            backToLoginScreen()
        }
    }

    private val keydir: File by lazy {
        File(filesDir, "keystore")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun backToLoginScreen() {
        if (!isActivityVisible) return
        val mIntent = intent
        finish()
        startActivity(mIntent)
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        if (outOfTime) backToLoginScreen()
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    override fun onStart() {
        super.onStart()
        if (!keydir.exists()) {
            navigateAddWalletActivity()
        }
    }

    fun navigateAddWalletActivity(){
        startActivity(Intent(this, AddWalletActivity::class.java))
        finish()
    }

    private fun startHandlerThread() {
        removeHandlerTimeout()
        mHandlerThread = null
        mHandler = null

        mHandlerThread = HandlerThread("Handler Thread")
        mHandlerThread?.start()
        mHandlerThread?.let {
            mHandler = Handler(it.looper)
        }
    }

    private fun restart(timeCountDown: Long = 30000) { // 30 second
        mHandler?.removeCallbacks(runnable)
        mHandler?.postDelayed(runnable, timeCountDown)
    }

    fun startHandlerTimeout(timeCountDown: Long = 30000) { // 30 second
        startHandlerThread()
        mHandler?.postDelayed(runnable, timeCountDown)
    }

    private fun removeHandlerTimeout() {
        mHandlerThread?.uncaughtExceptionHandler = null
        mHandler?.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        removeHandlerTimeout()
        mHandlerThread = null
        mHandler = null
        super.onDestroy()
    }

    // clear focus on touch outside
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        restart()
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}