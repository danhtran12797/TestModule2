package com.intelnet.omniwallet.ui.addWallet

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseActivity
import com.intelnet.omniwallet.databinding.ActivityAddWalletBinding
import com.intelnet.omniwallet.ui.addWallet.createWallet.ConfirmPhraseFragment
import com.intelnet.omniwallet.ui.addWallet.createWallet.MemorizePhraseFragment
import com.intelnet.omniwallet.ui.home.HomeActivity
import java.io.File

class AddWalletActivity : BaseActivity() {

    lateinit var binding: ActivityAddWalletBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentAddWallet) as NavHostFragment
        navController = navHostFragment.navController

        binding.toolbarAddWallet.imgBack.setOnClickListener {
            hideKeyboard()
            if (!navController.popBackStack()) {
                finish()
//                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
            } else {
                val fragment = getForegroundFragment()
                if (fragment != null && (fragment is MemorizePhraseFragment || fragment is ConfirmPhraseFragment))
                    deleteDir(File(filesDir, ""))
            }
        }
    }

    private fun getForegroundFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentAddWallet)
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    fun navigateHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("start_home", true)
        startActivity(intent)
        finish()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Check if no view has focus
        val currentFocusedView = currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val childrens = dir.list() ?: return false
            for (i in childrens.indices) {
                val success = deleteDir(File(dir, childrens[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
    }

    // clear focus on touch outside
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
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